package io.github.alexeyaleksandrov.jacademicsupport.dto.hh;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Vacancy {
    private long id;
    private String name;
    private String publishedAt;
    private String description;
    private List<String> skills;
}
