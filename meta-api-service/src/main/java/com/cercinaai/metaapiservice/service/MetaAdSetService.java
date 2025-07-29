package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.MetaAccount;
import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.repository.MetaAccountRepository;
import com.cercinaai.metaapiservice.repository.MetaAdRepository;
import com.cercinaai.metaapiservice.repository.MetaAdSetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaAdSetService {

    private final MetaAdSetRepository metaAdSetRepository;
    private final MetaAccountRepository metaAccountRepository;
    private final MetaTokenService tokenService;

    @Value("${meta.ad.account-id:act_121780531366304}")
    private String adAccountId;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com/v19.0")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

    public MetaAdSet create(MetaAdSet adSet) {
        log.info("Création d’un AdSet Meta: {}", adSet.getName());

        try {

            MetaAdSet saved = metaAdSetRepository.save(adSet);

            tokenService.refreshAccessTokenIfNeeded();
            MetaAccount account = metaAccountRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("MetaAccount introuvable"));

            if (account.getAccessToken() == null || account.getAccessToken().isEmpty()) {
                throw new RuntimeException("Token d'accès Meta manquant ou invalide");
            }

            String metaAdSetId = createAdSetOnMeta(saved, account);

            saved.setMetaAdSetId(metaAdSetId);
            saved = metaAdSetRepository.save(saved);

            log.info("✅ AdSet créé avec succès. ID local: {}, ID Meta: {}", saved.getId(), metaAdSetId);
            return saved;

        } catch (WebClientResponseException e) {
            log.error("Erreur API Meta: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la création d’AdSet Meta: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error(" Exception lors de la création d’AdSet: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l’appel à Meta API : " + e.getMessage());
        }
    }

    private String createAdSetOnMeta(MetaAdSet adSet, MetaAccount account) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
       /* ObjectMapper mapper = new ObjectMapper();
        JsonNode targetingJson;
        try {
            targetingJson = mapper.readTree(adSet.getTargetingJson()); // ✅ important !
        } catch (Exception e) {
            throw new RuntimeException("Le champ 'targetingJson' n'est pas un JSON valide : " + e.getMessage());
        }*/
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");// Map<String, Object>
        formData.add("name", adSet.getName());
        formData.add("campaign_id", adSet.getMetaCampaignId());
        formData.add("daily_budget", adSet.getDailyBudget().toString());
        formData.add("billing_event", adSet.getBillingEvent());
        formData.add("optimization_goal", adSet.getOptimizationGoal());
        formData.add("start_time", adSet.getStartTime().atOffset(java.time.ZoneOffset.of("+0000")).format(formatter));
        formData.add("end_time", adSet.getEndTime().atOffset(java.time.ZoneOffset.of("+0000")).format(formatter));
        formData.add("status", adSet.getStatus());
        formData.add("targeting", adSet.getTargetingJson());
        formData.add("access_token", account.getAccessToken());
        formData.add("bid_strategy", adSet.getBidStrategy());

        if ("BID_CAP".equals(adSet.getBidStrategy()) && adSet.getBidAmount() != null) {
            formData.add("bid_amount", String.valueOf(adSet.getBidAmount()));
        }

        if ("VALUE".equals(adSet.getOptimizationGoal()) && adSet.getBidConstraints() != null) {
            formData.add("bid_constraints", adSet.getBidConstraints());
        }
        try {
            JsonNode response = webClient.post()
                    .uri("/" + adAccountId + "/adsets")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("id")) {
                String adSetId = response.get("id").asText();
                log.info("🟢 AdSet Meta créé avec ID: {}", adSetId);
                return adSetId;
            } else {
                throw new RuntimeException("Réponse invalide de Meta API: " + response);
            }

        } catch (WebClientResponseException e) {
            log.error("Détail erreur API Meta: Status {}, Headers {}, Body {}", e.getStatusCode(), e.getHeaders(), e.getResponseBodyAsString());
            throw e;
        }
    }
}
