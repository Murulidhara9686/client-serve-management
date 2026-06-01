package com.csm.controller;

import com.csm.model.*;
import com.csm.service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final UserService userService;
    private final ServiceRequestService requestService;
    private final NotificationService notifService;
    private final CommentService commentService;

    // =========================================================
    // GET CURRENT LOGGED-IN USER
    // =========================================================

    private User getCurrentUser(UserDetails ud) {

        return userService
                .findByEmail(ud.getUsername())
                .orElseThrow();
    }

    // =========================================================
    // MANAGER DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud,
                            Model model,
                            @RequestParam(required = false) String success) {

        User user = getCurrentUser(ud);

        List<ServiceRequest> all = requestService.findAll();

        List<User> assistants =
                userService.findByRole(Role.ASSISTANT_MANAGER);

        long unread = notifService.countUnread(user);

        model.addAttribute("user", user);

        model.addAttribute("requests", all);

        model.addAttribute("assistants", assistants);


        model.addAttribute(
                "notifications",
                notifService.getAllForUser(user)
        );

        model.addAttribute("unreadCount", unread);

        // =====================================================
        // DASHBOARD COUNTS
        // =====================================================

        model.addAttribute("total", (long) all.size());

        model.addAttribute(
                "pendingApproval",
                all.stream()
                        .filter(r ->
                                r.getStatus() ==
                                        TaskStatus.PENDING_APPROVAL)
                        .count()
        );

        model.addAttribute(
                "approved",
                all.stream()
                        .filter(r ->
                                r.getStatus() ==
                                        TaskStatus.APPROVED)
                        .count()
        );

        model.addAttribute(
                "inProgress",
                all.stream()
                        .filter(r ->
                                r.getStatus() ==
                                        TaskStatus.IN_PROGRESS)
                        .count()
        );

        model.addAttribute(
                "completed",
                all.stream()
                        .filter(r ->
                                r.getStatus() ==
                                        TaskStatus.COMPLETED)
                        .count()
        );

        model.addAttribute(
                "rejected",
                all.stream()
                        .filter(r ->
                                r.getStatus() ==
                                        TaskStatus.REJECTED)
                        .count()
        );

        if (success != null) {

            model.addAttribute(
                    "successMsg",
                    success
            );
        }

        return "manager/dashboard";
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @PostMapping("/request/approve/{id}")
    public String approveRequest(@PathVariable Long id,
                                 @RequestParam Long assistantId,
                                 @RequestParam(required = false)
                                 String managerNote,
                                 @AuthenticationPrincipal
                                 UserDetails ud,
                                 RedirectAttributes ra) {

        User manager = getCurrentUser(ud);

        requestService.findById(id).ifPresent(req -> {

            userService.findById(assistantId)
                    .ifPresent(assistant -> {

                        // Assign users
                        req.setAssignedAssistant(assistant);

                        req.setAssignedManager(manager);

                        // Update status
                        req.setStatus(TaskStatus.APPROVED);

                        // Review tracking
                        req.setReviewedBy(manager);

                        req.setReviewedAt(
                                LocalDateTime.now()
                        );

                        // Manager note
                        req.setManagerNote(
                                managerNote != null
                                        && !managerNote.isBlank()
                                        ? managerNote.trim()
                                        : "Approved by Manager"
                        );

                        requestService.save(req);

                        // Notify assistant
                        notifService.send(
                                assistant,
                                "New request assigned: "
                                        + req.getTitle(),
                                "SUCCESS",
                                "/assistant/dashboard"
                        );

                        // Notify customer
                        notifService.send(
                                req.getCustomer(),
                                "Your request '"
                                        + req.getTitle()
                                        + "' has been approved.",
                                "SUCCESS",
                                "/customer/dashboard"
                        );
                    });
        });

        ra.addAttribute(
                "success",
                "Request approved successfully!"
        );

        return "redirect:/manager/dashboard";
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @PostMapping("/request/reject/{id}")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false)
                                String reason,
                                @AuthenticationPrincipal
                                UserDetails ud,
                                RedirectAttributes ra) {

        User manager = getCurrentUser(ud);

        requestService.findById(id).ifPresent(req -> {

            if (req.getStatus()
                    == TaskStatus.PENDING_APPROVAL) {

                req.setStatus(TaskStatus.REJECTED);

                req.setReviewedBy(manager);

                req.setReviewedAt(
                        LocalDateTime.now()
                );

                req.setRejectionReason(
                        reason != null
                                && !reason.isBlank()
                                ? reason.trim()
                                : "Rejected by Manager"
                );

                requestService.save(req);

                // Notify customer
                notifService.send(
                        req.getCustomer(),
                        "Your request '"
                                + req.getTitle()
                                + "' was rejected. Reason: "
                                + req.getRejectionReason(),
                        "ALERT",
                        "/customer/dashboard"
                );
            }
        });

        ra.addAttribute(
                "success",
                "Request rejected successfully!"
        );

        return "redirect:/manager/dashboard";
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    @PostMapping("/request/comment/{id}")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @AuthenticationPrincipal
                             UserDetails ud,
                             RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        requestService.findById(id).ifPresent(r -> {

            if (!content.isBlank()) {

                commentService.add(r, user, content);

                notifService.send(
                        r.getCustomer(),
                        "Manager commented on request: "
                                + r.getTitle(),
                        "INFO",
                        "/customer/dashboard"
                );
            }
        });

        ra.addAttribute(
                "success",
                "Comment added successfully."
        );

        return "redirect:/manager/dashboard";
    }

    // =========================================================
    // MARK NOTIFICATIONS READ
    // =========================================================

    @PostMapping("/notifications/mark-read")
    public String markRead(
            @AuthenticationPrincipal UserDetails ud) {

        notifService.markAllRead(
                getCurrentUser(ud)
        );

        return "redirect:/manager/dashboard";
    }

    // =========================================================
    // USER MANAGEMENT
    // =========================================================

    @GetMapping("/users")
    public String users(@AuthenticationPrincipal UserDetails ud,
                        Model model) {

        model.addAttribute(
                "user",
                getCurrentUser(ud)
        );

        model.addAttribute(
                "employees",
                userService.findByRole(Role.EMPLOYEE)
        );

        model.addAttribute(
                "assistants",
                userService.findByRole(
                        Role.ASSISTANT_MANAGER
                )
        );

        model.addAttribute(
                "customers",
                userService.findByRole(Role.CUSTOMER)
        );

        return "manager/users";
    }
}