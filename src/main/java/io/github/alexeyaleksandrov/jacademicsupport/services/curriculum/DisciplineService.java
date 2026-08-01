package io.github.alexeyaleksandrov.jacademicsupport.services.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.Discipline;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DisciplineService {

    private final DisciplineRepository disciplineRepository;
    private final DisciplineCoverageService coverageService;

    public List<DisciplineResponseDto> getByCurriculum(Long curriculumId) {
        return disciplineRepository.findByCurriculumIdOrderBySemesterAscNameAsc(curriculumId)
                .stream()
                .map(this::toDtoWithCoverage)
                .toList();
    }

    public DisciplineResponseDto getById(Long id) {
        Discipline d = disciplineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discipline not found: " + id));
        return toDtoWithCoverage(d);
    }

    public DisciplineResponseDto create(DisciplineDto dto) {
        Discipline d = new Discipline();
        d.setCurriculumId(dto.getCurriculumId());
        d.setName(dto.getName());
        d.setTotalHours(dto.getTotalHours());
        d.setSemester(dto.getSemester());
        return toDtoWithCoverage(disciplineRepository.save(d));
    }

    public DisciplineResponseDto update(Long id, DisciplineDto dto) {
        Discipline d = disciplineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discipline not found: " + id));
        d.setName(dto.getName());
        d.setTotalHours(dto.getTotalHours());
        d.setSemester(dto.getSemester());
        if (dto.getCurriculumId() != null) {
            d.setCurriculumId(dto.getCurriculumId());
        }
        return toDtoWithCoverage(disciplineRepository.save(d));
    }

    public void delete(Long id) {
        disciplineRepository.deleteById(id);
    }

    public DisciplineResponseDto toDtoWithCoverage(Discipline d) {
        return new DisciplineResponseDto(
                d.getId(),
                d.getCurriculumId(),
                d.getName(),
                d.getTotalHours(),
                d.getSemester(),
                coverageService.getByDisciplineId(d.getId())
        );
    }
}
