package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.entity.MetaCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaAdSetRepository extends JpaRepository<MetaAdSet, Integer> {
}
