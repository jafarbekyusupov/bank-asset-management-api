package com.bank.assets.modules.asset;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class QrService {
    @Value("${app.qr.base-url}")
    private String baseUrl;

    @Value("${app.qr.size:300}")
    private int size;

    public byte[] generateAssetQr(UUID assetId) {
        String url = baseUrl + "/asset/" + assetId;
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.ERROR_CORRECTION, 
                ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("failed to generate QR code for asset {}: {}", assetId, e.getMessage());
            throw new RuntimeException("QR code generation failed", e);
        }
    }
}
