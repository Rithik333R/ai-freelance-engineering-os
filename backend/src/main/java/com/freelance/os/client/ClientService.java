package com.freelance.os.client;

import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.common.dto.PagedResponse;
import com.freelance.os.common.exception.ResourceNotFoundException;
import com.freelance.os.user.User;
import com.freelance.os.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClientResponse createClient(ClientRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Client client = Client.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .contactEmail(request.getContactEmail())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .build();

        Client savedClient = clientRepository.save(client);
        return mapToResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getClientsForUser(UUID userId) {
        return clientRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ClientResponse> getClientsForUserPaginated(UUID userId, Pageable pageable) {
        int cappedSize = Math.min(pageable.getPageSize(), 100);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), cappedSize, pageable.getSort());
        Page<ClientResponse> page = clientRepository.findByUserId(userId, cappedPageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID clientId, UUID userId) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        return mapToResponse(client);
    }

    @Transactional
    public ClientResponse updateClient(UUID clientId, ClientRequest request, UUID userId) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        client.setCompanyName(request.getCompanyName());
        client.setContactEmail(request.getContactEmail());
        client.setPhone(request.getPhone());
        client.setNotes(request.getNotes());

        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }

    @Transactional
    public void deleteClient(UUID clientId, UUID userId) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        clientRepository.delete(client);
    }

    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .companyName(client.getCompanyName())
                .contactEmail(client.getContactEmail())
                .phone(client.getPhone())
                .notes(client.getNotes())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
