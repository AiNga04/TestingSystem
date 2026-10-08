package org.vti.jamie.com.project_spring_boot;

import org.junit.jupiter.api.*;
import org.vti.jamie.com.project_spring_boot.entity.Position;
import org.vti.jamie.com.project_spring_boot.enums.PositionName;
import org.vti.jamie.com.project_spring_boot.dto.request.PositionRequest;
import org.vti.jamie.com.project_spring_boot.exception.*;
import org.vti.jamie.com.project_spring_boot.mapper.PositionMapper;
import org.vti.jamie.com.project_spring_boot.repository.PositionRepository;
import org.vti.jamie.com.project_spring_boot.service.impl.PositionServiceImpl;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PositionServiceTests {
    PositionRepository repository;
    PositionServiceImpl service;
    @BeforeEach void setup() {
        repository = mock(PositionRepository.class);
        service = new PositionServiceImpl(repository, new PositionMapper());
    }

    @Test void createsWhenNameAvailable() {
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            Position entity = invocation.getArgument(0);
            entity.setId((short) 5);
            return entity;
        });
        var result = service.create(new PositionRequest(PositionName.DEV));
        assertEquals(PositionName.DEV, result.positionName());
        assertEquals((short) 5, result.positionId());
        assertNull(result.deletedAt());
    }

    @Test void deletesWithoutRemovingRowAndRestoresSameId() {
        Position position = new Position(PositionName.DEV);
        position.setId((short) 5);
        when(repository.findByIdForUpdate((short) 5)).thenReturn(Optional.of(position));
        when(repository.saveAndFlush(position)).thenReturn(position);
        service.delete((short) 5);
        assertNotNull(position.getDeletedAt());
        verify(repository, never()).delete(any(Position.class));
        assertThrows(ResourceNotFoundException.class, () -> service.delete((short) 5));
        var result = service.restore((short) 5);
        assertNull(position.getDeletedAt());
        assertEquals((short) 5, result.positionId());
    }

    @Test void duplicateUpdateIsRejected() {
        Position position = new Position(PositionName.DEV);
        position.setId((short) 5);
        when(repository.findByIdForUpdate((short) 5)).thenReturn(Optional.of(position));
        when(repository.existsByNameAndIdNot(PositionName.PM, (short) 5)).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> service.update((short) 5, new PositionRequest(PositionName.PM)));
        assertEquals(PositionName.DEV, position.getName());
    }

    @Test void unknownRestoreIsNotFound() {
        when(repository.findByIdForUpdate((short) 5)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.restore((short) 5));
    }
}
