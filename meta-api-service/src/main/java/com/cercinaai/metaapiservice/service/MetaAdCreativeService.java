package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.Campaign;
import com.cercinaai.metaapiservice.entity.MetaAccount;
import com.cercinaai.metaapiservice.entity.MetaAdCreative;
import com.cercinaai.metaapiservice.repository.MetaAccountRepository;
import com.cercinaai.metaapiservice.repository.MetaAdCreativeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class MetaAdCreativeService {

    private final MetaAdCreativeRepository adCreativeRepository;
    private final MetaAccountRepository accountRepository;
    private final MetaTokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${meta.page-id}")
    private String pageId;

    @Value("${meta.instagram-id:}")
    private String instagramActorId;

    private final String adAccountId = "act_121780531366304";

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com/v19.0")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public MetaAdCreative create(MetaAdCreative creative, MultipartFile imageFile) {
        log.info("Création d’un AdCreative Meta: {}", creative.getName());

        try {
            /* creative.setCreatedAt(LocalDateTime.now());*/

            // 1. Sauvegarde initiale en BDD (sans metaCreativeId)
            MetaAdCreative saved = adCreativeRepository.save(creative);

            tokenService.refreshAccessTokenIfNeeded();
            MetaAccount account = accountRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Meta account introuvable"));

            if (account.getAccessToken() == null || account.getAccessToken().isEmpty()) {
                throw new RuntimeException("Token d'accès Meta invalide ou expiré");
            }

            // 2. Upload image Meta et création AdCreative
            String imageHash = uploadImageToMeta(imageFile, account.getAccessToken());
            System.out.println(imageHash);
            String creativeId = createCreativeOnMeta(saved, imageHash, account);

            // 3. Mise à jour locale
            saved.setImageHash(imageHash);
            saved.setMetaAdCretaive(creativeId);
            saved.setFullSpecJson(buildFullSpecJson(imageHash, creative.getLink(), creative.getMessage()));

            saved = adCreativeRepository.save(saved);

            log.info("✅ AdCreative créé avec succès. ID local: {}, ID Meta: {}", saved.getId(), creativeId);
            return saved;

        } catch (WebClientResponseException e) {
            log.error("Erreur API Meta (AdCreative): Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur Meta API : " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Erreur lors de la création de l’AdCreative", e);
            throw new RuntimeException("Erreur interne : " + e.getMessage());
        }
    }



    public String getCreativeImageUrl(String creativeId, String accessToken) {
        String fields = "id,name,object_story_spec,thumbnail_url,image_url,effective_object_story_id";

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + creativeId)
                        .queryParam("fields", fields)
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    // 1. Try object_story_spec.link_data.picture
                    JsonNode pictureNode = json.at("/object_story_spec/link_data/picture");
                    if (!pictureNode.isMissingNode()) {
                        return pictureNode.asText();
                    }

                    // 2. Try thumbnail_url
                    JsonNode thumbnailUrl = json.get("thumbnail_url");
                    if (thumbnailUrl != null && !thumbnailUrl.isNull()) {
                        return thumbnailUrl.asText();
                    }

                    // 3. Try image_url
                    JsonNode imageUrl = json.get("image_url");
                    if (imageUrl != null && !imageUrl.isNull()) {
                        return imageUrl.asText();
                    }

                    return null; // Aucun champ trouvé
                })
                .block(); // ✅ convertit le flux réactif en String (synchrone)
    }



    private String uploadImageToMeta(MultipartFile file, String accessToken) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Fichier image manquant ou vide !");
            }

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource()); // ✅ clé correcte + bonne ressource
            builder.part("access_token", accessToken);

            log.info("Image name: {}", file.getOriginalFilename());
            log.info("Image content-type: {}", file.getContentType());
            log.info("Image size: {}", file.getSize());

            JsonNode response = webClient.post()
                    .uri("/" + adAccountId + "/adimages")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            log.info("Réponse Meta Image Upload: {}", response);

            if (response != null && response.has("images")) {
                return response.get("images").elements().next().get("hash").asText();
            } else {
                throw new RuntimeException("Réponse invalide de Meta lors de l’upload d’image : " + response);
            }

        } catch (WebClientResponseException e) {
            log.error("❌ Erreur Meta API - Upload Image : {}", e.getResponseBodyAsString());
            throw new RuntimeException("Upload échoué : " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("❌ Exception technique upload image Meta", e);
            throw new RuntimeException("Upload échoué : " + e.getMessage());
        }
    }


    private String createCreativeOnMeta(MetaAdCreative creative, String imageHash, MetaAccount account) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

            String objectStorySpec = buildFullSpecJson(imageHash, creative.getLink(), creative.getMessage());

            formData.add("name", creative.getName());
            formData.add("object_story_spec", objectStorySpec);
            formData.add("access_token", account.getAccessToken());

            JsonNode response = webClient.post()
                    .uri("/" + adAccountId + "/adcreatives")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("id")) {
                return response.get("id").asText();
            } else {
                throw new RuntimeException("Réponse invalide de Meta lors de la création de l’AdCreative : " + response);
            }

        } catch (WebClientResponseException e) {
            log.error("Erreur API Meta (createCreative): {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    private String buildFullSpecJson(String imageHash, String link, String message) {
        ObjectNode spec = objectMapper.createObjectNode();
        spec.put("page_id", pageId);

        if (instagramActorId != null && !instagramActorId.isEmpty()) {
            spec.put("instagram_actor_id", instagramActorId);
        }

        ObjectNode linkData = objectMapper.createObjectNode();
        linkData.put("image_hash", imageHash);
        linkData.put("link", link);
        linkData.put("message", message);

        spec.set("link_data", linkData);

        return spec.toString();
    }



    public MetaAdCreative getMetaAdCreativeById(String idCreative ) {

            return adCreativeRepository.findBymetaAdCretaive(idCreative).get();
    }

    /*private String uploadImageToMeta(MultipartFile file, String accessToken) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("access_token", accessToken);
            builder.part("bytes", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);

            JsonNode response = webClient.post()
                    .uri("/" + adAccountId + "/adimages")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("images")) {
                return response.get("images").elements().next().get("hash").asText();
            } else {
                throw new RuntimeException("Réponse invalide de Meta lors de l’upload d’image : " + response);
            }

        } catch (Exception e) {
            log.error("Erreur upload image Meta", e);
            throw new RuntimeException("Upload échoué : " + e.getMessage());
        }
    }
*/
     /*   private String uploadImageToMeta(MultipartFile file, String accessToken) {
            System.out.println(accessToken);
            try {
                if (file == null || file.isEmpty()) {
                    throw new RuntimeException("Fichier image manquant ou vide !");
                }

                MultipartBodyBuilder builder = new MultipartBodyBuilder();

                builder.part("file", new ByteArrayResource(file.getBytes()) {
                            @Override
                            public String getFilename() {
                                return file.getOriginalFilename();
                            }
                        })

                        .header("Content-Disposition", "form-data; name=file; filename=\"" + file.getOriginalFilename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM);
                builder.part("access_token", accessToken);
                log.info("Image name: {}", file.getOriginalFilename());
                log.info("Image content-type: {}", file.getContentType());
                log.info("Image size: {}", file.getSize());
                JsonNode response = webClient.post()
                        .uri("/" + adAccountId + "/adimages")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(builder.build()))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();

                System.out.println(response);

                if (response != null && response.has("images")) {
                    return response.get("images").elements().next().get("hash").asText();
                } else {
                    throw new RuntimeException("Réponse invalide de Meta lors de l’upload d’image : " + response);
                }

            } catch (WebClientResponseException e) {
                log.error("❌ Erreur Meta API - Upload Image : {}", e.getResponseBodyAsString());
                throw new RuntimeException("Upload échoué : " + e.getResponseBodyAsString());

            } catch (Exception e) {
                log.error("❌ Exception technique upload image Meta", e);
                throw new RuntimeException("Upload échoué : " + e.getMessage());
            }
        }*/

}
