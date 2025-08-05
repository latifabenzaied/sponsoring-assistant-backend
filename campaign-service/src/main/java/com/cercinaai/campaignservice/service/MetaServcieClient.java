package com.cercinaai.campaignservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "meta-api-service", url = "http://localhost:9093")
public interface MetaServcieClient {
    @PostMapping("/api/v1/SponsoredAdMapping")
    ResponseEntity<Void> createSponsoredMapping(@RequestParam("siteAdId") int siteAdId);
}
