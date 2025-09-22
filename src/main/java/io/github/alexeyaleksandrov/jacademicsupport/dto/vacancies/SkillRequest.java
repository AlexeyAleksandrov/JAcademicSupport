package io.github.alexeyaleksandrov.jacademicsupport.dto.vacancies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {
    @NotNull
    private Long skillId;
}