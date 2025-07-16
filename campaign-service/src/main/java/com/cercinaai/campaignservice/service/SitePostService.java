package com.cercinaai.campaignservice.service;

import com.cercinaai.campaignservice.entity.SitePost;
import com.cercinaai.campaignservice.repository.SitePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SitePostService {
    private final SitePostRepository sitePostRepository;

    public SitePost addSitePost(SitePost sitePost) {
       return  sitePostRepository.save(sitePost);
    }

    public List<SitePost> getAllSitePosts() {
        try {
            return sitePostRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }



    public SitePost updateSitePost(int idSitePost,SitePost updatedPost) {
        SitePost existingPost = sitePostRepository.findById(idSitePost)
                .orElseThrow(() -> new RuntimeException("SitePost with id " + idSitePost + " not found"));

        existingPost = SitePost.builder()
                .idSitePost(existingPost.getIdSitePost())
                .title(updatedPost.getTitle())
                .description(updatedPost.getDescription())
                .propertyType(updatedPost.getPropertyType())
                .type(updatedPost.getType())
                .area(updatedPost.getArea())
                .price(updatedPost.getPrice())
                .location(updatedPost.getLocation())
                .photoUrls(updatedPost.getPhotoUrls())
                .publishedAt(updatedPost.getPublishedAt())
                .status(updatedPost.getStatus())
                .isSponsored(updatedPost.isSponsored())
                .bedRoomsNb(updatedPost.getBedRoomsNb())
                .bathRoomsNb(updatedPost.getBathRoomsNb())
                .furnished(updatedPost.getFurnished())
                .availability(updatedPost.getAvailability())
                .build();

        return sitePostRepository.save(existingPost);
    }



    public void deleteSitePost(int idSitePost) {
        sitePostRepository.deleteById(idSitePost);
    }
}
