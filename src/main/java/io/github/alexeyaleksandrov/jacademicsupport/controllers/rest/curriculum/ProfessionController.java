package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.models.Profession;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ProfessionRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/professions")
@AllArgsConstructor
public class ProfessionController {

    private final ProfessionRepository professionRepository;

    @GetMapping
    public List<Profession> getAll() {
        return professionRepository.findAll();
    }
}
