package io.github.alexeyaleksandrov.jacademicsupport.models.hh;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vacancy {
    private long id;
    private String name;
    private String publishedAt;
    private List<String> skills;
}
