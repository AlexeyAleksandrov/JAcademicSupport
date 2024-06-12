package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreateRpdFormDto {
    private String disciplineName;
    private int year = 2024;
    private List<Long> selectedIndicators = new ArrayList<>();
}
