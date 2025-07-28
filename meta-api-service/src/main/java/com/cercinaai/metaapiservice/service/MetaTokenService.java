package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.MetaAccount;
import com.cercinaai.metaapiservice.repository.MetaAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class MetaTokenService {

    private final MetaAccountRepository metaAccountRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // ou injecté en @Bean
    @Value("${meta.app.client-id}")
    private String clientId;

    @Value("${meta.app.client-secret}")
    private String clientSecret;
    @Value("${meta.api.token.refresh.url:https://graph.facebook.com/v18.0/oauth/access_token}")
    private String tokenRefreshUrl;

    public void refreshAccessTokenIfNeeded() {
        MetaAccount account = metaAccountRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("MetaAccount not found"));

        boolean isExpired = account.getTokenExpiresAt() == null ||
                account.getTokenExpiresAt().isBefore(Instant.now().plusSeconds(1800)); // moins de 30 minutes
        System.out.printf(String.valueOf(isExpired));
        System.out.printf(String.valueOf(account.getTokenExpiresAt()));
        if (isExpired || !isAccessTokenValid(account.getAccessToken())) {
            URI uri = UriComponentsBuilder
                    .fromUriString(tokenRefreshUrl)
                    .queryParam("grant_type", "fb_exchange_token")
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("fb_exchange_token", account.getAccessToken())
                    .build().toUri();
            System.out.println(uri);
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String newAccessToken = response.getBody().get("access_token").asText();
                long expiresIn = response.getBody().get("expires_in").asLong();

                account.setAccessToken(newAccessToken);
                account.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
                account.setUpdatedAt(Instant.now());
                System.out.println(account);
                metaAccountRepository.save(account);
            } else {
                throw new RuntimeException("Échec du rafraîchissement du token Meta");
            }
        }
    }

    /**
     * Vérifie via l’API Meta si le token est toujours valide
     */
    public boolean isAccessTokenValid(String token) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/debug_token")
                .queryParam("input_token", token)
                .queryParam("access_token", token) // Self-debug
                .build().toUri();

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);
            return response.getBody().get("data").get("is_valid").asBoolean();
        } catch (Exception e) {
            return false;
        }
    }
}