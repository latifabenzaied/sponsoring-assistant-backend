package com.cercinaai.metaapiservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    private MetaStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String targetingJson;
    private Long bidAmount;
    private String bidStrategy;
    private String bidConstraints;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = true)
    private MetaCampaign campaign;
    @OneToMany(mappedBy = "adSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetaAd> ads = new ArrayList<>();

}
