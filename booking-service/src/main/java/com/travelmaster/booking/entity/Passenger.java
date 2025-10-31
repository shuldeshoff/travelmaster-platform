package com.travelmaster.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "passengers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "passport_number", length = 50)
    private String passportNumber;

    @Column(name = "passport_country", length = 3)
    private String passportCountry; // ISO 3166-1 alpha-3

    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
}

