package com.freelance.os.ai.action.service;

import com.freelance.os.ai.action.dto.AiActionProposal;
import com.freelance.os.ai.action.enums.AiActionType;
import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.project.dto.ProjectRequest;
import com.freelance.os.task.dto.TaskRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiActionExtractor {

    public Optional<AiActionProposal> extractActionProposal(String message) {
        if (message == null || message.trim().isEmpty()) {
            return Optional.empty();
        }

        String lower = message.toLowerCase();

        if (lower.contains("create client") || lower.contains("add client") || lower.contains("new client")) {
            return extractClientProposal(message);
        }

        if (lower.contains("create project") || lower.contains("add project") || lower.contains("new project")) {
            return extractProjectProposal(message);
        }

        if (lower.contains("create task") || lower.contains("add task") || lower.contains("new task")) {
            return extractTaskProposal(message);
        }

        return Optional.empty();
    }

    private Optional<AiActionProposal> extractClientProposal(String message) {
        String companyName = extractPattern(message, "(?:name|company|client)\\s+(?:is\\s+|named\\s+|called\\s+)?['\"]?([^'\",.\\n]+)['\"]?", 1);
        if (companyName == null || companyName.isBlank()) {
            companyName = extractPattern(message, "(?:create|add|new)\\s+client\\s+(?:named\\s+|called\\s+)?['\"]?([^'\",.\\n]+)['\"]?", 1);
        }

        if (companyName == null || companyName.isBlank()) {
            return Optional.empty();
        }

        String email = extractPattern(message, "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", 1);
        String phone = extractPattern(message, "(?:phone|tel|mobile)\\s+(?:is\\s+)?['\"]?([+0-9\\s-()]+)['\"]?", 1);

        ClientRequest clientRequest = ClientRequest.builder()
                .companyName(companyName.trim())
                .contactEmail(email != null ? email.trim() : null)
                .phone(phone != null ? phone.trim() : null)
                .notes("Created via AI Assistant")
                .build();

        return Optional.of(AiActionProposal.builder()
                .actionType(AiActionType.CREATE_CLIENT)
                .description("Create Client: " + clientRequest.getCompanyName())
                .clientPayload(clientRequest)
                .confirmationPrompt(String.format("Would you like me to create client '%s'%s?",
                        clientRequest.getCompanyName(),
                        clientRequest.getContactEmail() != null ? " with email " + clientRequest.getContactEmail() : ""))
                .build());
    }

    private Optional<AiActionProposal> extractProjectProposal(String message) {
        String projectName = extractPattern(message, "(?:create|add|new)\\s+project\\s+(?:named\\s+|called\\s+)?['\"]?([^'\",.\\n]+)['\"]?", 1);
        if (projectName == null || projectName.isBlank()) {
            projectName = extractPattern(message, "(?:project\\s+name\\s+is\\s+)?['\"]?([^'\",.\\n]+)['\"]?", 1);
        }

        if (projectName == null || projectName.isBlank()) {
            return Optional.empty();
        }

        String budgetStr = extractPattern(message, "(?:budget|cost)\\s+(?:of\\s+|is\\s+)?\\$?([0-9]+(?:\\.[0-9]{1,2})?)", 1);
        BigDecimal budget = null;
        if (budgetStr != null) {
            try {
                budget = new BigDecimal(budgetStr);
            } catch (Exception ignored) {}
        }

        ProjectRequest projectRequest = ProjectRequest.builder()
                .name(projectName.trim())
                .status("PLANNING")
                .budget(budget)
                .description("Created via AI Assistant")
                .build();

        return Optional.of(AiActionProposal.builder()
                .actionType(AiActionType.CREATE_PROJECT)
                .description("Create Project: " + projectRequest.getName())
                .projectPayload(projectRequest)
                .confirmationPrompt(String.format("Would you like me to create project '%s'%s?",
                        projectRequest.getName(),
                        budget != null ? " with budget $" + budget : ""))
                .build());
    }

    private Optional<AiActionProposal> extractTaskProposal(String message) {
        String taskTitle = extractPattern(message, "(?:create|add|new)\\s+task\\s+(?:named\\s+|called\\s+|titled\\s+)?['\"]?([^'\",.\\n]+)['\"]?", 1);
        if (taskTitle == null || taskTitle.isBlank()) {
            return Optional.empty();
        }

        String projectIdStr = extractPattern(message, "(?:project|for project)\\s+([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})", 1);
        UUID projectId = null;
        if (projectIdStr != null) {
            try {
                projectId = UUID.fromString(projectIdStr);
            } catch (Exception ignored) {}
        }

        TaskRequest taskRequest = TaskRequest.builder()
                .title(taskTitle.trim())
                .status("TODO")
                .priority("MEDIUM")
                .description("Created via AI Assistant")
                .build();

        return Optional.of(AiActionProposal.builder()
                .actionType(AiActionType.CREATE_TASK)
                .description("Create Task: " + taskRequest.getTitle())
                .taskPayload(taskRequest)
                .projectId(projectId)
                .confirmationPrompt(String.format("Would you like me to create task '%s'?", taskRequest.getTitle()))
                .build());
    }

    private String extractPattern(String text, String regex, int group) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(group);
        }
        return null;
    }
}
