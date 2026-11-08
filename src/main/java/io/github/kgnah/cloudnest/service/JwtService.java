package io.github.kgnah.cloudnest.service;

import io.github.kgnah.cloudnest.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import java.time.Instant;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // get signing key using the secret
    private SecretKey getSigningKey(){
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // generate token
    public String generateToken(User user) {
        SecretKey signingKey = getSigningKey();

        NimbusJwtEncoder encoder = NimbusJwtEncoder.withSecretKey(signingKey).build();

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId()) // this JWT belongs to this user
                .claim("email", user.getEmail())
                .issuedAt(now) // when the token is created
                .expiresAt(now.plusSeconds(3600)) // token expired after 1h
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
