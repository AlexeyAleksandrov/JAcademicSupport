package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.competency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CreateCompetenceForm {
    String number;
    String description;
}
