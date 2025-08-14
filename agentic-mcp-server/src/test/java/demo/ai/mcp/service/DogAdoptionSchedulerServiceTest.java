package demo.ai.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DogAdoptionSchedulerServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    private DogAdoptionSchedulerService service;

    @BeforeEach
    void setUp() {
        service = new DogAdoptionSchedulerService(objectMapper);
    }

    @Test
    void scheduleDogAdoptionAppointment_ShouldReturnFutureDate() throws Exception {
        // Given
        int dogId = 123;
        String dogName = "Buddy";
        String expectedJson = "\"2024-03-20T10:00:00Z\"";
        when(objectMapper.writeValueAsString(any(Instant.class))).thenReturn(expectedJson);

        // When
        String result = service.scheduleDogAdoptionAppointment(dogId, dogName);

        // Then
        assertThat(result).isEqualTo(expectedJson);
        verify(objectMapper).writeValueAsString(any(Instant.class));
    }

    @Test
    void scheduleDogAdoptionAppointment_ShouldHandleDifferentDogs() throws Exception {
        // Given
        String expectedJson = "\"2024-03-20T10:00:00Z\"";
        when(objectMapper.writeValueAsString(any(Instant.class))).thenReturn(expectedJson);

        // When & Then
        assertThat(service.scheduleDogAdoptionAppointment(1, "Max")).isEqualTo(expectedJson);
        assertThat(service.scheduleDogAdoptionAppointment(2, "Luna")).isEqualTo(expectedJson);
    }
} 