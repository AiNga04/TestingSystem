package org.vti.jamie.com.project_spring_boot.mapper;

import org.springframework.stereotype.Component;
import org.vti.jamie.com.project_spring_boot.dto.request.PositionRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.PositionResponse;
import org.vti.jamie.com.project_spring_boot.entity.Position;

@Component
public class PositionMapper {

    public Position toEntity(PositionRequest request) {
        return new Position(request.positionName());
    }

    public PositionResponse toResponse(Position entity) {
        return new PositionResponse(
                entity.getId(),
                entity.getName(),
                entity.getDeletedAt()
        );
    }

    public void updateEntity(
            PositionRequest request,
            Position entity
    ) {
        entity.setName(
                request.positionName()
        );
    }
}
