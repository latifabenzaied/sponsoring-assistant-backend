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

public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String description;
    private String photoUrl;
    private String destinitionUrl;
    @Enumerated(EnumType.STRING)
    private AdFormatType format;
    @Enumerated(EnumType.STRING)
    private PlatformType platform;
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_post_id")
    private SitePost idSitePost;
}
