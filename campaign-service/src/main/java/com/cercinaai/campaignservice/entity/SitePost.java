package com.cercinaai.campaignservice.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SitePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idSitePost;
    private String title;
    private String description;
    private String propertyType;
    @Enumerated(EnumType.STRING)
    private ListingType type;
    private Double area;
    private Double price;
    private String location;
    @ElementCollection
    private List<String> photoUrls;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;
    @Enumerated(EnumType.STRING)
    private StatusAnnonce status;
    private boolean isSponsored;
    private Integer bedRoomsNb;
    private Integer bathRoomsNb;
    private Boolean furnished;
    private String availability;

//    @OneToMany(mappedBy = "sitePost", cascade = CascadeType.ALL)
//    private List<Campaign> campaigns;
}
