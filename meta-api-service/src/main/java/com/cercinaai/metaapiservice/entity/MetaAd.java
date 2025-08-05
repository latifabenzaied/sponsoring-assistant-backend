package com.cercinaai.metaapiservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meta_ad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String creativeId;

    @Enumerated(EnumType.STRING)
    private MetaStatus status;
    private String metaAdId;
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_set_id", nullable = false)
    private MetaAdSet adSet;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_creative_id", nullable = false)
    private MetaAdCreative adCreative;
    @Column(length = 10000)
    private String imageUrl;
    @Column(name = "listing_id")
    private Integer listingId; // correspond à idSitePost dans SitePostDto


}