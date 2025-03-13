package io.github.alexeyaleksandrov.jacademicsupport.dto.hh;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyPage {
    private List<VacancyItem> vacancies;
    private int pages;
}
