package com.prueba.tecnica.infrastructure.persistence.originline;

import com.prueba.tecnica.domain.repository.OriginLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OriginLineRepositoryAdapter implements OriginLineRepository {

    private final JpaOriginLineRepository jpaRepository;

    @Override
    public boolean existPhoneNumber(String phoneNumber) {
        return jpaRepository.existsByPhoneNumber(phoneNumber);
    }
}
