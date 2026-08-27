package io.github.alexeyaleksandrov.jacademicsupport.services.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineCoverageDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.DisciplineCoverage;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineCoverageRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ProfessionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DisciplineCoverageServiceTest {
    private DisciplineCoverageRepository coverageRepository;
    private SkillCanonicalRepository skillRepository;
    private DisciplineCoverageService service;

    @BeforeEach
    void setUp() {
        coverageRepository = mock(DisciplineCoverageRepository.class);
        DisciplineRepository disciplineRepository = mock(DisciplineRepository.class);
        skillRepository = mock(SkillCanonicalRepository.class);
        service = new DisciplineCoverageService(coverageRepository, disciplineRepository,
                skillRepository, mock(ProfessionRepository.class));
        when(disciplineRepository.existsById(1L)).thenReturn(true);
        when(coverageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsDomainThatDoesNotMatchCanonicalSkill() {
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill(10L, "Python", "LANGUAGES", "Python")));
        DisciplineCoverageDto dto = new DisciplineCoverageDto(null, "DEVOPS", "Python", 10L, 20);
        assertThrows(ResponseStatusException.class, () -> service.create(1L, dto));
    }

    @Test
    void rejectsFamilyThatDoesNotMatchCanonicalSkill() {
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill(10L, "Python", "LANGUAGES", "Python")));
        DisciplineCoverageDto dto = new DisciplineCoverageDto(null, "LANGUAGES", "Automation", 10L, 20);
        assertThrows(ResponseStatusException.class, () -> service.create(1L, dto));
    }

    @Test
    void acceptsExplicitDomainForSkillWithoutFamily() {
        when(skillRepository.findById(11L)).thenReturn(Optional.of(skill(11L, "IDEF0", "GENERAL", null)));
        DisciplineCoverageDto dto = new DisciplineCoverageDto(null, "GENERAL", null, 11L, 20);
        service.create(1L, dto);
        assertEquals("GENERAL", dto.getDomain());
        assertEquals(null, dto.getTechFamily());
    }

    private SkillCanonical skill(Long id, String name, String domain, String family) {
        SkillCanonical skill = new SkillCanonical();
        skill.setId(id);
        skill.setName(name);
        skill.setNormalizedName(name.toLowerCase());
        skill.setDomain(domain);
        skill.setTechFamily(family);
        return skill;
    }
}
