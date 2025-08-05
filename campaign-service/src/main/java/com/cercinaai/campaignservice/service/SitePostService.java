package com.cercinaai.campaignservice.service;

import com.cercinaai.campaignservice.entity.SitePost;
import com.cercinaai.campaignservice.repository.SitePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SitePostService {
    private final SitePostRepository sitePostRepository;
    private final MetaServcieClient metaServcieClient;

    public SitePost addSitePost(SitePost sitePost, List<MultipartFile> imageFiles) {
        List<String> imagePaths = new ArrayList<>();

        for (MultipartFile image : imageFiles) {
            if (!image.isEmpty()) {
                try {
                    String path = saveImageToLocal(image);
                    imagePaths.add(path);
                } catch (IOException e) {
                    throw new RuntimeException("Erreur lors de l'enregistrement de l'image", e);
                }
            }
        }

        sitePost.setPhotoUrls(imagePaths);
        sitePost.setPublishedAt(LocalDateTime.now()); // optionnel
        SitePost saved=sitePostRepository.save(sitePost);
        metaServcieClient.createSponsoredMapping(saved.getIdSitePost());

        return saved;
    }
    private String saveImageToLocal(MultipartFile file) throws IOException {
        String uploadsDir = "uploads/";
        String originalName = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalName;

        Path uploadPath = Paths.get(uploadsDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadsDir + fileName;
    }


    public List<SitePost> getAllSitePosts() {
        try {
            return sitePostRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching complaints", e);
        }
    }

    public SitePost getSitePostById(int id ) {
        try {
            return sitePostRepository.findById(id).get();
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
