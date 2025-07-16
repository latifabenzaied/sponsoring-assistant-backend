package com.cercinaai.campaignservice.service;

import com.cercinaai.campaignservice.entity.Ad;
import com.cercinaai.campaignservice.entity.Campaign;
import com.cercinaai.campaignservice.entity.SitePost;
import com.cercinaai.campaignservice.repository.AdRepository;
import com.cercinaai.campaignservice.repository.CampaignRepository;
import com.cercinaai.campaignservice.repository.SitePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CampaignService {
    private final AdRepository adRepository;
    private final CampaignRepository campaignRepository;

    public Campaign addCampaign(Campaign campaign, int idAd) {
        Ad ad = adRepository.findById(idAd)
                .orElseThrow(() -> new RuntimeException("SitePost with id " + idAd + " not found"));
        campaign.setAd(ad);
        campaign.setCreatedAt(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    public List<Campaign> getAllCampaigns() {
        try {
            return campaignRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }

    public void deleteCampaign(int idCampaign) {
        campaignRepository.deleteById(idCampaign);
    }
}
