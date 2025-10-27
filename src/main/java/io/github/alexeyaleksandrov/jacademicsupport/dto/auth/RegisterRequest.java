package io.github.alexeyaleksandrov.jacademicsupport.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Имя обязательно для заполнения")
    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна для заполнения")
    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String lastName;

    @Size(max = 100, message = "Отчество не должно превышать 100 символов")
    private String middleName;

    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Некорректный формат email")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    private String email;

    @NotBlank(message = "Пароль обязателен для заполнения")
    @Size(min = 6, max = 100, message = "Пароль должен содержать от 6 до 100 символов")
    private String password;
}
