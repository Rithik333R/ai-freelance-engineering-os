package com.freelance.os.task.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title must not exceed 255 characters")
    private String title;

    private String description;

    @NotBlank(message = "Task status is required")
    private String status;

    @NotBlank(message = "Task priority is required")
    private String priority;

    @Min(value = 0, message = "Estimated hours must be a positive number")
    private Integer estimatedHours;

    private LocalDate dueDate;
}
