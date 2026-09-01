package com.freelance.os.client;

import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.common.ApiResponse;
import com.freelance.os.common.dto.PagedResponse;
import com.freelance.os.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody ClientRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ClientResponse response = clientService.createClient(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ClientResponse>>> getClients(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PagedResponse<ClientResponse> clients = clientService.getClientsForUserPaginated(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Clients retrieved successfully", clients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ClientResponse response = clientService.getClientById(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Client retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ClientResponse response = clientService.updateClient(id, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClient(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        clientService.deleteClient(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Client deleted successfully", null));
    }
}
