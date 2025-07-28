package com.cercinaai.metaapiservice.repository;

import com.cercinaai.metaapiservice.entity.MetaAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaAccountRepository extends JpaRepository<MetaAccount, Integer> {
}
