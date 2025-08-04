package com.cercinaai.metaapiservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class MetaAdSetRequest {
    private String name;
    private Long dailyBudget;
    private String billingEvent;
    private String optimizationGoal;
    private MetaStatus status;
    private String bidStrategy;
    private Long bidAmount;
    private String targetingJson;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String metaCampaignId; // 👈 juste l’ID ici, pas l’objet
}
