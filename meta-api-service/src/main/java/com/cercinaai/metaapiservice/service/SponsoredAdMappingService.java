package com.cercinaai.metaapiservice.service;

import com.cercinaai.metaapiservice.entity.SponsoredAdMapping;
import com.cercinaai.metaapiservice.repository.SponsoredAdMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SponsoredAdMappingService {
    private final SponsoredAdMappingRepository repository;


    // Étape 1 : Création avec siteAdId uniquement
    public SponsoredAdMapping createMapping(int siteAdId) {
        SponsoredAdMapping mapping = new SponsoredAdMapping();
        mapping.setSiteAdId(siteAdId);
        return repository.save(mapping);
    }

    // Étape 2 : Mise à jour avec metaAdId
    public SponsoredAdMapping updateWithMetaAdId(int siteAdId, String metaAdId) {
        SponsoredAdMapping mapping = repository.findBySiteAdId(siteAdId)
                .orElseThrow(() -> new RuntimeException("Mapping not found for siteAdId " + siteAdId));
        mapping.setMetaAdId(metaAdId);
        return repository.save(mapping);
    }

    public int getSiteAdIdByMetaAdId(String metaAdId) {
        return repository.findByMetaAdId(metaAdId)
                .map(SponsoredAdMapping::getSiteAdId)
                .orElseThrow(() -> new RuntimeException("Aucune annonce liée trouvée pour metaAdId = " + metaAdId));
    }
}
