package com.hydrospark.hydrospark.configuration;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) // Catch all exceptions
    public String handleAllExceptions(Exception ex, Model model) {
        // Add the error message to the model
        model.addAttribute("error", "An unexpected error occurred: " + ex.getMessage());
        return "unauthorized"; // Return the error view (error.html)
    }

    // Handle specific exceptions, if needed
    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointerException(NullPointerException ex, Model model) {
        model.addAttribute("error", "A Null Pointer Exception occurred.");
        return "unauthorized";
    }
}
