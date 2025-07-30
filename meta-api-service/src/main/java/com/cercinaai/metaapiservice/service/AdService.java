package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.Ad;
import com.cercinaai.metaapiservice.entity.SitePostDto;
import com.cercinaai.metaapiservice.repository.AdRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {
    private final AdRepository adRepository;
    private final SitePostClient sitePostClient;


    public Ad addAd(Ad ad, int idSitePost) {
        SitePostDto sitePost = sitePostClient.getSitePostById(idSitePost);
        if (sitePost == null) {
            throw new RuntimeException("SitePost ID invalide ou inexistant");
        }
        ad.setIdSitePost(sitePost.getIdSitePost());
        return adRepository.save(ad);
    }



    public Ad updateAd(int id, Ad updatedAd) {
        Ad existingAd = adRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ad not found with id: " + id));

        existingAd.setMetaCampaignId(updatedAd.getMetaCampaignId());
        existingAd.setMetaAdSetId(updatedAd.getMetaAdSetId());
        existingAd.setMetaAdId(updatedAd.getMetaAdId());

        return adRepository.save(existingAd);
    }



    public List<Ad> getAllAds() {
        try {
            return adRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }


}
