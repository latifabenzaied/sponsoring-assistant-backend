package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaAdSet;
import com.cercinaai.metaapiservice.entity.MetaCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaAdSetRepository extends JpaRepository<MetaAdSet, Integer> {
    Optional<MetaAdSet> findByMetaAdSetId(String metaAdSetId);

}
