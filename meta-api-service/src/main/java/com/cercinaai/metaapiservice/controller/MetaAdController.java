package com.cercinaai.metaapiservice.controller;


import com.cercinaai.metaapiservice.entity.*;
import com.cercinaai.metaapiservice.service.MetaAdCreativeService;
import com.cercinaai.metaapiservice.service.MetaAdService;
import com.cercinaai.metaapiservice.service.MetaTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/metaAd")
@CrossOrigin({"http://localhost:4201", "http://localhost:4200"})
@RequiredArgsConstructor
public class MetaAdController {

    private final MetaAdService metaAdService;
    private final MetaAdCreativeService metaAdCreative;
    private final MetaTokenService tokenService;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetaAd> create(@RequestBody MetaAdRequest request) {
        try {
            System.out.println(request);
            MetaAd createdAd = metaAdService.createFromLocalImage(request);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }




   /* @GetMapping("/{creativeId}/image-url")
    public Mono<ResponseEntity<String>> getImageUrl(@PathVariable String creativeId) {
        String accessToken = tokenService.getAccessToken();

        return metaAdCreative.getCreativeImageUrl(creativeId, accessToken)
                .map(url -> url != null
                        ? ResponseEntity.ok(url)
                        : ResponseEntity.<String>notFound().build()
                );

    }*/


    @GetMapping("/adSet/{adSetId}")
    public ResponseEntity<List<MetaAd>> getAdsByAdSet(@PathVariable Long adSetId) {
        try {
            List<MetaAd> ads = metaAdService.getAdsByAdSet(adSetId);
            return ResponseEntity.ok(ads);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("")
    public ResponseEntity<List<MetaAd>> getAll(
    ) {
        return ResponseEntity.ok(metaAdService.getAllMetaAd());
    }




  /*  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) {
        String imageHash = metaAdService.uploadImage(file);
        return ResponseEntity.ok(imageHash);
    }*/

}
