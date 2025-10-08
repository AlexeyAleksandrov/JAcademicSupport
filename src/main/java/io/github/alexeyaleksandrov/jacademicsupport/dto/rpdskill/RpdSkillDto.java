package io.github.alexeyaleksandrov.jacademicsupport.dto.rpdskill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpdSkillDto {
    private Long rpdId;
    private Long workSkillId;
    private Integer time; // время в академических часах
}
