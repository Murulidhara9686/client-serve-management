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

import java.util.List;

@Controller
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final UserService userService;
    private final ServiceRequestService requestService;
    private final NotificationService notifService;
    private final CommentService commentService;

    // =========================================================
    // GET CURRENT USER
    // =========================================================

    private User getCurrentUser(UserDetails ud) {

        return userService
                .findByEmail(ud.getUsername())
                .orElseThrow();
    }

    // =========================================================
    // ASSISTANT DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal UserDetails ud,
            Model model,
            @RequestParam(required = false)
            String success) {

        User user = getCurrentUser(ud);

        List<ServiceRequest> tasks =
                requestService.findByAssistant(user);

        List<User> employees =
                userService.findByRole(Role.EMPLOYEE);

        long unread =
                notifService.countUnread(user);

        model.addAttribute("user", user);

        model.addAttribute("tasks", tasks);

        model.addAttribute("employees", employees);

        model.addAttribute(
                "notifications",
                notifService.getAllForUser(user)
        );

        model.addAttribute(
                "unreadCount",
                unread
        );

        model.addAttribute(
                "total",
                (long) tasks.size()
        );

        model.addAttribute(
                "inProgress",
                tasks.stream()
                        .filter(t ->
                                t.getStatus()
                                        == TaskStatus.IN_PROGRESS)
                        .count()
        );

        model.addAttribute(
                "completed",
                tasks.stream()
                        .filter(t ->
                                t.getStatus()
                                        == TaskStatus.COMPLETED)
                        .count()
        );

        model.addAttribute(
                "unassigned",
                tasks.stream()
                        .filter(t ->
                                t.getAssignedEmployee() == null)
                        .count()
        );

        if (success != null) {

            model.addAttribute(
                    "successMsg",
                    success
            );
        }

        return "assistant/dashboard";
    }

    // =========================================================
    // ASSIGN TASK TO EMPLOYEE
    // =========================================================

    @PostMapping("/task/assign/{id}")
    public String assignToEmployee(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            RedirectAttributes ra) {

        requestService.findById(id).ifPresent(req -> {

            userService.findById(employeeId).ifPresent(emp -> {

                req.setAssignedEmployee(emp);

                // Update task status
                req.setStatus(TaskStatus.IN_PROGRESS);

                requestService.save(req);

                // =============================================
                // SEND NOTIFICATION
                // =============================================

                notifService.send(
                        emp,

                        "📌 You have been assigned task: "
                                + req.getTitle(),

                        "SUCCESS",

                        "/employee/dashboard"
                );
            });
        });

        ra.addAttribute(
                "success",
                "Task assigned successfully!"
        );

        return "redirect:/assistant/dashboard";
    }

    // =========================================================
    // REASSIGN TASK
    // =========================================================

    @PostMapping("/task/reassign/{id}")
    public String reassignEmployee(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            RedirectAttributes ra) {

        requestService.findById(id).ifPresent(req -> {

            userService.findById(employeeId).ifPresent(emp -> {

                req.setAssignedEmployee(emp);

                requestService.save(req);

                // =============================================
                // SEND NOTIFICATION
                // =============================================

                notifService.send(
                        emp,

                        "🔄 Task reassigned to you: "
                                + req.getTitle(),

                        "WARNING",

                        "/employee/dashboard"
                );
            });
        });

        ra.addAttribute(
                "success",
                "Task reassigned successfully!"
        );

        return "redirect:/assistant/dashboard";
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    @PostMapping("/task/comment/{id}")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        requestService.findById(id).ifPresent(r -> {

            if (!content.isBlank()) {

                commentService.add(
                        r,
                        user,
                        content
                );

                // =============================================
                // NOTIFY EMPLOYEE
                // =============================================

                if (r.getAssignedEmployee() != null) {

                    notifService.send(
                            r.getAssignedEmployee(),

                            "💬 Assistant commented on "
                                    + r.getTitle(),

                            "INFO",

                            "/employee/dashboard"
                    );
                }

                // =============================================
                // NOTIFY CUSTOMER
                // =============================================

                if (r.getCustomer() != null) {

                    notifService.send(
                            r.getCustomer(),

                            "💬 Assistant added a comment on "
                                    + r.getTitle(),

                            "INFO",

                            "/customer/dashboard"
                    );
                }
            }
        });

        ra.addAttribute(
                "success",
                "Comment added successfully."
        );

        return "redirect:/assistant/dashboard";
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

        return "redirect:/assistant/dashboard";
    }
}