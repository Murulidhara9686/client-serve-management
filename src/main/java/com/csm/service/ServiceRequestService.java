package com.csm.service;

import com.csm.model.ServiceRequest;
import com.csm.model.TaskStatus;
import com.csm.model.User;
import com.csm.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository repo;

    // =========================================================
    // SAVE REQUEST
    // =========================================================

    @Transactional
    public ServiceRequest save(ServiceRequest req) {

        return repo.save(req);
    }

    // =========================================================
    // FIND REQUEST BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Optional<ServiceRequest> findById(Long id) {

        return repo.findById(id);
    }

    // =========================================================
    // FIND ALL REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ServiceRequest> findAll() {

        return repo.findByOrderByCreatedAtDesc();
    }

    // =========================================================
    // FIND CUSTOMER REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ServiceRequest> findByCustomer(User customer) {

        return repo.findByCustomer(customer);
    }

    // =========================================================
    // FIND ASSISTANT REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ServiceRequest> findByAssistant(User assistant) {

        return repo.findByAssignedAssistant(assistant);
    }

    // =========================================================
    // FIND EMPLOYEE REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ServiceRequest> findByEmployee(User employee) {

        return repo.findByAssignedEmployee(employee);
    }

    // =========================================================
    // FIND MANAGER REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ServiceRequest> findByManager(User manager) {

        return repo.findByAssignedManager(manager);
    }

    // =========================================================
    // FIND REQUESTS BY STATUS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ServiceRequest> getRequestsByStatus(TaskStatus status) {

        return repo.findByStatus(status);
    }

    // =========================================================
    // COUNT REQUESTS BY STATUS
    // =========================================================

    @Transactional(readOnly = true)
    public long countByStatus(TaskStatus status) {

        return repo.countByStatus(status);
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @Transactional
    public void approveRequest(Long requestId,
                               User manager) {

        ServiceRequest request = repo.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        request.setStatus(TaskStatus.APPROVED);

        request.setReviewedBy(manager);

        request.setReviewedAt(LocalDateTime.now());

        repo.save(request);
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @Transactional
    public void rejectRequest(Long requestId,
                              String reason,
                              User manager) {

        ServiceRequest request = repo.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        request.setStatus(TaskStatus.REJECTED);

        request.setRejectionReason(reason);

        request.setReviewedBy(manager);

        request.setReviewedAt(LocalDateTime.now());

        repo.save(request);
    }

    // =========================================================
    // DELETE REQUEST
    // =========================================================

    @Transactional
    public void deleteById(Long id) {

        repo.deleteById(id);
    }
}