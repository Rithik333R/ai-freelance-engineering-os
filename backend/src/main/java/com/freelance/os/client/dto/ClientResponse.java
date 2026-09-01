package com.freelance.os.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    private UUID id;
    private String companyName;
    private String contactEmail;
    private String phone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
