package io.github.alexeyaleksandrov.jacademicsupport.dto.keywords;

import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeywordDto {
    private Long id;
    private String keyword;
    private List<Long> workSkillIds;

    public static KeywordDto fromEntity(Keyword entity) {
        KeywordDto dto = new KeywordDto();
        dto.setId(entity.getId());
        dto.setKeyword(entity.getKeyword());
        
        if (entity.getWorkSkills() != null) {
            dto.setWorkSkillIds(entity.getWorkSkills().stream()
                    .map(ws -> ws.getId())
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
