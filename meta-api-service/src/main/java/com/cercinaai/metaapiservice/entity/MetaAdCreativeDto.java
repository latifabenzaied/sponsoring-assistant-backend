package com.cercinaai.metaapiservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaAdCreativeDto {
    private String name;
    private String message;
    private String link;
}