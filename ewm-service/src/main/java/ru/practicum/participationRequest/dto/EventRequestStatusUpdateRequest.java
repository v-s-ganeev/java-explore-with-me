package ru.practicum.participationRequest.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.participationRequest.model.ParticipationRequestStatus;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private ParticipationRequestStatus status;
}
