package com.cercinaai.metaapiservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity

@Table(name = "MetaCampaign")
public class MetaCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ObjectiveType objective;
    @Enumerated(EnumType.STRING)
    private MetaStatus status;
    private LocalDateTime createdAt;
    @Column(name = "meta_campaign_id", unique = true, nullable = true)
    private String metaCampaignId;
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetaAdSet> adSets = new ArrayList<>();
}
