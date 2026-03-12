package com.bank.assets.modules.meta;

import com.bank.assets.common.enums.AssetAction;
import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.enums.UserStatus;
import com.bank.assets.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name="Meta")
@RequestMapping("/meta")
public class MetaController {
    @GetMapping("/enums")
    public ApiResponse<Map<String, List<String>>> getEnums() {
        return ApiResponse.ok(Map.of(
            "assetStatuses", enumNames(AssetStatus.values()),
            "assetActions", enumNames(AssetAction.values()),
            "userRoles", enumNames(UserRole.values()),
            "userStatuses", enumNames(UserStatus.values())
        ));
    }

    private List<String> enumNames(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
