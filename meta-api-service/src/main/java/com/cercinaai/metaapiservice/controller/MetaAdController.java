package com.cercinaai.metaapiservice.controller;


import com.cercinaai.metaapiservice.entity.MetaAd;
import com.cercinaai.metaapiservice.service.MetaAdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@Slf4j
@RestController
@RequestMapping("/metaAd")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class MetaAdController {

    private final MetaAdService metaAdService;


    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MetaAd> create(
            @RequestPart("metaAd") String metaAdJson,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            MetaAd metaAd = mapper.readValue(metaAdJson, MetaAd.class); // désérialise manuellement

            System.out.println("HELLO"); // ce message doit maintenant s’afficher

            MetaAd createdAd = metaAdService.create(metaAd, file);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }




    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) {
        String imageHash = metaAdService.uploadImage(file);
        return ResponseEntity.ok(imageHash);
    }

}
