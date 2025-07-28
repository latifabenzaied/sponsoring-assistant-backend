package com.cercinaai.metaapiservice.service;

import com.cercinaai.campaignservice.entity.SitePost;
import com.cercinaai.campaignservice.repository.SitePostRepository;
import com.cercinaai.metaapiservice.entity.Ad;
import com.cercinaai.metaapiservice.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {
    private final AdRepository adRepository;
    private final SitePostRepository sitePostRepository;

    public Ad addAd(Ad ad, int idSitePost) {
        SitePost Post = sitePostRepository.findById(idSitePost)
                .orElseThrow(() -> new RuntimeException("SitePost with id " + idSitePost + " not found"));

        ad.setIdSitePost(Post);
        ad.setCreatedAt(LocalDateTime.now());
        return adRepository.save(ad);
    }


    public List<Ad> getAllAds() {
        try {
            return adRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }


}
