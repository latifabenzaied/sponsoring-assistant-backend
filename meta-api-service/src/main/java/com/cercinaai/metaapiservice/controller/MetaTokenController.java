package com.cercinaai.metaapiservice.controller;

import com.cercinaai.metaapiservice.service.MetaTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta/token")
@RequiredArgsConstructor
public class MetaTokenController {
    private final MetaTokenService metaTokenService;

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshToken() {
        try {
            metaTokenService.refreshAccessTokenIfNeeded();
            return ResponseEntity.ok("✅ Token Meta rafraîchi avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Erreur lors du rafraîchissement : " + e.getMessage());
        }
    }
}
