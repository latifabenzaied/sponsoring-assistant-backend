package com.cercinaai.metaapiservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    private String name;//(titleeee)

    @Enumerated(EnumType.STRING)
    private ObjectiveType objective;

    @Enumerated(EnumType.STRING)
    private MetaStatus status;

    private LocalDateTime createdAt;

    @Column(name = "meta_campaign_id", unique = true , nullable = true)
    private String metaCampaignId;
}
