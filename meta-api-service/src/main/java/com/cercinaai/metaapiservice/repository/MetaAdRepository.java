package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaAd;
import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.entity.MetaCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaAdRepository extends JpaRepository<MetaAd, Integer> {
    List<MetaAd> findByAdSet_Id(Long adSetId);
}
