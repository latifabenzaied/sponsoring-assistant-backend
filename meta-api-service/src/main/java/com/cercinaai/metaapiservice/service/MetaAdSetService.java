package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.MetaAccount;
import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.entity.MetaAdSetRequest;
import com.cercinaai.metaapiservice.entity.MetaCampaign;
import com.cercinaai.metaapiservice.repository.MetaAccountRepository;
import com.cercinaai.metaapiservice.repository.MetaAdRepository;
import com.cercinaai.metaapiservice.repository.MetaAdSetRepository;
import com.cercinaai.metaapiservice.repository.MetaCampaignRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaAdSetService {

    private final MetaAdSetRepository metaAdSetRepository;
    private final MetaCampaignRepository  metaCampaignRepository;
    private final MetaAccountRepository metaAccountRepository;
    private final MetaTokenService tokenService;

    @Value("${meta.ad.account-id:act_121780531366304}")
    private String adAccountId;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com/v19.0")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

    public MetaAdSet create(MetaAdSetRequest adSet) {
        log.info("Création d’un AdSet Meta: {}", adSet.getName());

        try {
            tokenService.refreshAccessTokenIfNeeded();
            MetaAccount account = metaAccountRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("MetaAccount introuvable"));

            if (account.getAccessToken() == null || account.getAccessToken().isEmpty()) {
                throw new RuntimeException("Token d'accès Meta manquant ou invalide");
            }
            MetaCampaign campaign = metaCampaignRepository.findByMetaCampaignId(adSet.getMetaCampaignId())
                    .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

            if (adSetExists(campaign, adSet)) {
                throw new RuntimeException("Un AdSet similaire existe déjà pour cette campagne.");
            }
            String generatedName = generateAdSetName(adSet);

            MetaAdSet adSetSaved = MetaAdSet.builder()
                    .name(generatedName)
                    .dailyBudget(adSet.getDailyBudget())
                    .billingEvent(adSet.getBillingEvent())
                    .optimizationGoal(adSet.getOptimizationGoal())
                    .status(adSet.getStatus())
                    .bidStrategy(adSet.getBidStrategy())
                    .bidAmount(adSet.getBidAmount())
                    .targetingJson(adSet.getTargetingJson())
                    .startTime(adSet.getStartTime())
                    .endTime(adSet.getEndTime())
                    .campaign(campaign)
                    .build();
            String metaAdSetId = createAdSetOnMeta(adSetSaved, account);
            adSetSaved.setMetaAdSetId(metaAdSetId);
            adSetSaved= metaAdSetRepository.save(adSetSaved);

           /* log.info("✅ AdSet créé avec succès. ID local: {}, ID Meta: {}", adSetSaved.getId(), metaAdSetId);*/
            return adSetSaved;

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
        System.out.println(adSet);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");// Map<String, Object>
        formData.add("name", adSet.getName());
        formData.add("campaign_id", adSet.getCampaign().getMetaCampaignId());
        formData.add("daily_budget", adSet.getDailyBudget().toString());
        formData.add("billing_event", adSet.getBillingEvent());
        formData.add("optimization_goal", adSet.getOptimizationGoal());
        formData.add("start_time", adSet.getStartTime().atOffset(java.time.ZoneOffset.of("+0000")).format(formatter));
        formData.add("end_time", adSet.getEndTime().atOffset(java.time.ZoneOffset.of("+0000")).format(formatter));
        formData.add("status", adSet.getStatus().name());
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
                log.info("AdSet Meta créé avec ID: {}", adSetId);
                return adSetId;
            } else {
                throw new RuntimeException("Réponse invalide de Meta API: " + response);
            }

        } catch (WebClientResponseException e) {
            log.error("Détail erreur API Meta: Status {}, Headers {}, Body {}", e.getStatusCode(), e.getHeaders(), e.getResponseBodyAsString());
            throw e;
        }
    }


    public List<MetaAdSet> getAllCampaigns() {
        try {
            return metaAdSetRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }


    private boolean adSetExists(MetaCampaign campaign, MetaAdSetRequest request) {
        List<MetaAdSet> existingAdSets = metaAdSetRepository.findByCampaign(campaign);

        return existingAdSets.stream().anyMatch(existing ->
                                existing.getDailyBudget().equals(request.getDailyBudget()) &&
                                existing.getBillingEvent().equals(request.getBillingEvent()) &&
                                existing.getOptimizationGoal().equals(request.getOptimizationGoal()) &&
                                existing.getTargetingJson().equals(request.getTargetingJson()) &&
                                existing.getStartTime().equals(request.getStartTime()) &&
                                existing.getEndTime().equals(request.getEndTime())

        );
    }


    private String generateAdSetName(MetaAdSetRequest request) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM");

        String audienceLabel = extractAudienceLabel(request.getTargetingJson()); // facultatif
        String date = request.getStartTime().format(dateFormatter);
        String budget = request.getDailyBudget() != null ? request.getDailyBudget() + "€" : "BudgetNA";

        return String.format("AdSet_%s_%s_%s",
                audienceLabel,
                budget,
                date
        );
    }
    private String extractAudienceLabel(String targetingJson) {
        try {
            JsonNode targeting = new ObjectMapper().readTree(targetingJson);
            String geo = targeting.at("/geo_locations/countries/0").asText("NA");
            String ageMin = targeting.path("age_min").asText("?");
            String ageMax = targeting.path("age_max").asText("?");

            return geo + "_A" + ageMin + "-" + ageMax;
        } catch (Exception e) {
            return "AudienceNA";
        }
    }

}
