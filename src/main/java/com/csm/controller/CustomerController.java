package com.csm.controller;

import com.csm.model.*;
import com.csm.service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

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
    // CUSTOMER DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud,
                            Model model,
                            @RequestParam(required = false)
                            String success,
                            @RequestParam(required = false)
                            String error) {

        User user = getCurrentUser(ud);

        List<ServiceRequest> requests =
                requestService.findByCustomer(user);

        long unread =
                notifService.countUnread(user);

        model.addAttribute("user", user);

        model.addAttribute("requests", requests);

        model.addAttribute(
                "notifications",
                notifService.getAllForUser(user)
        );

        model.addAttribute(
                "unreadCount",
                unread
        );

        model.addAttribute(
                "categories",
                List.of(
                        "Web Development",
                        "Mobile App",
                        "Database",
                        "API Integration",
                        "Cloud Services",
                        "UI/UX Design",
                        "Data Analytics",
                        "DevOps",
                        "Other"
                )
        );

        model.addAttribute(
                "priorities",
                Priority.values()
        );

        model.addAttribute(
                "total",
                (long) requests.size()
        );

        model.addAttribute(
                "pending",
                requests.stream()
                        .filter(r ->
                                r.getStatus()
                                        == TaskStatus.PENDING_APPROVAL)
                        .count()
        );

        model.addAttribute(
                "inProgress",
                requests.stream()
                        .filter(r ->
                                r.getStatus()
                                        == TaskStatus.IN_PROGRESS)
                        .count()
        );

        model.addAttribute(
                "completed",
                requests.stream()
                        .filter(r ->
                                r.getStatus()
                                        == TaskStatus.COMPLETED)
                        .count()
        );

        if (success != null) {

            model.addAttribute(
                    "successMsg",
                    success
            );
        }

        if (error != null) {

            model.addAttribute(
                    "errorMsg",
                    error
            );
        }

        return "customer/dashboard";
    }

    // =========================================================
    // SUBMIT REQUEST
    // =========================================================

    @PostMapping("/request/submit")
    public String submitRequest(

            @RequestParam String title,

            @RequestParam String description,

            @RequestParam String category,

            @RequestParam(defaultValue = "MEDIUM")
            String priority,

            @RequestParam(required = false)
            String budgetRange,

            @RequestParam(required = false)
            String expectedDeadline,

            @RequestParam(required = false)
            MultipartFile document,

            @AuthenticationPrincipal UserDetails ud,

            RedirectAttributes ra) {

        try {

            User user = getCurrentUser(ud);

            ServiceRequest req = new ServiceRequest();

            req.setTitle(title.trim());

            req.setDescription(description.trim());

            req.setCategory(
                    category.isEmpty()
                            ? "Other"
                            : category
            );

            req.setBudgetRange(budgetRange);

            req.setStatus(
                    TaskStatus.PENDING_APPROVAL
            );

            // =============================================
            // DEADLINE
            // =============================================

            if (expectedDeadline != null
                    && !expectedDeadline.isEmpty()) {

                req.setExpectedDeadline(
                        java.time.LocalDate.parse(expectedDeadline)
                );
            }

            // =============================================
            // PRIORITY
            // =============================================

            try {

                req.setPriority(
                        Priority.valueOf(priority)
                );

            } catch (Exception e) {

                req.setPriority(
                        Priority.MEDIUM
                );
            }

            // =============================================
            // CUSTOMER
            // =============================================

            req.setCustomer(user);

            // =============================================
            // FILE UPLOAD
            // =============================================

            if (document != null
                    && !document.isEmpty()) {

                String originalName =
                        document.getOriginalFilename();

                String fileName =
                        System.currentTimeMillis()
                                + "_"
                                + originalName;

                Path uploadPath =
                        Paths.get(
                                "uploads/customer-documents/"
                        );

                Files.createDirectories(uploadPath);

                Path filePath =
                        uploadPath.resolve(fileName);

                Files.copy(
                        document.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                req.setCustomerDocumentName(
                        originalName
                );

                req.setCustomerDocumentPath(
                        filePath.toString()
                );
            }

            // =============================================
            // SAVE REQUEST
            // =============================================

            requestService.save(req);

            // =============================================
            // NOTIFY MANAGERS
            // =============================================

            userService.findByRole(Role.MANAGER)
                    .forEach(manager ->

                            notifService.send(
                                    manager,

                                    "📌 New request from "
                                            + user.getName()
                                            + ": "
                                            + req.getTitle(),

                                    "INFO",

                                    "/manager/dashboard"
                            )
                    );

            ra.addAttribute(
                    "success",
                    "Request submitted successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            ra.addAttribute(
                    "error",
                    "File upload failed!"
            );
        }

        return "redirect:/customer/dashboard";
    }

    // =========================================================
    // EDIT REQUEST
    // =========================================================

    @PostMapping("/request/edit/{id}")
    public String editRequest(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam(defaultValue = "MEDIUM")
            String priority,
            @RequestParam(required = false)
            String budgetRange,
            @RequestParam(required = false)
            String expectedDeadline,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        requestService.findById(id).ifPresent(r -> {

            if (r.getCustomer()
                    .getId()
                    .equals(user.getId())

                    && r.getStatus()
                    == TaskStatus.PENDING_APPROVAL) {

                r.setTitle(title.trim());

                r.setDescription(description.trim());

                r.setCategory(
                        category.isEmpty()
                                ? "Other"
                                : category
                );

                r.setBudgetRange(budgetRange);

                if (expectedDeadline != null
                        && !expectedDeadline.isEmpty()) {

                    try {

                        r.setExpectedDeadline(
                                LocalDate.parse(expectedDeadline)
                        );

                    } catch (Exception ignored) {
                    }

                } else {

                    r.setExpectedDeadline(null);
                }

                try {

                    r.setPriority(
                            Priority.valueOf(priority)
                    );

                } catch (Exception e) {

                    r.setPriority(
                            Priority.MEDIUM
                    );
                }

                requestService.save(r);
            }
        });

        ra.addAttribute(
                "success",
                "Request updated successfully."
        );

        return "redirect:/customer/dashboard";
    }

    // =========================================================
    // CANCEL REQUEST
    // =========================================================

    @PostMapping("/request/cancel/{id}")
    public String cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        requestService.findById(id).ifPresent(r -> {

            if (r.getCustomer()
                    .getId()
                    .equals(user.getId())

                    && r.getStatus()
                    == TaskStatus.PENDING_APPROVAL) {

                requestService.deleteById(id);
            }
        });

        ra.addAttribute(
                "success",
                "Request cancelled successfully."
        );

        return "redirect:/customer/dashboard";
    }

    // =========================================================
    // RATE REQUEST
    // =========================================================

    @PostMapping("/request/rate/{id}")
    public String rateRequest(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")
            int rating,
            @RequestParam(required = false)
            String feedback,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        requestService.findById(id).ifPresent(r -> {

            if (r.getCustomer()
                    .getId()
                    .equals(user.getId())

                    && r.getStatus()
                    == TaskStatus.COMPLETED) {

                if (rating >= 1 && rating <= 5) {

                    r.setCustomerRating(rating);
                }

                if (feedback != null
                        && !feedback.isBlank()) {

                    r.setCustomerFeedback(
                            feedback.trim()
                    );
                }

                requestService.save(r);
            }
        });

        ra.addAttribute(
                "success",
                "Thank you for your feedback!"
        );

        return "redirect:/customer/dashboard";
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    @PostMapping("/request/comment/{id}")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        requestService.findById(id).ifPresent(r -> {

            if (r.getCustomer()
                    .getId()
                    .equals(user.getId())

                    && !content.isBlank()) {

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

                            "💬 New comment from "
                                    + user.getName()
                                    + " on "
                                    + r.getTitle(),

                            "INFO",

                            "/employee/dashboard"
                    );
                }

                // =============================================
                // NOTIFY ASSISTANT
                // =============================================

                if (r.getAssignedAssistant() != null) {

                    notifService.send(
                            r.getAssignedAssistant(),

                            "💬 New comment from "
                                    + user.getName()
                                    + " on "
                                    + r.getTitle(),

                            "INFO",

                            "/assistant/dashboard"
                    );
                }
            }
        });

        ra.addAttribute(
                "success",
                "Comment added successfully."
        );

        return "redirect:/customer/dashboard";
    }

    // =========================================================
    // MARK NOTIFICATIONS READ
    // =========================================================

    @PostMapping("/notifications/mark-read")
    public String markNotificationsRead(
            @AuthenticationPrincipal UserDetails ud) {

        notifService.markAllRead(
                getCurrentUser(ud)
        );

        return "redirect:/customer/dashboard";
    }

    // =========================================================
    // PROFILE PAGE
    // =========================================================

    @GetMapping("/profile")
    public String profilePage(
            @AuthenticationPrincipal UserDetails ud,
            Model model) {

        model.addAttribute(
                "user",
                getCurrentUser(ud)
        );

        return "customer/profile";
    }

    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam(required = false)
            String phone,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        user.setName(name.trim());

        user.setPhone(
                phone != null
                        ? phone.trim()
                        : null
        );

        userService.save(user);

        ra.addAttribute(
                "success",
                "Profile updated successfully."
        );

        return "redirect:/customer/dashboard";
    }
}