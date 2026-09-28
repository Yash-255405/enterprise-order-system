package com.company.payment.controller;

import com.company.payment.dto.PaymentRequest;
import com.company.payment.dto.PaymentResponse;
import com.company.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}