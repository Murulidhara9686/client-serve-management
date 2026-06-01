package com.csm.controller;

import com.csm.model.*;
import com.csm.service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.List;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

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
    // EMPLOYEE DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal UserDetails ud,
            Model model,
            @RequestParam(required = false)
            String success) {

        User user = getCurrentUser(ud);

        List<ServiceRequest> tasks =
                requestService.findByEmployee(user);

        long unread =
                notifService.countUnread(user);

        model.addAttribute("user", user);

        model.addAttribute("tasks", tasks);

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
                "statuses",
                new TaskStatus[]{
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.COMPLETED
                }
        );

        if (success != null) {

            model.addAttribute(
                    "successMsg",
                    success
            );
        }

        return "employee/dashboard";
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    @PostMapping("/task/update/{id}")
    public String updateTask(

            @PathVariable Long id,

            @RequestParam String employeeUpdate,

            @RequestParam String status,

            @RequestParam(required = false)
            String completionSummary,

            @AuthenticationPrincipal UserDetails ud,

            RedirectAttributes ra) {

        User user = getCurrentUser(ud);

        requestService.findById(id).ifPresent(req -> {

            if (req.getAssignedEmployee() != null
                    &&
                    req.getAssignedEmployee()
                            .getId()
                            .equals(user.getId())) {

                boolean wasCompleted =
                        req.getStatus()
                                == TaskStatus.COMPLETED;

                // =============================================
                // UPDATE TEXT
                // =============================================

                req.setEmployeeUpdate(

                        employeeUpdate != null
                                ? employeeUpdate.trim()
                                : ""
                );

                // =============================================
                // UPDATE STATUS
                // =============================================

                TaskStatus newStatus;

                try {

                    newStatus =
                            TaskStatus.valueOf(status);

                } catch (Exception e) {

                    newStatus =
                            TaskStatus.IN_PROGRESS;
                }

                req.setStatus(newStatus);

                // =============================================
                // COMPLETION SUMMARY
                // =============================================

                if (completionSummary != null
                        &&
                        !completionSummary.isBlank()) {

                    req.setCompletionSummary(
                            completionSummary.trim()
                    );
                }

                requestService.save(req);

                // =============================================
                // NOTIFY CUSTOMER
                // =============================================

                if (!wasCompleted
                        &&
                        newStatus
                                == TaskStatus.COMPLETED) {

                    notifService.send(

                            req.getCustomer(),

                            "✅ Your request "
                                    + req.getTitle()
                                    + " has been completed! "
                                    + "Please review the work.",

                            "SUCCESS",

                            "/customer/dashboard"
                    );

                    // =========================================
                    // NOTIFY ASSISTANT
                    // =========================================

                    if (req.getAssignedAssistant() != null) {

                        notifService.send(

                                req.getAssignedAssistant(),

                                "✅ Task "
                                        + req.getTitle()
                                        + " marked completed by "
                                        + user.getName(),

                                "SUCCESS",

                                "/assistant/dashboard"
                        );
                    }
                }
            }
        });

        ra.addAttribute(
                "success",
                "Task updated successfully!"
        );

        return "redirect:/employee/dashboard";
    }

    // =========================================================
    // UPLOAD FINAL WORK
    // =========================================================

    @PostMapping("/upload-work/{id}")
    public String uploadWork(

            @PathVariable Long id,

            @RequestParam MultipartFile workFile,

            RedirectAttributes ra) {

        try {

            ServiceRequest req =
                    requestService.findById(id)
                            .orElseThrow();

            // =============================================
            // ORIGINAL FILE NAME
            // =============================================

            String originalName =
                    workFile.getOriginalFilename();

            // =============================================
            // UNIQUE FILE NAME
            // =============================================

            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + originalName;

            // =============================================
            // UPLOAD DIRECTORY
            // =============================================

            Path uploadPath =
                    Paths.get(
                            "uploads/completed-work/"
                    );

            Files.createDirectories(uploadPath);

            // =============================================
            // FINAL FILE PATH
            // =============================================

            Path filePath =
                    uploadPath.resolve(fileName);

            // =============================================
            // SAVE FILE
            // =============================================

            Files.copy(
                    workFile.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // =============================================
            // SAVE DB VALUES
            // =============================================

            req.setCompletedWorkName(
                    originalName
            );

            req.setCompletedWorkPath(
                    filePath.toString()
            );

            // =============================================
            // AUTO COMPLETE
            // =============================================

            req.setStatus(
                    TaskStatus.COMPLETED
            );

            requestService.save(req);

            // =============================================
            // NOTIFY CUSTOMER
            // =============================================

            notifService.send(

                    req.getCustomer(),

                    "✅ Final work uploaded for: "
                            + req.getTitle(),

                    "SUCCESS",

                    "/customer/dashboard"
            );

            // =============================================
            // SUCCESS
            // =============================================

            ra.addAttribute(

                    "success",

                    "Final work uploaded successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            ra.addAttribute(

                    "error",

                    "Upload failed!"
            );
        }

        return "redirect:/employee/dashboard";
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

            if (r.getAssignedEmployee() != null
                    &&
                    r.getAssignedEmployee()
                            .getId()
                            .equals(user.getId())
                    &&
                    !content.isBlank()) {

                commentService.add(
                        r,
                        user,
                        content
                );

                notifService.send(

                        r.getCustomer(),

                        user.getName()
                                + " commented on your request "
                                + r.getTitle(),

                        "INFO",

                        "/customer/dashboard"
                );
            }
        });

        ra.addAttribute(
                "success",
                "Comment added."
        );

        return "redirect:/employee/dashboard";
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

        return "redirect:/employee/dashboard";
    }
}