package com.cercinaai.metaapiservice.controller;


import com.cercinaai.metaapiservice.entity.MetaAd;
import com.cercinaai.metaapiservice.service.MetaAdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metaAd")
@RequiredArgsConstructor
public class MetaAdController {

    private final MetaAdService metaAdService;

    @PostMapping
    public ResponseEntity<MetaAd> create(@RequestBody MetaAd metaAd) {
        MetaAd createdAd = metaAdService.create(metaAd);
        return ResponseEntity.ok(createdAd);
    }
}
