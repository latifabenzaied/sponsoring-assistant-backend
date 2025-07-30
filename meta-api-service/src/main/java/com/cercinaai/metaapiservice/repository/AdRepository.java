package com.cercinaai.metaapiservice.repository;


import com.cercinaai.metaapiservice.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Integer> {
    Optional<Ad> findByIdSitePost(int idSitePost);

}
