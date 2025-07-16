package com.cercinaai.campaignservice.repository;


import com.cercinaai.campaignservice.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
}
