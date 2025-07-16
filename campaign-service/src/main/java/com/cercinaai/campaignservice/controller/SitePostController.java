package com.cercinaai.campaignservice.controller;

import com.cercinaai.campaignservice.entity.SitePost;
import com.cercinaai.campaignservice.service.SitePostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sitePost")
@RequiredArgsConstructor
public class SitePostController {
    private final SitePostService sitePostService;

    @PostMapping("")
    public ResponseEntity<?> addSitePost(@RequestBody SitePost sitePost) {
        SitePost savedSitePost=sitePostService.addSitePost(sitePost);
        return ResponseEntity.ok(savedSitePost);
    }

    @GetMapping("")
    public ResponseEntity<List<SitePost>> getAll(
    ) {
        return ResponseEntity.ok(sitePostService.getAllSitePosts());
    }


    @PostMapping("/{idPost}")
    public ResponseEntity<?> updateSitePost(@PathVariable int idPost, @RequestBody SitePost sitePost){
        return ResponseEntity.ok(sitePostService.updateSitePost(idPost,sitePost));
    }


    @DeleteMapping("/{idSitePost}")
    public ResponseEntity<Void> delete(@PathVariable int idSitePost) {
        sitePostService.deleteSitePost(idSitePost);
        return ResponseEntity.noContent().build();
    }
}
