package com.csm.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Do NOT handle Spring Security exceptions — let Security handle them
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex) {
        throw ex;  // re-throw so Spring Security handles the redirect
    }

    @ExceptionHandler(AuthenticationException.class)
    public String handleAuth(AuthenticationException ex) {
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAll(Exception ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorTitle",   "Something went wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        model.addAttribute("errorDetail",  ex.getMessage());
        model.addAttribute("errorPath",    request.getRequestURI());
        return "error/error";
    }
}
