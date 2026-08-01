package io.github.alexeyaleksandrov.jacademicsupport.dto.foresight;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ForesightResponseDto {
    private Long id;
    private Long workSkillId;
    private String sourceName;
    private String sourceUrl;
    private Long canonicalId;
    private String canonicalName;
    private BigDecimal confidence;
    private String direction;
    private String professionCode;
    private String domain;
    private String techFamily;
    private LocalDate forecastDate;

    public ForesightResponseDto(Long id, Long workSkillId, String sourceName, String sourceUrl) {
        this.id = id;
        this.workSkillId = workSkillId;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
    }
}
