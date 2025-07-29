package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.MetaAccount;
import com.cercinaai.metaapiservice.entity.MetaAd;
import com.cercinaai.metaapiservice.repository.MetaAccountRepository;
import com.cercinaai.metaapiservice.repository.MetaAdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaAdService {

    private final MetaAdRepository metaAdRepository;
    private final MetaAccountRepository metaAccountRepository;
    private final MetaTokenService tokenService;

    private String adAccountId="act_121780531366304";

    @Value("${meta.page-id}")
    private String pageId;
    private String instagramAccountId="";

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com/v19.0")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

    public MetaAd create(MetaAd ad) {
        log.info("Création d’un Ad Meta: {}", ad.getName());

        try {
            MetaAd saved = metaAdRepository.save(ad);

            tokenService.refreshAccessTokenIfNeeded();
            MetaAccount account = metaAccountRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("MetaAccount introuvable"));

            if (account.getAccessToken() == null || account.getAccessToken().isEmpty()) {
                throw new RuntimeException("Token d'accès Meta manquant ou invalide");
            }

            // 1. Créer un AdCreative temporaire
            String creativeId = createAdCreative(account, ad);

            // 2. Créer le Ad avec le creative_id
            String metaAdId = createAdOnMeta(ad, creativeId, account);

            saved.setMetaAdSetId(metaAdId);
            saved = metaAdRepository.save(saved);

            log.info("✅ Ad créé avec succès. ID local: {}, ID Meta: {}", saved.getId(), metaAdId);
            return saved;

        } catch (WebClientResponseException e) {
            log.error("Erreur API Meta: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la création d’Ad Meta: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Exception lors de la création d’Ad: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l’appel à Meta API : " + e.getMessage());
        }
    }

    private String createAdCreative(MetaAccount account, MetaAd ad) {
        try {
            WebClient client = WebClient.create("https://graph.facebook.com/v19.0");
            ObjectMapper mapper = new ObjectMapper();

            // Construire object_story_spec
            ObjectNode objectStorySpec = mapper.createObjectNode();
            objectStorySpec.put("page_id",pageId);

            if (instagramAccountId!= null && !instagramAccountId.isEmpty()) {
                objectStorySpec.put("instagram_actor_id", instagramAccountId);
            }

            ObjectNode linkData = mapper.createObjectNode();
            linkData.put("image_hash", "f132c8f3d4ba810fb4f9f322f897933e");
            linkData.put("link", "https://tonsite.com/annonce/123");
            linkData.put("message", "TTTT");

            objectStorySpec.set("link_data", linkData);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("name", "TTTTTTT");
            formData.add("object_story_spec", objectStorySpec.toString());
            formData.add("access_token", account.getAccessToken());

            JsonNode response = client.post()
                    .uri("/" + adAccountId + "/adcreatives")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("id")) {
                return response.get("id").asText();
            } else {
                throw new RuntimeException("Réponse invalide lors de la création du AdCreative : " + response);
            }

        } catch (WebClientResponseException e) {
            log.error("Erreur Meta API - AdCreative: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    private String createAdOnMeta(MetaAd ad, String creativeId, MetaAccount account) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", ad.getName());
        System.out.println(ad.getMetaAdSetId());
        formData.add("adset_id", ad.getMetaAdSetId());
        formData.add("status","PAUSED");
        formData.add("creative", "{\"creative_id\":\"" + creativeId + "\"}");
        formData.add("access_token", account.getAccessToken());

        JsonNode response = webClient.post()
                .uri("/" + adAccountId + "/ads")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response != null && response.has("id")) {
            return response.get("id").asText();
        } else {
            throw new RuntimeException("Réponse invalide lors de la création du Ad : " + response);
        }
    }
}
