package com.travelmaster.payment.service;

import com.travelmaster.common.exception.EntityNotFoundException;
import com.travelmaster.payment.dto.PaymentResponse;
import com.travelmaster.payment.entity.Payment;
import com.travelmaster.payment.entity.PaymentMethod;
import com.travelmaster.payment.entity.PaymentStatus;
import com.travelmaster.payment.mapper.PaymentMapper;
import com.travelmaster.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;
    private PaymentResponse testPaymentResponse;

    @BeforeEach
    void setUp() {
        testPayment = Payment.builder()
                .id(1L)
                .paymentReference("PAY-2025-001")
                .bookingId(100L)
                .userId(200L)
                .amount(new BigDecimal("50000.00"))
                .currency("RUB")
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .createdAt(LocalDateTime.now())
                .build();

        testPaymentResponse = PaymentResponse.builder()
                .id(1L)
                .paymentReference("PAY-2025-001")
                .bookingId(100L)
                .userId(200L)
                .amount(new BigDecimal("50000.00"))
                .currency("RUB")
                .status("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should get payment by id successfully")
    void shouldGetPaymentByIdSuccessfully() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment)).thenReturn(testPaymentResponse);

        // When
        PaymentResponse result = paymentService.getPaymentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PAY-2025-001", result.getPaymentReference());
        verify(paymentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when payment not found")
    void shouldThrowExceptionWhenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> paymentService.getPaymentById(999L));
        verify(paymentRepository).findById(999L);
    }

    @Test
    @DisplayName("Should check payment status is PENDING")
    void shouldCheckPaymentStatusIsPending() {
        // Given/When
        boolean isPending = testPayment.getStatus() == PaymentStatus.PENDING;

        // Then
        assertTrue(isPending);
    }

    @Test
    @DisplayName("Should check payment status is SUCCESS")
    void shouldCheckPaymentStatusIsSuccess() {
        // Given
        testPayment.setStatus(PaymentStatus.SUCCESS);

        // When
        boolean isSuccess = testPayment.getStatus() == PaymentStatus.SUCCESS;

        // Then
        assertTrue(isSuccess);
    }

    @Test
    @DisplayName("Should check payment method is CREDIT_CARD")
    void shouldCheckPaymentMethodIsCreditCard() {
        // Given/When
        boolean isCreditCard = testPayment.getPaymentMethod() == PaymentMethod.CREDIT_CARD;

        // Then
        assertTrue(isCreditCard);
    }
}

