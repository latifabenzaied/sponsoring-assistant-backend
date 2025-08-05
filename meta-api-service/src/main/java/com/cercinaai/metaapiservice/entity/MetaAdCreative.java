package com.cercinaai.metaapiservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "meta_creative")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAdCreative {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    private String imageHash;
    private String link;
    @Column(length = 10000)
    private String message;
    @Column(length = 20000)
    private String fullSpecJson;
    @Column(name = "metaAdCretaiveId", unique = true, nullable = true)
    private String metaAdCretaive;
    @JsonIgnore
    @OneToMany(mappedBy = "adCreative", cascade = CascadeType.ALL)
    private List<MetaAd> ads = new ArrayList<>();
}
