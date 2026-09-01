package com.freelance.os.ai.action.dto;

import com.freelance.os.ai.action.enums.AiActionStatus;
import com.freelance.os.ai.action.enums.AiActionType;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.project.dto.ProjectResponse;
import com.freelance.os.task.dto.TaskResponse;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiActionExecutionResult {

    private AiActionType actionType;
    private AiActionStatus status;
    private String message;
    private UUID createdResourceId;
    private ClientResponse clientResponse;
    private ProjectResponse projectResponse;
    private TaskResponse taskResponse;
}
