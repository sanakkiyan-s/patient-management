package com.sana.authservice.Controller;

import com.sana.authservice.Model.AppUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUsers() {
        return "This endpoint is restricted to ADMIN users only";
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public String getCurrentUser() {
        return "This endpoint is accessible to USER role";
    }
}
