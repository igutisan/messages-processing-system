package com.prueba.tecnica.infrastructure.config;

import com.prueba.tecnica.infrastructure.persistence.originline.JpaOriginLineRepository;
import com.prueba.tecnica.infrastructure.persistence.originline.OriginLineEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JpaOriginLineRepository originLineRepository;

    @Override
    public void run(String... args) {
        if (originLineRepository.count() > 0) {
            log.info("OriginLine table already has data, skipping seed.");
            return;
        }

        List<OriginLineEntity> seedLines = List.of(
                OriginLineEntity.builder().phoneNumber("+573111111111").build(),
                OriginLineEntity.builder().phoneNumber("+573122222222").build(),
                OriginLineEntity.builder().phoneNumber("+573133333333").build(),
                OriginLineEntity.builder().phoneNumber("+573144444444").build(),
                OriginLineEntity.builder().phoneNumber("+573155555555").build());

        originLineRepository.saveAll(seedLines);
        log.info("Seeded {}", seedLines.size());
    }
}
