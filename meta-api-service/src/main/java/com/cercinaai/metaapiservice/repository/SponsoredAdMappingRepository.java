package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaCampaign;
import com.cercinaai.metaapiservice.entity.SponsoredAdMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SponsoredAdMappingRepository extends JpaRepository<SponsoredAdMapping, Integer> {

    Optional<SponsoredAdMapping> findBySiteAdId(int siteAdId);
    Optional<SponsoredAdMapping> findByMetaAdId(String metaAdId);
}
