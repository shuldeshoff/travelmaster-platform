package com.travelmaster.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerResponse {

    private Long id;
    
    private String firstName;
    
    private String lastName;
    
    private String middleName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private String passportNumber;
    
    private String passportCountry;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate passportExpiry;
    
    private String gender;
    
    private String email;
    
    private String phoneNumber;
    
    private Integer age;
}

