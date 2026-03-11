package com.prueba.tecnica.infrastructure.persistence.originline;

import com.prueba.tecnica.domain.model.OriginLine;

public final class OriginLinePersistenceMapper {

    private OriginLinePersistenceMapper() {
    
    }

    public static OriginLineEntity toEntity(OriginLine originLine) {
        OriginLineEntity entity = new OriginLineEntity();
        entity.setId(originLine.getId());
        entity.setPhoneNumber(originLine.getPhoneNumber());
        return entity;
    }

    public static OriginLine toDomain(OriginLineEntity entity) {
        return new OriginLine(
                entity.getId(),
                entity.getPhoneNumber());
    }
}
