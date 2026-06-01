package com.csm.controller;

import com.csm.model.Role;
import com.csm.model.TaskStatus;
import com.csm.model.ServiceRequest;
import com.csm.service.ServiceRequestService;
import com.csm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StatsController {
    private final ServiceRequestService requestService;
    private final UserService userService;

    @GetMapping("/stats")
    public String stats(Model model) {
        List<ServiceRequest> all = requestService.findAll();
        long total      = all.size();
        long pending    = all.stream().filter(r -> r.getStatus() == TaskStatus.PENDING_APPROVAL).count();
        long inProgress = all.stream().filter(r -> r.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completed  = all.stream().filter(r -> r.getStatus() == TaskStatus.COMPLETED).count();
        long rejected   = all.stream().filter(r -> r.getStatus() == TaskStatus.REJECTED).count();

        // Average rating from completed+rated requests
        double avgRating = all.stream()
            .filter(r -> r.getCustomerRating() != null && r.getCustomerRating() > 0)
            .mapToInt(ServiceRequest::getCustomerRating)
            .average().orElse(0.0);
        long ratedCount = all.stream()
            .filter(r -> r.getCustomerRating() != null && r.getCustomerRating() > 0).count();

        model.addAttribute("total",      total);
        model.addAttribute("pending",    pending);
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("completed",  completed);
        model.addAttribute("rejected",   rejected);
        model.addAttribute("avgRating",  String.format("%.1f", avgRating));
        model.addAttribute("ratedCount", ratedCount);
        model.addAttribute("managers",   (long) userService.findByRole(Role.MANAGER).size());
        model.addAttribute("assistants", (long) userService.findByRole(Role.ASSISTANT_MANAGER).size());
        model.addAttribute("employees",  (long) userService.findByRole(Role.EMPLOYEE).size());
        model.addAttribute("customers",  (long) userService.findByRole(Role.CUSTOMER).size());
        return "stats/dashboard";
    }
}
