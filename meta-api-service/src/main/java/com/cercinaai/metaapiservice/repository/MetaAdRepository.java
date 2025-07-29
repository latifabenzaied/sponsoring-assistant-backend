package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaAd;
import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.entity.MetaCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaAdRepository extends JpaRepository<MetaAd, Integer> {
}
