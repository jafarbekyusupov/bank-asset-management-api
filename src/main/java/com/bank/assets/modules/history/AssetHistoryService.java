package com.bank.assets.modules.history;

import com.bank.assets.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetHistoryService {
    private final AssetHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public Page<AssetHistoryResponse> getAssetHistory(UUID assetId, Pageable pageable) {
        return historyRepository
                .findByAssetIdOrderByChangedAtDesc(assetId, pageable)
                .map(AssetHistoryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AssetHistoryResponse> getMyActivity(User user, Pageable pageable) {
        return switch (user.getRole()) {
            case STAFF -> historyRepository
                .findActivityForUser(user.getId(), pageable)
                .map(AssetHistoryResponse::from);

            case DEPT_MANAGER -> {
                if (user.getDepartment() == null) yield Page.empty(pageable);
                yield historyRepository
                    .findActivityForDept(user.getDepartment().getId(), pageable)
                    .map(AssetHistoryResponse::from);
            }

            case BRANCH_MANAGER -> {
                if (user.getBranch() == null) yield Page.empty(pageable);
                yield historyRepository
                    .findActivityForBranch(user.getBranch().getId(), pageable)
                    .map(AssetHistoryResponse::from);
            }

            case ADMIN -> historyRepository
                .findAll(pageable)
                .map(AssetHistoryResponse::from);
        };
    }
}
