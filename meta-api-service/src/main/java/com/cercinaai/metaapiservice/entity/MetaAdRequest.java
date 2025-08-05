package com.cercinaai.metaapiservice.entity;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class MetaAdRequest {
    private String name;
    private MetaStatus status;
    private String metaAdSetId;
    private String link;
    private String message;
    private String imageFileName;
    private int siteAdId; // ✅ nouveau champ
}
