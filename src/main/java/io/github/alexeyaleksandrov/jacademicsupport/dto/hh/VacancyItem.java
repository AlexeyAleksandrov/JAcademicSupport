package io.github.alexeyaleksandrov.jacademicsupport.dto.hh;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyItem {
    private long id;
    private String name;
    private String publishedAt;
}
