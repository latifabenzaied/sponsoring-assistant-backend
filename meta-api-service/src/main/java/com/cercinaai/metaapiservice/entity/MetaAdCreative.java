package com.cercinaai.metaapiservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Lob;
import org.springframework.data.annotation.Id;

public class MetaAdCreative {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    @Column(name = "page_id")
    private String pageId;

    @Column(name = "instagram_actor_id")
    private String instagramActorId;

    private String imageHash;
    private String link;

    @Lob
    private String message; // long text

    @Lob
    private String fullSpecJson;

}
