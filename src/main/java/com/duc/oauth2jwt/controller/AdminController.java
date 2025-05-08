package com.duc.oauth2jwt.controller;

import com.duc.oauth2jwt.dto.UserDto;
import com.duc.oauth2jwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{id}/give-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> giveAdminRole(@PathVariable Long id) {
        return ResponseEntity.ok(userService.giveAdminRole(id));
    }

    @PutMapping("/users/{id}/remove-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> removeAdminRole(@PathVariable Long id) {
        return ResponseEntity.ok(userService.removeAdminRole(id));
    }
}