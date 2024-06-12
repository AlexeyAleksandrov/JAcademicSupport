package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.competency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EditCompetenceForm {
    String number;
    String description;
    List<Long> selectedKeywords = new ArrayList<>();
}
