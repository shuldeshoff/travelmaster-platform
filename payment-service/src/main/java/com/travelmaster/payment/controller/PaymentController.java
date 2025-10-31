package com.travelmaster.payment.controller;

import com.travelmaster.payment.dto.CreatePaymentRequest;
import com.travelmaster.payment.dto.PaymentResponse;
import com.travelmaster.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "API для управления платежами")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Создать платеж",
            description = "Создает и обрабатывает платеж для бронирования",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        PaymentResponse response = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить платеж по ID",
            description = "Возвращает информацию о платеже",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{reference}")
    @Operation(
            summary = "Получить платеж по номеру",
            description = "Возвращает информацию о платеже по уникальному номеру",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PaymentResponse> getPaymentByReference(@PathVariable String reference) {
        PaymentResponse response = paymentService.getPaymentByReference(reference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(
            summary = "Получить платеж по бронированию",
            description = "Возвращает информацию о платеже для конкретного бронирования",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(@PathVariable Long bookingId) {
        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(
            summary = "Получить мои платежи",
            description = "Возвращает все платежи текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PaymentResponse> response = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @Operation(
            summary = "Вернуть платеж",
            description = "Возвращает средства по платежу",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        PaymentResponse response = paymentService.refundPayment(id, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retry")
    @Operation(
            summary = "Повторить платеж",
            description = "Повторная попытка обработки неудавшегося платежа",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PaymentResponse> retryPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.retryPayment(id);
        return ResponseEntity.ok(response);
    }
}

