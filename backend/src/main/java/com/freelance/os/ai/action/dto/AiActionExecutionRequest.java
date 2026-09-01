package com.freelance.os.ai.action.dto;

import com.freelance.os.ai.action.enums.AiActionType;
import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.project.dto.ProjectRequest;
import com.freelance.os.task.dto.TaskRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiActionExecutionRequest {

    @NotNull(message = "Action type is required")
    private AiActionType actionType;

    private ClientRequest clientPayload;
    private ProjectRequest projectPayload;
    private TaskRequest taskPayload;
    private UUID projectId;

    private boolean confirmed;
}
