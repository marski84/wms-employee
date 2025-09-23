package org.localhost.wmsemployee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @GetMapping("/public")
    public ResponseEntity<String> publicMessage() {
        return ResponseEntity.ok("This is a public message.");
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedMessage() {
        return ResponseEntity.ok("This is a protected message. Authentication is required.");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('read:admin-messages')")
    public ResponseEntity<String> adminMessage() {
        return ResponseEntity.ok("This is an admin-only message. Specific permission is required.");
    }
}