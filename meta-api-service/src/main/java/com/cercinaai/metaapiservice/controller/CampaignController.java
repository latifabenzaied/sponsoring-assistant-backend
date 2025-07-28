package com.cercinaai.metaapiservice.controller;

import com.cercinaai.metaapiservice.entity.Campaign;
import com.cercinaai.metaapiservice.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

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
