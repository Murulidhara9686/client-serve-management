package com.csm.controller;

import com.csm.model.ServiceRequest;
import com.csm.service.ServiceRequestService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final ServiceRequestService requestService;

    // =========================================================
    // DOWNLOAD CUSTOMER DOCUMENT
    // =========================================================

    @GetMapping("/customer-document/{id}")
    public ResponseEntity<Resource> downloadCustomerDocument(
            @PathVariable Long id) throws Exception {

        ServiceRequest req =
                requestService.findById(id)
                        .orElseThrow();

        if (req.getCustomerDocumentPath() == null) {

            throw new RuntimeException(
                    "Document not found"
            );
        }

        Path path =
                Paths.get(
                        req.getCustomerDocumentPath()
                );

        Resource resource =
                new UrlResource(
                        path.toUri()
                );

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=\""
                                + req.getCustomerDocumentName()
                                + "\""
                )

                .body(resource);
    }

    // =========================================================
    // DOWNLOAD COMPLETED WORK
    // =========================================================

    @GetMapping("/completed-work/{id}")
    public ResponseEntity<Resource> downloadCompletedWork(
            @PathVariable Long id) throws Exception {

        ServiceRequest req =
                requestService.findById(id)
                        .orElseThrow();

        if (req.getCompletedWorkPath() == null) {

            throw new RuntimeException(
                    "Completed work not found"
            );
        }

        Path path =
                Paths.get(
                        req.getCompletedWorkPath()
                );

        Resource resource =
                new UrlResource(
                        path.toUri()
                );

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=\""
                                + req.getCompletedWorkName()
                                + "\""
                )

                .body(resource);
    }
}