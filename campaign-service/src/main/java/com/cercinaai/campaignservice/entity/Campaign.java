package com.cercinaai.campaignservice.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder

@Entity
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String objective;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double budget;

    @Enumerated(EnumType.STRING)
    private StatusType status;

    private LocalDateTime createdAt;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ad_id")
    private Ad ad;

    @OneToOne(mappedBy = "campaign", cascade = CascadeType.ALL)
    private Performance performance;
}

