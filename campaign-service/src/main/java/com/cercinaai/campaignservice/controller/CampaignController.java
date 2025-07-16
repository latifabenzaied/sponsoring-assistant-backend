package com.cercinaai.campaignservice.controller;

import com.cercinaai.campaignservice.entity.Campaign;
import com.cercinaai.campaignservice.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService  campaignService;

    @PostMapping("/{idAd}")
    public ResponseEntity<?> addAd(@RequestBody Campaign campaign, @PathVariable int idAd ) {
        return ResponseEntity.ok(campaignService.addCampaign(campaign, idAd));
    }

    @GetMapping("")
    public ResponseEntity<List<Campaign>> getAll(
    ) {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }
}
