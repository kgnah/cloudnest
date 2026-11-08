package io.github.kgnah.cloudnest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class GithubWebhookSignatureService {

    @Value("${cloudnest.webhook.secret}")
    private String webhookSecret;

    public boolean isValidSignature(String payload, String signature) {

        if (payload == null || signature == null || !signature.startsWith("sha256")) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8));

            String expectedSignature = "sha256=" + HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            throw new IllegalStateException("Could not verify Github webhook signature", e);
        }
    }
}
