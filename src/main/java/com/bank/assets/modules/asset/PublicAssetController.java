package com.bank.assets.modules.asset;

import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name="Asset - Public")
@RequestMapping("/pub/asset")
@RequiredArgsConstructor
public class PublicAssetController {
    private final AssetRepository assetRepository;
    private final QrService qrService;


    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable UUID id) {
        if (!assetRepository.existsById(id)) {
            throw AppException.notFound(ErrorCode.ASSET_NOT_FOUND);
        }
        byte[] png = qrService.generateAssetQr(id);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(png);
    }
}
