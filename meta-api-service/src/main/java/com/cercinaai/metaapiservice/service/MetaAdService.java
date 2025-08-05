package com.cercinaai.metaapiservice.service;


import com.cercinaai.metaapiservice.entity.*;
import com.cercinaai.metaapiservice.repository.MetaAccountRepository;
import com.cercinaai.metaapiservice.repository.MetaAdRepository;
import com.cercinaai.metaapiservice.repository.MetaAdSetRepository;
import com.cercinaai.metaapiservice.repository.SponsoredAdMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaAdService {

    private final MetaAdRepository metaAdRepository;
    private final MetaAccountRepository metaAccountRepository;
    private final MetaTokenService tokenService;
    private final MetaAdCreativeService adCreativeService;
    private final MetaAdSetRepository metaAdSetRepository;
    private final SponsoredAdMappingService sponsoredAdMappingService;


    private String adAccountId = "act_121780531366304";

    @Value("${meta.page-id}")
    private String pageId;
    private String instagramAccountId = "";

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com/v19.0")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();


    public List<MetaAd> getAdsByAdSet(Long adSetId) {
        return metaAdRepository.findByAdSet_Id(adSetId);
    }

  /*  public MetaAd create(MetaAd ad, MultipartFile file) {
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
            String creativeId = createAdCreative(account, ad, file);
            System.out.println(creativeId);
            String metaAdId = createAdOnMeta(ad, creativeId, account);
            saved.setCreativeId(creativeId);
            *//*saved.setMetaAdSetId(metaAdId);*//*
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
    }*/

  public MetaAd createFromLocalImage(MetaAdRequest adRequest) throws IOException {
      log.info("Création d’un Ad Meta avec image stockée localement : {}", adRequest.getImageFileName());

      // 1. Construire le chemin local de l’image
      Path imagePath = Paths.get("uploads", adRequest.getImageFileName());

      if (!Files.exists(imagePath)) {
          throw new FileNotFoundException("Image non trouvée dans uploads/: " + imagePath);
      }


      byte[] imageBytes = Files.readAllBytes(imagePath);
      MultipartFile imageFile = new MockMultipartFile(
              "file", adRequest.getImageFileName(), "image/jpeg", imageBytes
      );

      return create(adRequest, imageFile);
  }


    public MetaAd create(MetaAdRequest adRequest, MultipartFile imageFile) {
        log.info("Création d’un Ad Meta avec AdCreative associé : {}", adRequest.getName());

        try {

            // 2. Authentification + récupération du compte
            tokenService.refreshAccessTokenIfNeeded();
            MetaAccount account = metaAccountRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("MetaAccount introuvable"));

            if (account.getAccessToken() == null || account.getAccessToken().isEmpty()) {
                throw new RuntimeException("Token d'accès Meta invalide ou expiré");
            }
            MetaAdSet adSet = metaAdSetRepository.findByMetaAdSetId(adRequest.getMetaAdSetId())
                    .orElseThrow(() -> new RuntimeException("AdSet introuvable"));

            System.out.println(adRequest.toString());
            MetaAdCreative creative = MetaAdCreative.builder()
                    .name(adRequest.getName())
                    .link("https://tonsite.com/annonce/123")
                    .message(adRequest.getMessage())
                    .build();

            MetaAd ad = MetaAd.builder()
                    .name(adRequest.getName())
                    .status(adRequest.getStatus())
                    .adSet(adSet)
                    .adCreative(creative)
                    .build();

            MetaAdCreative savedCreative = adCreativeService.create(creative, imageFile);

           /* String metaAdId = createAdOnMeta(ad, savedCreative.getMetaAdCretaive(), account);*/
           /* sponsoredAdMappingService.updateWithMetaAdId(adRequest.getSiteAdId(), metaAdId);*/
            /*Thread.sleep(3000);*/
           String imageUrl = adCreativeService.getCreativeImageUrl(
                    savedCreative.getMetaAdCretaive(),
                    account.getAccessToken()
            );
            System.out.println(imageUrl);

            ad.setImageUrl(imageUrl);
            ad.setCreativeId(savedCreative.getMetaAdCretaive());
            /*ad.setMetaAdId(metaAdId);*/
            ad.setCreatedAt(LocalDateTime.now());

            return metaAdRepository.save(ad);

        } catch (WebClientResponseException e) {
            log.error("Erreur API Meta (Ad): Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur Meta API : " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Erreur lors de la création de l’Ad Meta", e);
            throw new RuntimeException("Erreur interne : " + e.getMessage());
        }
    }


    private String createAdOnMeta(MetaAd ad, String creativeId, MetaAccount account) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", ad.getName());
        formData.add("adset_id", ad.getAdSet().getMetaAdSetId());
        formData.add("status", "PAUSED");
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


    public List<MetaAd> getAllMetaAd() {
        try {
            return metaAdRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }
}
