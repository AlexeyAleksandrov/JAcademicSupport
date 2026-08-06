package io.github.alexeyaleksandrov.jacademicsupport.services.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineCoverageDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineCoverageResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.DisciplineCoverage;
import io.github.alexeyaleksandrov.jacademicsupport.models.Profession;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineCoverageRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ProfessionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DisciplineCoverageService {

    private final DisciplineCoverageRepository coverageRepository;
    private final SkillCanonicalRepository skillCanonicalRepository;
    private final ProfessionRepository professionRepository;

    public List<DisciplineCoverageResponseDto> getByDisciplineId(Long disciplineId) {
        List<DisciplineCoverage> entries = coverageRepository.findByDisciplineId(disciplineId);
        return enrich(entries);
    }

    public DisciplineCoverageResponseDto create(Long disciplineId, DisciplineCoverageDto dto) {
        DisciplineCoverage entry = new DisciplineCoverage();
        entry.setDisciplineId(disciplineId);
        entry.setProfessionCode(dto.getProfessionCode());
        entry.setDomain(dto.getDomain());
        entry.setTechFamily(dto.getTechFamily());
        entry.setCanonicalId(dto.getCanonicalId());
        entry.setHours(dto.getHours() != null ? dto.getHours() : 0);
        DisciplineCoverage saved = coverageRepository.save(entry);
        SkillCanonical sc = resolveCanonical(saved.getCanonicalId());
        return toDto(saved,
                sc != null ? sc.getName()       : null,
                sc != null ? sc.getDomain()     : null,
                sc != null ? sc.getTechFamily() : null,
                resolveProfessionName(saved.getProfessionCode()));
    }

    public void delete(Long id) {
        coverageRepository.deleteById(id);
    }

    /**
     * Считает supply(РПД) как долю часов по заданному критерию
     * от общего числа часов дисциплин в учебном плане.
     * Используется в DST-расчёте как метрика покрытия.
     */
    public double computeSupply(List<Long> disciplineIds, String domain,
                                 String techFamily, Long canonicalId) {
        int total = coverageRepository.sumTotalHoursByDisciplineIds(disciplineIds);
        if (total == 0) return 0.0;
        int specific;
        if (canonicalId != null) {
            specific = coverageRepository.sumHoursByDisciplineIdsAndCanonical(disciplineIds, canonicalId);
        } else if (techFamily != null) {
            specific = coverageRepository.sumHoursByDisciplineIdsAndFamily(disciplineIds, techFamily);
        } else if (domain != null) {
            specific = coverageRepository.sumHoursByDisciplineIdsAndDomain(disciplineIds, domain);
        } else {
            return 0.0;
        }
        return (double) specific / total;
    }

    private List<DisciplineCoverageResponseDto> enrich(List<DisciplineCoverage> entries) {
        List<Long> canonicalIds = entries.stream()
                .map(DisciplineCoverage::getCanonicalId)
                .filter(id -> id != null)
                .distinct().toList();
        Map<Long, SkillCanonical> canonicalMap = skillCanonicalRepository.findAllById(canonicalIds)
                .stream().collect(Collectors.toMap(SkillCanonical::getId, sc -> sc));

        List<String> profCodes = entries.stream()
                .map(DisciplineCoverage::getProfessionCode)
                .filter(c -> c != null)
                .distinct().toList();
        Map<String, String> profNames = professionRepository.findAll().stream()
                .filter(p -> profCodes.contains(p.getCode()))
                .collect(Collectors.toMap(Profession::getCode, Profession::getName));

        return entries.stream()
                .map(e -> {
                    SkillCanonical sc = e.getCanonicalId() != null ? canonicalMap.get(e.getCanonicalId()) : null;
                    return toDto(e,
                            sc != null ? sc.getName()       : null,
                            sc != null ? sc.getDomain()     : null,
                            sc != null ? sc.getTechFamily() : null,
                            e.getProfessionCode() != null ? profNames.get(e.getProfessionCode()) : null);
                })
                .toList();
    }

    private DisciplineCoverageResponseDto toDto(DisciplineCoverage e,
                                                  String canonicalName,
                                                  String canonicalDomain,
                                                  String canonicalTechFamily,
                                                  String professionName) {
        DisciplineCoverageResponseDto dto = new DisciplineCoverageResponseDto(
                e.getId(), e.getDisciplineId(),
                e.getProfessionCode(), professionName,
                e.getDomain(), e.getTechFamily(),
                e.getCanonicalId(), canonicalName,
                null, null,
                e.getHours()
        );
        dto.setCanonicalDomain(canonicalDomain);
        dto.setCanonicalTechFamily(canonicalTechFamily);
        return dto;
    }

    private SkillCanonical resolveCanonical(Long canonicalId) {
        if (canonicalId == null) return null;
        return skillCanonicalRepository.findById(canonicalId).orElse(null);
    }

    private String resolveProfessionName(String profCode) {
        if (profCode == null) return null;
        Optional<Profession> p = professionRepository.findAll().stream()
                .filter(pr -> pr.getCode().equals(profCode))
                .findFirst();
        return p.map(Profession::getName).orElse(null);
    }
}
