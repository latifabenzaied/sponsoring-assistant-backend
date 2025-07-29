package com.cercinaai.metaapiservice.entity;

import lombok.Data;

@Data
public class SitePostDto {
    private int idSitePost;
    private String title;
    private String decription;
   /* private String photoUrls;*/
    private String location;
    private Double price;
}
