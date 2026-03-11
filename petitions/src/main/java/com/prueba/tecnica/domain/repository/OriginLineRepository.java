package com.prueba.tecnica.domain.repository;

/**
 * Repository contract for OriginLine persistence.
 * Implemented by the infrastructure persistence adapter.
 */
public interface OriginLineRepository {
    boolean existPhoneNumber(String phoneNumber);
}
