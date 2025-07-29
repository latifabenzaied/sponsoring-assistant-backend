package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaCampaignRepository extends JpaRepository<MetaCampaign, Integer> {
}
