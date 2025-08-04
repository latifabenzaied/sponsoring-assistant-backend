package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaCampaignRepository extends JpaRepository<MetaCampaign, Integer> {

    Optional<MetaCampaign> findByMetaCampaignId(String metaCampaignId);
}
