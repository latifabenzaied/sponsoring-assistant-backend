package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.*;
import com.cercinaai.metaapiservice.repository.CampaignRepository;
import com.cercinaai.metaapiservice.repository.MetaAccountRepository;
import com.cercinaai.metaapiservice.repository.MetaCampaignRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class MetaCampaignService {


    /* @Value("${meta.app.client-id}")*/
    private String adAccountId = "act_121780531366304";
    private final MetaTokenService tokenService;
    private final MetaCampaignRepository metaCampaignRepository;
    private final MetaAccountRepository metaAccountRepository;


    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com/v19.0")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();


    public MetaCampaign create(MetaCampaign campaign) {
        log.info("Création d'une nouvelle campagne Meta: {}", campaign.getName());

        try {

            campaign.setCreatedAt(LocalDateTime.now());
            MetaCampaign saved = metaCampaignRepository.save(campaign);

            tokenService.refreshAccessTokenIfNeeded();
            MetaAccount account = metaAccountRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Meta account not found"));

            if (account.getAccessToken() == null || account.getAccessToken().isEmpty()) {
                throw new RuntimeException("Token d'accès Meta invalide ou expiré");
            }

            String metaCampaignId = createCampaignOnMeta(saved, account);

            saved.setMetaCampaignId(metaCampaignId);
            saved = metaCampaignRepository.save(saved);

            log.info("Campagne créée avec succès. ID local: {}, ID Meta: {}",
                    saved.getId(), metaCampaignId);

            return saved;

        } catch (WebClientResponseException e) {
            log.error("Erreur API Meta: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la création de campagne Meta: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Erreur lors de la création de campagne: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'appel à Meta API : " + e.getMessage());
        }
    }

    private String createCampaignOnMeta(MetaCampaign campaign, MetaAccount account) {
        // Préparer les données du formulaire selon les specs Meta
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("name", campaign.getName());
        formData.add("objective", campaign.getObjective().name());
        formData.add("status", campaign.getStatus().name());
        formData.add("access_token", account.getAccessToken());
        formData.add("special_ad_categories", "[\"HOUSING\"]");
        try {
            JsonNode response = webClient.post()
                    .uri("/" + adAccountId + "/campaigns")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("id")) {
                String campaignId = response.get("id").asText();
                log.info("Campagne créée sur Meta avec l'ID: {}", campaignId);
                return campaignId;
            } else {
                throw new RuntimeException("Réponse invalide de Meta API: " + response);
            }

        } catch (WebClientResponseException e) {
            log.error("Erreur détaillée Meta API: Status: {}, Headers: {}, Body: {}",
                    e.getStatusCode(), e.getHeaders(), e.getResponseBodyAsString());
            throw e;
        }
    }


    public List<MetaCampaign> getAllCampaigns() {
        try {
            return metaCampaignRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }

}

