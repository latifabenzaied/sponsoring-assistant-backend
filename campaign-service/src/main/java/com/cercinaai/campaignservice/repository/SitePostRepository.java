package com.cercinaai.campaignservice.repository;

import com.cercinaai.campaignservice.entity.SitePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SitePostRepository extends JpaRepository<SitePost, Integer> {

}

