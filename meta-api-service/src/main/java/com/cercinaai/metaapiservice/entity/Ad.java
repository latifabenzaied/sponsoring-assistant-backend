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

public class  Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
  /*  private String description;
    private String photoUrl;
    private String destinitionUrl;
    @Enumerated(EnumType.STRING)
    private AdFormatType format;
    @Enumerated(EnumType.STRING)
    private PlatformType platform;
    private LocalDateTime createdAt;*/

    // ID de la campagne Meta
    @Column(nullable = true)
    private String metaCampaignId;

    // ID de l'ad set Meta
    @Column(nullable = true)
    private String metaAdSetId;

    // ID de l'ad Meta
    @Column(nullable = true)
    private String metaAdId;

    @Column(name = "site_post_id", nullable = false)
    private int idSitePost;
}
