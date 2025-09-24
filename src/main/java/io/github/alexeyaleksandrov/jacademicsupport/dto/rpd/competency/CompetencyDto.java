package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.competency;

import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CompetencyDto {
    private Long id;
    private String number;
    private String description;
    private List<String> keywords;

    public static CompetencyDto fromEntity(Competency competency) {
        CompetencyDto dto = new CompetencyDto();
        dto.setId(competency.getId());
        dto.setNumber(competency.getNumber());
        dto.setDescription(competency.getDescription());
        if (competency.getKeywords() != null) {
            dto.setKeywords(competency.getKeywords().stream()
                    .map(Keyword::getKeyword)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
