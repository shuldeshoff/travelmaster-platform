package com.travelmaster.payment.service;

import com.travelmaster.common.exception.BusinessException;
import com.travelmaster.common.exception.EntityNotFoundException;
import com.travelmaster.payment.dto.CreatePaymentRequest;
import com.travelmaster.payment.dto.PaymentResponse;
import com.travelmaster.payment.entity.Payment;
import com.travelmaster.payment.entity.PaymentMethod;
import com.travelmaster.payment.entity.PaymentStatus;
import com.travelmaster.payment.event.*;
import com.travelmaster.payment.gateway.PaymentGateway;
import com.travelmaster.payment.gateway.PaymentGatewayRequest;
import com.travelmaster.payment.gateway.PaymentGatewayResponse;
import com.travelmaster.payment.mapper.PaymentMapper;
import com.travelmaster.payment.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Создание и обработка платежа.
     */
    @Transactional
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "createPaymentFallback")
    @Retry(name = "paymentGateway")
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        log.info("Creating payment for user {} and booking {}", userId, request.getBookingId());

        // 1. Создаем запись платежа
        Payment payment = paymentMapper.toEntity(request);
        payment.setUserId(userId);
        payment.setPaymentReference(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.PENDING);
        
        payment = paymentRepository.save(payment);

        // 2. Публикуем событие создания
        eventPublisher.publishPaymentCreated(buildPaymentCreatedEvent(payment));

        // 3. Обрабатываем платеж через шлюз
        payment.markAsProcessing();
        paymentRepository.save(payment);

        PaymentGatewayRequest gatewayRequest = PaymentGatewayRequest.builder()
                .paymentReference(payment.getPaymentReference())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .cardNumber(request.getCardNumber())
                .cardHolderName(request.getCardHolderName())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .cvv(request.getCvv())
                .ipAddress(payment.getIpAddress())
                .userAgent(payment.getUserAgent())
                .build();

        PaymentGatewayResponse gatewayResponse = paymentGateway.processPayment(gatewayRequest);

        // 4. Обновляем статус платежа
        if (gatewayResponse.isSuccess()) {
            payment.markAsSuccess(gatewayResponse.getTransactionId());
            payment.setGatewayName(paymentGateway.getGatewayName());
            payment.setCardLastFour(gatewayResponse.getCardLastFour());
            payment.setCardBrand(gatewayResponse.getCardBrand());
            
            payment = paymentRepository.save(payment);
            
            log.info("Payment processed successfully: {}", payment.getPaymentReference());
            
            eventPublisher.publishPaymentProcessed(buildPaymentProcessedEvent(payment));
        } else {
            payment.markAsFailed(gatewayResponse.getErrorMessage());
            payment = paymentRepository.save(payment);
            
            log.error("Payment failed: {}", gatewayResponse.getErrorMessage());
            
            eventPublisher.publishPaymentFailed(buildPaymentFailedEvent(payment));
            
            throw new BusinessException("Ошибка при обработке платежа: " + gatewayResponse.getErrorMessage());
        }

        return paymentMapper.toResponse(payment);
    }

    /**
     * Fallback метод при ошибке платежного шлюза.
     */
    private PaymentResponse createPaymentFallback(Long userId, CreatePaymentRequest request, Exception e) {
        log.error("Payment gateway unavailable, using fallback. Error: {}", e.getMessage());
        
        // Создаем платеж в статусе FAILED
        Payment payment = paymentMapper.toEntity(request);
        payment.setUserId(userId);
        payment.setPaymentReference(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.FAILED);
        payment.markAsFailed("Payment gateway unavailable: " + e.getMessage());
        
        payment = paymentRepository.save(payment);
        
        eventPublisher.publishPaymentFailed(buildPaymentFailedEvent(payment));
        
        return paymentMapper.toResponse(payment);
    }

    /**
     * Возврат платежа.
     */
    @Transactional
    @CircuitBreaker(name = "paymentGateway")
    @Retry(name = "paymentGateway")
    public PaymentResponse refundPayment(Long paymentId, String reason) {
        log.info("Refunding payment {}", paymentId);

        Payment payment = findPaymentById(paymentId);

        if (!payment.canBeRefunded()) {
            throw new BusinessException("Payment cannot be refunded. Status: " + payment.getStatus());
        }

        // Возврат через шлюз
        PaymentGatewayResponse response = paymentGateway.refundPayment(
                payment.getGatewayTransactionId(), 
                payment.getAmount()
        );

        if (!response.isSuccess()) {
            throw new BusinessException("Refund failed: " + response.getErrorMessage());
        }

        // Обновляем платеж
        payment.markAsRefunded(payment.getAmount(), reason);
        payment = paymentRepository.save(payment);

        log.info("Payment {} refunded successfully", paymentId);

        eventPublisher.publishPaymentRefunded(buildPaymentRefundedEvent(payment));

        return paymentMapper.toResponse(payment);
    }

    /**
     * Повторная попытка обработки платежа.
     */
    @Transactional
    public PaymentResponse retryPayment(Long paymentId) {
        log.info("Retrying payment {}", paymentId);

        Payment payment = findPaymentById(paymentId);

        if (!payment.canBeRetried()) {
            throw new BusinessException("Payment cannot be retried. Status: " + payment.getStatus() + ", retries: " + payment.getRetryCount());
        }

        // TODO: Реализовать retry logic

        return paymentMapper.toResponse(payment);
    }

    /**
     * Получение платежа по ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = findPaymentById(id);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Получение платежа по reference.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String reference) {
        Payment payment = paymentRepository.findByPaymentReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + reference));
        return paymentMapper.toResponse(payment);
    }

    /**
     * Получение платежа по бронированию.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for booking: " + bookingId));
        return paymentMapper.toResponse(payment);
    }

    /**
     * Получение всех платежей пользователя.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return paymentMapper.toResponseList(payments.getContent());
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
    }

    private PaymentCreatedEvent buildPaymentCreatedEvent(Payment payment) {
        return PaymentCreatedEvent.builder()
                .paymentId(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PaymentProcessedEvent buildPaymentProcessedEvent(Payment payment) {
        return PaymentProcessedEvent.builder()
                .paymentId(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .processedAt(payment.getProcessedAt())
                .build();
    }

    private PaymentFailedEvent buildPaymentFailedEvent(Payment payment) {
        return PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .failureReason(payment.getFailureReason())
                .retryCount(payment.getRetryCount())
                .failedAt(payment.getFailedAt())
                .build();
    }

    private PaymentRefundedEvent buildPaymentRefundedEvent(Payment payment) {
        return PaymentRefundedEvent.builder()
                .paymentId(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .refundAmount(payment.getRefundAmount())
                .refundReason(payment.getRefundReason())
                .refundedAt(payment.getRefundedAt())
                .build();
    }
}

