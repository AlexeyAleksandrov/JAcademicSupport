package io.github.alexeyaleksandrov.jacademicsupport.dto.foresight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForesightDto {
    private Long workSkillId;
    private String sourceName;
    private String sourceUrl;
    private Long canonicalId;
    private BigDecimal confidence;
    private String direction = "POSITIVE";
    private String professionCode;
    private String domain;
    private String techFamily;
    private LocalDate forecastDate;
}
