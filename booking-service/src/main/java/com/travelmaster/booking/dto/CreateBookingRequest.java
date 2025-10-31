package com.travelmaster.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Trip ID обязателен")
    private Long tripId;

    @NotEmpty(message = "Необходим минимум 1 пассажир")
    @Valid
    private List<PassengerRequest> passengers;

    private String specialRequests;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerRequest {
        
        @NotNull(message = "Имя обязательно")
        private String firstName;
        
        @NotNull(message = "Фамилия обязательна")
        private String lastName;
        
        private String middleName;
        
        @NotNull(message = "Дата рождения обязательна")
        private String dateOfBirth; // yyyy-MM-dd format
        
        private String passportNumber;
        
        private String passportCountry;
        
        private String passportExpiry; // yyyy-MM-dd format
        
        private String gender; // MALE, FEMALE, OTHER
        
        private String email;
        
        private String phoneNumber;
    }
}

