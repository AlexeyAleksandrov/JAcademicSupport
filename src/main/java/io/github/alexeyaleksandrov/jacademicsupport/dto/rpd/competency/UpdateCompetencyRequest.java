package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.competency;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCompetencyRequest {
    @NotBlank(message = "Number is required")
    private String number;
    
    @NotBlank(message = "Description is required")
    private String description;
}
