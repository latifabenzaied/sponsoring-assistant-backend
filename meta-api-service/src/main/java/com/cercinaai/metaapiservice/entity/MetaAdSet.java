package com.cercinaai.metaapiservice.entity;

import jakarta.persistence.Entity;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class MetaAdSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metaAdSetId;

    private String name;
    private Long dailyBudget;
    private String billingEvent;

    private String optimizationGoal;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Lob
    private String targetingJson;

    private Long bidAmount;
    private String bidStrategy; // e.g., "LOWEST_COST_WITHOUT_CAP"// e.g., 100 (si BID_CAP)
    private String bidConstraints; // sous forme JSON si ROAS
    @Column(name = "meta_campaign_id", nullable = false)
    private String metaCampaignId; // Juste l’ID de MetaCampaign, pas d’objet
}
