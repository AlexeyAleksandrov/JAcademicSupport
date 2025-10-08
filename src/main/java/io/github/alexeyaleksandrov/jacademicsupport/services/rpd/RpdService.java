package io.github.alexeyaleksandrov.jacademicsupport.services.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RpdService {
    private final RpdRepository rpdRepository;
    private final CompetencyAchievementIndicatorRepository indicatorRepository;

    public Rpd createRpd(CreateRpdDTO createRpdDTO) {
        Rpd rpd = new Rpd();
        rpd.setDisciplineName(createRpdDTO.getDiscipline_name());
        rpd.setYear(createRpdDTO.getYear());
        rpd.setCompetencyAchievementIndicators(
                createRpdDTO.getCompetencyAchievementIndicators().stream()
                        .map(indicatorRepository::findByNumber)
                        .toList()
        );
        rpd = rpdRepository.saveAndFlush(rpd);
        return rpd;
    }

    public List<Rpd> findAll() {
        return rpdRepository.findAll();
    }

    public Optional<Rpd> findById(Long id) {
        return rpdRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return rpdRepository.existsById(id);
    }
}
