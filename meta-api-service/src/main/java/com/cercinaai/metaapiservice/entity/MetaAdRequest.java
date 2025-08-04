package com.cercinaai.metaapiservice.entity;

import lombok.Data;

@Data
public class MetaAdRequest {
    private String name;
    private MetaStatus status;
    private String metaAdSetId;
    private String link;
    private String message;
    private String imageFileName;
}
