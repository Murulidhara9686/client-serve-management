package com.csm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // BASIC REQUEST DETAILS
    // =========================================================

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING_APPROVAL;

    // =========================================================
    // CUSTOMER INPUT
    // =========================================================

    // Budget estimation
    private String budgetRange;

    // Expected deadline
    private LocalDate expectedDeadline;

    // =========================================================
    // CUSTOMER DOCUMENTS
    // =========================================================

    // Original uploaded document name
    private String customerDocumentName;

    // Stored document path
    @Column(length = 1000)
    private String customerDocumentPath;

    // =========================================================
    // EMPLOYEE FINAL SUBMISSION
    // =========================================================

    // Final work file name
    private String completedWorkName;

    // Final work file path
    @Column(length = 1000)
    private String completedWorkPath;

    // =========================================================
    // USER RELATIONSHIPS
    // =========================================================

    // Customer who created request
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private User customer;

    // Manager reviewing request
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_manager_id")
    private User assignedManager;

    // Assistant manager
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_assistant_id")
    private User assignedAssistant;

    // Employee handling task
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_employee_id")
    private User assignedEmployee;

    // =========================================================
    // MANAGER REVIEW SECTION
    // =========================================================

    // Manager note
    @Column(length = 1000)
    private String managerNote;

    // Rejection reason
    @Column(length = 1000)
    private String rejectionReason;

    // Manager who reviewed
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    // Review timestamp
    private LocalDateTime reviewedAt;

    // =========================================================
    // EMPLOYEE WORK SECTION
    // =========================================================

    // Employee progress updates
    @Column(length = 2000)
    private String employeeUpdate;

    // Completion summary
    @Column(length = 2000)
    private String completionSummary;

    // =========================================================
    // CUSTOMER FEEDBACK
    // =========================================================

    // Customer rating (1-5)
    private Integer customerRating;

    @Column(length = 1000)
    private String customerFeedback;

    // =========================================================
    // TIMESTAMPS
    // =========================================================

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    // =========================================================
    // AUTO TIMESTAMP HANDLING
    // =========================================================

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();

        updatedAt = LocalDateTime.now();

        // Default status
        if (status == null) {

            status = TaskStatus.PENDING_APPROVAL;
        }

        // Default priority
        if (priority == null) {

            priority = Priority.MEDIUM;
        }
    }

    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();

        // Auto completion timestamp
        if (status == TaskStatus.COMPLETED
                && completedAt == null) {

            completedAt = LocalDateTime.now();
        }
    }
}