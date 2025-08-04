package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaAccount;
import com.cercinaai.metaapiservice.entity.MetaAdCreative;
import com.cercinaai.metaapiservice.entity.MetaAdSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaAdCreativeRepository extends JpaRepository<MetaAdCreative, Integer> {
    Optional<MetaAdCreative> findBymetaAdCretaive(String metaAdCretaive);
}
