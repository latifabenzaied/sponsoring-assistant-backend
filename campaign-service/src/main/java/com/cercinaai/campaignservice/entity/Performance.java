package com.cercinaai.campaignservice.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder

@Entity
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer impressions;
    private Integer reach;
    private Integer clicks;
    private Integer actions;
    private Integer engagements;

    private Double ctr;
    private Double cpc;

    private LocalDate date_start;
    private LocalDate datePulledAt;
    private LocalDate date_stop;

    @OneToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
}