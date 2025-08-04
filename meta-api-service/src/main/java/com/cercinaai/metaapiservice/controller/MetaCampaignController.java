package com.cercinaai.metaapiservice.controller;


import com.cercinaai.metaapiservice.entity.Ad;
import com.cercinaai.metaapiservice.entity.Campaign;
import com.cercinaai.metaapiservice.entity.MetaCampaign;
import com.cercinaai.metaapiservice.service.AdService;
import com.cercinaai.metaapiservice.service.MetaCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metaCampaign")
@CrossOrigin({"http://localhost:4201", "http://localhost:4200"})
@RequiredArgsConstructor
public class MetaCampaignController {

    private final MetaCampaignService metaCampaignService;

    @PostMapping
    public ResponseEntity<MetaCampaign> create(@RequestBody MetaCampaign request) {
        MetaCampaign created = metaCampaignService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<MetaCampaign>> getAll(
    ) {
        return ResponseEntity.ok(metaCampaignService.getAllCampaigns());
    }
}
