package com.company.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String status;
}