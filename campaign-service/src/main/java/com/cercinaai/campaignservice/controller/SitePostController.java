package com.cercinaai.campaignservice.controller;

import com.cercinaai.campaignservice.entity.SitePost;
import com.cercinaai.campaignservice.service.FileStorageService;
import com.cercinaai.campaignservice.service.SitePostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/sitePost")
@CrossOrigin("http://localhost:4201")
@RequiredArgsConstructor
public class SitePostController {
    private final SitePostService sitePostService;
    private final FileStorageService fileStorageService;

//    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> addSitePost(
//            @RequestPart("sitePost") SitePost sitePost,
//            @RequestPart(value = "photos", required = false) MultipartFile[] photos) {
//        List<String> photoUrls = null;
//        try {
//            photoUrls = fileStorageService.saveFiles(photos);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        sitePost.setPhotoUrls(photoUrls);
//        SitePost savedSitePost=sitePostService.addSitePost(sitePost);
//        return ResponseEntity.ok(savedSitePost);
//    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addSitePost(
            @RequestPart("sitePost") String sitePostJson,
            @RequestPart("images") List<MultipartFile> imageFiles
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            SitePost sitePost = objectMapper.readValue(sitePostJson, SitePost.class);
            System.out.println(sitePost);
            System.out.println(imageFiles);
            SitePost savedSitePost = sitePostService.addSitePost(sitePost, imageFiles);

            return ResponseEntity.ok(savedSitePost);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid sitePost JSON: " + e.getMessage());
        }
    }
    @GetMapping("/test-image")
    public ResponseEntity<String> testFileAccess() {
        System.out.printf("hhhhhhhhhhh");
        File imageFile = new File("C:/Users/hp/Desktop/sponsoring-assistant-backend/uploads/pp.png");

        System.out.println("🔍 Chemin absolu : " + imageFile.getAbsolutePath());
        System.out.println("📁 Existe ? " + imageFile.exists());
        System.out.println("🔓 Lisible ? " + imageFile.canRead());
        System.out.println("🧱 Est fichier ? " + imageFile.isFile());

        if (!imageFile.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok("✅ Le fichier est accessible.");
    }

    @GetMapping("")
    public ResponseEntity<List<SitePost>> getAll(
    ) {
        return ResponseEntity.ok(sitePostService.getAllSitePosts());
    }
    @GetMapping("/{idPost}")
    public ResponseEntity<SitePost> getSitePostById(@PathVariable int idPost
    ) {
        return ResponseEntity.ok(sitePostService.getSitePostById(idPost));
    }


    @PostMapping("/{idPost}")
    public ResponseEntity<?> updateSitePost(@PathVariable int idPost, @RequestBody SitePost sitePost){
        return ResponseEntity.ok(sitePostService.updateSitePost(idPost,sitePost));
    }


    @DeleteMapping("/{idSitePost}")
    public ResponseEntity<Void> delete(@PathVariable int idSitePost) {
        sitePostService.deleteSitePost(idSitePost);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
//        try {
//            String savedFilename = fileStorageService.saveFiles(file);
//            String cvFileName = "uploads/" + savedFilename;
//            Path cvPath = Paths.get(cvFileName);
//            return ResponseEntity.ok("Fichier sauvegardé : " + cvFileName);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur d'enregistrement.");
//        }
//    }
}
