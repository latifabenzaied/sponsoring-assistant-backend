package com.cercinaai.metaapiservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meta_ads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String creativeId; // stocké séparément

    @Enumerated(EnumType.STRING)
    private MetaStatus status;

    private String metaAdSetId;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_set_id")
    private MetaAdSet adSet;
}