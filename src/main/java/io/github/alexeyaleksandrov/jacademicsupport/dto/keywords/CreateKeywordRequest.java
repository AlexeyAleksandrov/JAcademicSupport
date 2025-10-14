package io.github.alexeyaleksandrov.jacademicsupport.dto.keywords;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateKeywordRequest {
    @NotBlank(message = "Keyword cannot be blank")
    private String keyword;
    
    private List<Long> workSkillIds;
}
