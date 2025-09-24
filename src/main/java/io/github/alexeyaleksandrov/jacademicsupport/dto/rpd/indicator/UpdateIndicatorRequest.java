package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateIndicatorRequest {
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Indicator know is required")
    private String indicatorKnow;
    
    @NotBlank(message = "Indicator able is required")
    private String indicatorAble;
    
    @NotBlank(message = "Indicator possess is required")
    private String indicatorPossess;
}
