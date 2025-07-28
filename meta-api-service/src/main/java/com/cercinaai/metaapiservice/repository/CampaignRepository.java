package com.cercinaai.metaapiservice.repository;



import com.cercinaai.metaapiservice.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
}
