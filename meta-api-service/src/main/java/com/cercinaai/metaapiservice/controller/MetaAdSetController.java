package com.cercinaai.metaapiservice.controller;

import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.entity.MetaAdSetRequest;
import com.cercinaai.metaapiservice.entity.MetaCampaign;
import com.cercinaai.metaapiservice.service.MetaAdSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metaAdSet")
@CrossOrigin({"http://localhost:4201", "http://localhost:4200"})
@RequiredArgsConstructor
public class MetaAdSetController {
    private final MetaAdSetService metaAdSetService;

    @PostMapping
    public ResponseEntity<MetaAdSet> create(@RequestBody MetaAdSetRequest adSet) {
        MetaAdSet created = metaAdSetService.create(adSet);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<MetaAdSet>> getAll(
    ) {
        return ResponseEntity.ok(metaAdSetService.getAllCampaigns());
    }
}
