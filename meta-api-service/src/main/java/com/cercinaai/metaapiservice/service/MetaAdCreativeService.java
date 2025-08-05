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
            String creativeId = createCreativeOnMeta(saved, imageHash, account);
            System.out.println("📸 ID de la creative retournée: " + creativeId);


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



  /*  public String getCreativeImageUrl(String creativeId, String accessToken) {
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
                    System.out.println("📄 Réponse JSON brute: " + json.toPrettyString());
                    // 1. Try object_story_spec.link_data.picture
                    JsonNode pictureNode = json.at("/object_story_spec/link_data/picture");
                    if (!pictureNode.isMissingNode()) {
                        return pictureNode.asText();
                    }
                    JsonNode imageUrl = json.get("image_url");
                    if (imageUrl != null && !imageUrl.isNull()) {
                        return imageUrl.asText().replaceAll("_s110x80_", "_n");
                    }
                    // 2. Try thumbnail_url
                    JsonNode thumbnailUrl = json.get("thumbnail_url");
                    if (thumbnailUrl != null && !thumbnailUrl.isNull()) {
                        return thumbnailUrl.asText();
                    }
                    // 3. Try image_url

                    return null; // Aucun champ trouvé
                })
                .block();
    }*/

    public String getCreativeImageUrl(String creativeId, String accessToken) {
        String fields = "id,effective_object_story_id,object_story_spec,thumbnail_url,image_url";

        JsonNode creativeJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + creativeId)
                        .queryParam("fields", fields)
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        System.out.println("📄 JSON AdCreative: " + creativeJson.toPrettyString());

        // 1. Récupère effective_object_story_id
        JsonNode storyIdNode = creativeJson.get("effective_object_story_id");
        if (storyIdNode != null && !storyIdNode.isNull()) {
            String storyId = storyIdNode.asText();

            JsonNode storyJson = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + storyId)
                            .queryParam("fields", "full_picture")
                            .queryParam("access_token", accessToken)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            System.out.println("🖼️ JSON Object Story: " + storyJson.toPrettyString());

            JsonNode fullPicture = storyJson.get("full_picture");
            if (fullPicture != null && !fullPicture.isNull()) {
                return fullPicture.asText(); // ✅ Haute qualité
            }
        }

        // Fallback: image_url
        JsonNode imageUrl = creativeJson.get("image_url");
        if (imageUrl != null && !imageUrl.isNull()) {
            return imageUrl.asText(); // (mais risque d’être basse qualité)
        }

        return null;
    }

/*public String getCreativeImageUrl(String creativeId, String accessToken) {
    String fields = "thumbnail_url";

    JsonNode response = webClient.get()
            .uri("/" + creativeId + "?fields=" + fields + "&access_token=" + accessToken)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

    if (response != null && response.has("thumbnail_url")) {
        String url = response.get("thumbnail_url").asText();
        log.info("📸 URL récupérée: {}", url);
        return url; // SANS MODIFICATION
    }

    return null;
}*/
/*
public String getCreativeImageUrl(String creativeId, String accessToken) {
    String fields = "id,name,object_story_spec,thumbnail_url,image_url,effective_object_story_id,image_hash,image_crops";

    log.info("🔍 Récupération de l'image pour creative ID: {}", creativeId);

    return webClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/" + creativeId)
                    .queryParam("fields", fields)
                    .queryParam("access_token", accessToken)
                    .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(json -> {
                // 📋 LOG COMPLET pour debug
                log.info("🔍 RÉPONSE COMPLÈTE Meta API pour creative {}: {}", creativeId, json.toPrettyString());

                // Vérifier tous les chemins possibles
                String imageUrl = null;

                // 1. object_story_spec.link_data.picture (URL directe)
                JsonNode pictureNode = json.at("/object_story_spec/link_data/picture");
                if (!pictureNode.isMissingNode() && !pictureNode.asText().isEmpty()) {
                    imageUrl = pictureNode.asText();
                    log.info("✅ Image trouvée dans object_story_spec.link_data.picture: {}", imageUrl);
                    return getFullSizeImageUrl(imageUrl); // Conversion en taille normale
                }

                // 2. object_story_spec.link_data.image_hash
                JsonNode imageHashNode = json.at("/object_story_spec/link_data/image_hash");
                if (!imageHashNode.isMissingNode() && !imageHashNode.asText().isEmpty()) {
                    String hash = imageHashNode.asText();
                    log.info("✅ Image hash trouvé: {}", hash);
                    // Construire l'URL à partir du hash
                    imageUrl = buildImageUrlFromHash(hash);
                    return imageUrl;
                }

                // 3. thumbnail_url
                JsonNode thumbnailUrl = json.get("thumbnail_url");
                if (thumbnailUrl != null && !thumbnailUrl.isNull() && !thumbnailUrl.asText().isEmpty()) {
                    imageUrl = thumbnailUrl.asText();
                    log.info("✅ Image trouvée dans thumbnail_url: {}", imageUrl);
                    return getFullSizeImageUrl(imageUrl); // Conversion en taille normale
                }

                // 4. image_url
                JsonNode imageUrlNode = json.get("image_url");
                if (imageUrlNode != null && !imageUrlNode.isNull() && !imageUrlNode.asText().isEmpty()) {
                    imageUrl = imageUrlNode.asText();
                    log.info("✅ Image trouvée dans image_url: {}", imageUrl);
                    return getFullSizeImageUrl(imageUrl); // Conversion en taille normale
                }

                // 5. Chercher dans image_crops si présent
                JsonNode imageCrops = json.get("image_crops");
                if (imageCrops != null && imageCrops.isArray() && imageCrops.size() > 0) {
                    JsonNode firstCrop = imageCrops.get(0);
                    JsonNode cropUrl = firstCrop.get("url");
                    if (cropUrl != null && !cropUrl.asText().isEmpty()) {
                        imageUrl = cropUrl.asText();
                        log.info("✅ Image trouvée dans image_crops: {}", imageUrl);
                        return getFullSizeImageUrl(imageUrl);
                    }
                }

                log.error("❌ AUCUNE IMAGE TROUVÉE pour creative: {}", creativeId);
                log.error("❌ Champs disponibles: {}", json.fieldNames());
                return null;
            })
            .block();
}
*/




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
