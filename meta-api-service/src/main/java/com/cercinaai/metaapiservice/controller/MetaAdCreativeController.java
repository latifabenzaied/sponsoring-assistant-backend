package com.cercinaai.metaapiservice.controller;


import com.cercinaai.metaapiservice.entity.Campaign;
import com.cercinaai.metaapiservice.entity.MetaAdCreative;
import com.cercinaai.metaapiservice.entity.MetaAdCreativeDto;
import com.cercinaai.metaapiservice.service.MetaAdCreativeService;
import com.cercinaai.metaapiservice.service.MetaTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/metaAdCreative")
@CrossOrigin({"http://localhost:4201", "http://localhost:4200"})
@RequiredArgsConstructor
public class MetaAdCreativeController {

    private final MetaAdCreativeService metaAdCreative;
    private final MetaTokenService tokenService;


    @GetMapping("{idAdCretive}")
    public ResponseEntity<MetaAdCreativeDto> getAdCreativeDto(@PathVariable String idAdCretive) {
        MetaAdCreative creative = metaAdCreative.getMetaAdCreativeById(idAdCretive);

        MetaAdCreativeDto dto = MetaAdCreativeDto.builder()
                .name(creative.getName())
                .message(creative.getMessage())
                .link(creative.getLink())
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{creativeId}/image")
    public String getCreativeImage(@PathVariable String creativeId) {
        return metaAdCreative.getCreativeImageUrl(creativeId, tokenService.getAccessToken());
    }
}
