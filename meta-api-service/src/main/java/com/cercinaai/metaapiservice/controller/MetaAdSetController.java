package com.cercinaai.metaapiservice.controller;

import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.service.MetaAdSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metaAdSet")
@RequiredArgsConstructor
public class MetaAdSetController {
    private final MetaAdSetService metaAdSetService;

    @PostMapping
    public ResponseEntity<MetaAdSet> create(@RequestBody MetaAdSet adSet) {
        MetaAdSet created = metaAdSetService.create(adSet);
        return ResponseEntity.ok(created);
    }
}
