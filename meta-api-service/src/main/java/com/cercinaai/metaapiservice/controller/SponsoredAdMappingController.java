package com.cercinaai.metaapiservice.controller;


import com.cercinaai.metaapiservice.entity.SponsoredAdMapping;
import com.cercinaai.metaapiservice.service.SponsoredAdMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/SponsoredAdMapping")
@CrossOrigin({"http://localhost:4201", "http://localhost:4200"})
@RequiredArgsConstructor
public class SponsoredAdMappingController {

    private final SponsoredAdMappingService service;

    @PostMapping
    public ResponseEntity<SponsoredAdMapping> create(@RequestParam int siteAdId) {
        SponsoredAdMapping mapping = service.createMapping(siteAdId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping);
    }

    // Étape 2 : PATCH /sponsored-mappings/{siteAdId}
    @PatchMapping("/{siteAdId}")
    public ResponseEntity<SponsoredAdMapping> update(@PathVariable int siteAdId, @RequestParam String metaAdId) {
        SponsoredAdMapping updated = service.updateWithMetaAdId(siteAdId, metaAdId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/sitepost")
    public ResponseEntity<Integer> getSiteAdId(@RequestParam String metaAdId) {
        int siteAdId = service.getSiteAdIdByMetaAdId(metaAdId);
        return ResponseEntity.ok(siteAdId);
    }
}
