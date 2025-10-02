package io.github.alexeyaleksandrov.jacademicsupport.dto.foresight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForesightResponseDto {
    private Long id;
    private Long workSkillId;  // ID of referenced WorkSkill
    private String sourceName;
    private String sourceUrl;
}
