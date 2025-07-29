package com.cercinaai.metaapiservice.controller;


import com.cercinaai.metaapiservice.entity.Ad;
import com.cercinaai.metaapiservice.service.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    @PostMapping("/{idPost}")
    public ResponseEntity<?> addAd(@RequestBody Ad ad, @PathVariable int idPost ) {
        return ResponseEntity.ok(adService.addAd(ad,idPost));
    }

    @GetMapping("")
    public ResponseEntity<List<Ad>> getAll(
    ) {
        return ResponseEntity.ok(adService.getAllAds());
    }

}
