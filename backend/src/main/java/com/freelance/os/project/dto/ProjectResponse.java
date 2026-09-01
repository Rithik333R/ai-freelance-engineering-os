package com.freelance.os.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private UUID clientId;
    private String clientName;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate targetEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
