package com.bank.assets.modules.ai;

import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.enums.UserRole;
import com.bank.assets.modules.asset.Asset;
import com.bank.assets.modules.asset.AssetRepository;
import com.bank.assets.modules.history.AssetHistory;
import com.bank.assets.modules.history.AssetHistoryRepository;
import com.bank.assets.modules.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiToolRegistry {

    private final AssetRepository assetRepository;
    private final AssetHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public ArrayNode buildDeclarations(User user) {
        ArrayNode declarations = objectMapper.createArrayNode();

        declarations.add(declare(
            "searchAvailableAssets",
            "Search for assets with REGISTERED status that can be requested or assigned. " +
            "Call this when the user asks about finding, needing, or checking availability of any asset. " +
            "Both parameters are optional - omit them to list all available assets. " +
            "The query matches against asset name, brand, model, type, and category. " +
            "If the result is empty, retry with synonymous or broader terms: " +
            "'laptop' -> also try 'notebook' or 'computer'; " +
            "'monitor' -> also try 'display' or 'screen'; " +
            "'phone' -> also try 'mobile' or 'device'; " +
            "'printer' -> also try 'scanner'. " +
            "Try at most 2 retries before telling the user nothing is available.",
            params(
                prop("query", "Search term matching asset name, brand, model, type name, or category name. Use synonyms if first attempt returns empty."),
                prop("categoryName", "Category filter e.g. IT, Office, Security. Use only when user explicitly mentions a category.")
            ),
            List.of()
        ));

        declarations.add(declare(
            "getMyAssets",
            "Get all assets currently assigned to the current user. " +
            "Call this for 'my assets', 'my equipment', 'what laptop do I have', etc.",
            params(),
            List.of()
        ));

        declarations.add(declare(
            "getAssetDetails",
            "Get full details of a specific asset by its serial number: " +
            "specs, warranty date, current status, and who it's assigned to.",
            params(prop("serialNumber", "The asset serial number")),
            List.of("serialNumber")
        ));

        declarations.add(declare(
            "getAssetHistory",
            "Get the full history of a specific asset: assignments, repairs, status changes, issues. " +
            "Call this when the user asks about an asset's past or whether it's been repaired before.",
            params(prop("serialNumber", "The asset serial number")),
            List.of("serialNumber")
        ));

        // managers and admins get dept lvl visibility
        if (isManagerOrAdmin(user)) {
            declarations.add(declare(
                "getDepartmentAssets",
                "Get all assets belonging to a department. " +
                "Use when a manager asks about what assets their department has.",
                params(prop("deptName", "Department name to look up")),
                List.of("deptName")
            ));
        }
        return declarations;
    }

    @Transactional(readOnly = true)
    public Object execute(String name, JsonNode args, User user) {
        log.debug("AI tool call: {}({})", name, args);
        return switch (name) {
            case "searchAvailableAssets" -> searchAvailableAssets(
                args.path("query").asText(null),
                args.path("categoryName").asText(null)
            );
            case "getMyAssets" -> getMyAssets(user);
            case "getAssetDetails" -> getAssetDetails(args.path("serialNumber").asText());
            case "getAssetHistory" -> getAssetHistory(args.path("serialNumber").asText());
            case "getDepartmentAssets" -> isManagerOrAdmin(user)
                    ? getDepartmentAssets(args.path("deptName").asText())
                    : Map.of("error", "Insufficient permissions");
            default -> Map.of("error", "Unknown tool: " + name);
        };
    }

    private static final float SIMILARITY_THRESHOLD = 0.25f;

    private List<Map<String, Object>> searchAvailableAssets(String query, String categoryName) {
        Specification<Asset> spec = (root, q, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("status"), AssetStatus.REGISTERED));

            if (query != null && !query.isBlank()) {
                String like = "%" + query.toLowerCase() + "%";
                String term = query.toLowerCase();

                // like handles partial matches; similarity() handles typos via pg_trgm
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("brand")), like),
                    cb.like(cb.lower(root.get("model")), like),
                    cb.like(cb.lower(root.get("type").get("name")), like),
                    cb.like(cb.lower(root.get("category").get("name")), like),
                    cb.greaterThan(cb.function("similarity", Float.class, cb.lower(root.get("name")), cb.literal(term)), SIMILARITY_THRESHOLD),
                    cb.greaterThan(cb.function("similarity", Float.class, cb.lower(root.get("brand")), cb.literal(term)), SIMILARITY_THRESHOLD),
                    cb.greaterThan(cb.function("similarity", Float.class, cb.lower(root.get("model")), cb.literal(term)), SIMILARITY_THRESHOLD),
                    cb.greaterThan(cb.function("similarity", Float.class, cb.lower(root.get("type").get("name")), cb.literal(term)), SIMILARITY_THRESHOLD)
                ));
            }
            if (categoryName != null && !categoryName.isBlank()) {
                predicates.add(cb.like(
                    cb.lower(root.get("category").get("name")),
                    "%" + categoryName.toLowerCase() + "%"
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return assetRepository.findAll(spec, PageRequest.of(0, 15))
            .getContent().stream()
            .map(this::assetToMap)
            .toList();
    }

    private List<Map<String, Object>> getMyAssets(User user) {
        return assetRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("owner").get("id"), user.getId()),
                PageRequest.of(0, 20))
            .getContent().stream()
            .map(this::assetToMap)
            .toList();
    }

    private Object getAssetDetails(String serialNumber) {
    return assetRepository.findBySerialNumber(serialNumber)
        .<Object>map(a -> Map.ofEntries(
            Map.entry("name", nullSafe(a.getName())),
            Map.entry("serialNumber", nullSafe(a.getSerialNumber())),
            Map.entry("brand", nullSafe(a.getBrand())),
            Map.entry("model", nullSafe(a.getModel())),
            Map.entry("category", a.getCategory() != null ? a.getCategory().getName() : "-"),
            Map.entry("type", a.getType() != null ? a.getType().getName() : "-"),
            Map.entry("status", a.getStatus().name()),
            Map.entry("warrantyUntil", nullSafe(a.getWarrantyUntil())),
            Map.entry("purchaseDate", nullSafe(a.getPurchaseDate())),
            Map.entry("owner", a.getOwner() != null ? a.getOwner().getFullName() : "unassigned"),
            Map.entry("notes", nullSafe(a.getNotes())),
            Map.entry("specifications", a.getSpecifications() != null ? a.getSpecifications() : Map.of())
        ))
        .orElse(Map.of("error", "Asset with serial number '" + serialNumber + "' not found"));
    }

    private List<Map<String, Object>> getAssetHistory(String serialNumber) {
        return assetRepository.findBySerialNumber(serialNumber)
            .map(asset -> historyRepository
                .findByAssetIdOrderByChangedAtDesc(asset.getId(), PageRequest.of(0, 20))
                .getContent().stream()
                .map(this::historyToMap)
                .toList())
            .orElse(List.of(Map.of("error", "Asset '" + serialNumber + "' not found")));
    }

    private List<Map<String, Object>> getDepartmentAssets(String deptName) {
        Specification<Asset> spec = (root, q, cb) ->
            cb.like(cb.lower(root
                .get("department")
                .get("name")),
                "%" + deptName.toLowerCase() + "%"
            );

        return assetRepository.findAll(spec, PageRequest.of(0, 20))
            .getContent().stream()
            .map(this::assetToMap)
            .toList();
    }

    private Map<String, Object> assetToMap(Asset a) {
        return Map.ofEntries(
            Map.entry("name", a.getName()),
            Map.entry("serialNumber", a.getSerialNumber()),
            Map.entry("brand", nullSafe(a.getBrand())),
            Map.entry("model", nullSafe(a.getModel())),
            Map.entry("category", a.getCategory() != null ? a.getCategory().getName() : "-"),
            Map.entry("type", a.getType() != null ? a.getType().getName() : "-"),
            Map.entry("status", a.getStatus().name()),
            Map.entry("warrantyUntil", nullSafe(a.getWarrantyUntil())),
            Map.entry("owner", a.getOwner() != null ? a.getOwner().getFullName() : "unassigned"),
            Map.entry("department", a.getDepartment() != null ? a.getDepartment().getName() : "unassigned"),
            Map.entry("branch", a.getBranch() != null ? a.getBranch().getName() : "unassigned")
        );
    }

    private Map<String, Object> historyToMap(AssetHistory h) {
        return Map.of(
            "action", h.getAction().name(),
            "oldStatus", h.getOldStatus() != null ? h.getOldStatus().name() : "-",
            "newStatus", h.getNewStatus() != null ? h.getNewStatus().name() : "-",
            "changedBy", h.getChangedBy() != null ? h.getChangedBy().getFullName() : "-",
            "changedAt", h.getChangedAt().toString(),
            "reason", nullSafe(h.getReason())
        );
    }

    private ObjectNode declare(String name, String description, ObjectNode parameters, List<String> required) {
        ObjectNode fn = objectMapper.createObjectNode();
        fn.put("name", name);
        fn.put("description", description);
        if (!required.isEmpty()) {
            ArrayNode req = parameters.putArray("required");
            required.forEach(req::add);
        }
        fn.set("parameters", parameters);
        return fn;
    }

    private ObjectNode params(ObjectNode... props) {
        ObjectNode p = objectMapper.createObjectNode();
        p.put("type", "object");
        if (props.length > 0) {
            ObjectNode properties = p.putObject("properties");
            for (ObjectNode prop : props) {
                String name = prop.path("_name").asText();
                prop.remove("_name");
                properties.set(name, prop);
            }
        }
        return p;
    }

    private ObjectNode prop(String name, String description) {
        ObjectNode p = objectMapper.createObjectNode();
        p.put("_name", name);
        p.put("type", "string");
        p.put("description", description);
        return p;
    }

    private boolean isManagerOrAdmin(User user) {
        return user.getRole() == UserRole.ADMIN
                || user.getRole() == UserRole.DEPT_MANAGER
                || user.getRole() == UserRole.BRANCH_MANAGER;
    }

    private String nullSafe(Object o) {
        return o != null ? o.toString() : "-";
    }
}
