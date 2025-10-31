package com.travelmaster.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 50, message = "Имя не может быть длиннее 50 символов")
    private String firstName;

    @Size(max = 50, message = "Фамилия не может быть длиннее 50 символов")
    private String lastName;

    @Size(max = 20, message = "Номер телефона не может быть длиннее 20 символов")
    private String phoneNumber;
}

