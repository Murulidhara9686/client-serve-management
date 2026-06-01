package com.csm.repository;

import com.csm.model.ServiceRequest;
import com.csm.model.TaskStatus;
import com.csm.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {

    // =========================================================
    // CUSTOMER REQUESTS
    // =========================================================
    List<ServiceRequest> findByCustomer(User customer);

    // =========================================================
    // ASSISTANT MANAGER REQUESTS
    // =========================================================
    List<ServiceRequest> findByAssignedAssistant(User assistant);

    // =========================================================
    // EMPLOYEE REQUESTS
    // =========================================================
    List<ServiceRequest> findByAssignedEmployee(User employee);

    // =========================================================
    // MANAGER REQUESTS
    // =========================================================
    List<ServiceRequest> findByAssignedManager(User manager);

    // =========================================================
    // STATUS FILTERING
    // =========================================================
    List<ServiceRequest> findByStatus(TaskStatus status);

    // =========================================================
    // STATUS COUNT
    // =========================================================
    long countByStatus(TaskStatus status);

    // =========================================================
    // SORTING
    // =========================================================
    List<ServiceRequest> findByOrderByCreatedAtDesc();
}