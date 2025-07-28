package com.cercinaai.metaapiservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "meta_account")
public class MetaAccount {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private String accessToken;

    private Instant tokenExpiresAt;

    private Instant updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.from(LocalDateTime.now());
    }
}
