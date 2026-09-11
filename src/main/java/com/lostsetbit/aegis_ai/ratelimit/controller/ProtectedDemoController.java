package com.lostsetbit.aegis_ai.ratelimit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class ProtectedDemoController {

    @GetMapping("/products")
    public ResponseEntity<Map<String, String>> getProducts() {
        return ResponseEntity.ok(Map.of("status", "success", "data", "Sample Products Payload"));
    }

    @GetMapping("/unprotected")
    public ResponseEntity<Map<String, String>> getUnprotected() {
        return ResponseEntity.ok(Map.of("status", "success", "data", "No Rule Configured"));
    }
}