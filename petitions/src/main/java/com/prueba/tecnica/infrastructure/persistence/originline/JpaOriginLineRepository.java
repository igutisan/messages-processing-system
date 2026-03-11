package com.prueba.tecnica.infrastructure.persistence.originline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaOriginLineRepository extends JpaRepository<OriginLineEntity, UUID> {

    boolean existsByPhoneNumber(String phoneNumber);
}
