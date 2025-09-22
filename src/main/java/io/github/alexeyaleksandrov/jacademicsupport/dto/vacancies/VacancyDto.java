package io.github.alexeyaleksandrov.jacademicsupport.dto.vacancies;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyDto {
    @NotNull
    private Long hhId;

    @NotBlank
    private String name;

    private String publishedAt;
    private String description;
    private List<Long> skillIds;
}
