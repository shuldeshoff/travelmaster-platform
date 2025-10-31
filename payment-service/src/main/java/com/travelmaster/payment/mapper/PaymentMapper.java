package com.travelmaster.payment.mapper;

import com.travelmaster.payment.dto.CreatePaymentRequest;
import com.travelmaster.payment.dto.PaymentResponse;
import com.travelmaster.payment.entity.Payment;
import com.travelmaster.payment.entity.PaymentMethod;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentMethod", source = "paymentMethod", qualifiedByName = "stringToPaymentMethod")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Payment toEntity(CreatePaymentRequest request);

    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    @Mapping(target = "paymentMethod", expression = "java(payment.getPaymentMethod().name())")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

    @Named("stringToPaymentMethod")
    default PaymentMethod stringToPaymentMethod(String paymentMethod) {
        return paymentMethod != null ? PaymentMethod.valueOf(paymentMethod.toUpperCase()) : null;
    }
}

