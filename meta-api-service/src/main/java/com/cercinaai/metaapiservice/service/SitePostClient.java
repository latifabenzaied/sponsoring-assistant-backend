package com.cercinaai.metaapiservice.service;


import com.cercinaai.metaapiservice.entity.SitePostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Service
@FeignClient(name = "campaign-service", url = "http://localhost:9091") // ou IP/DNS réel du service
public interface SitePostClient {

    @GetMapping("/api/v1/sitePost/{idPost}")
    SitePostDto getSitePostById(@PathVariable("idPost") int idPost);
}
