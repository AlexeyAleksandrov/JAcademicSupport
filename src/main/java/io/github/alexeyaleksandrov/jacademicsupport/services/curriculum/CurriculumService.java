package io.github.alexeyaleksandrov.jacademicsupport.services.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.CurriculumDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.CurriculumResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.Curriculum;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CurriculumRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final DisciplineRepository disciplineRepository;
    private final DisciplineService disciplineService;

    public List<CurriculumResponseDto> getAll() {
        return curriculumRepository.findAll().stream()
                .map(this::toShallowDto)
                .toList();
    }

    public CurriculumResponseDto getById(Long id) {
        Curriculum c = curriculumRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curriculum not found: " + id));
        List<DisciplineResponseDto> disciplines = disciplineRepository
                .findByCurriculumIdOrderBySemesterAscNameAsc(id)
                .stream()
                .map(d -> disciplineService.toDtoWithCoverage(d))
                .toList();
        return new CurriculumResponseDto(c.getId(), c.getName(), c.getSpecialization(),
                c.getProfile(), c.getAcademicYear(), disciplines);
    }

    public CurriculumResponseDto create(CurriculumDto dto) {
        Curriculum c = new Curriculum();
        c.setName(dto.getName());
        c.setSpecialization(dto.getSpecialization());
        c.setProfile(dto.getProfile());
        c.setAcademicYear(dto.getAcademicYear());
        Curriculum saved = curriculumRepository.save(c);
        return toShallowDto(saved);
    }

    public CurriculumResponseDto update(Long id, CurriculumDto dto) {
        Curriculum c = curriculumRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curriculum not found: " + id));
        c.setName(dto.getName());
        c.setSpecialization(dto.getSpecialization());
        c.setProfile(dto.getProfile());
        c.setAcademicYear(dto.getAcademicYear());
        return toShallowDto(curriculumRepository.save(c));
    }

    public void delete(Long id) {
        curriculumRepository.deleteById(id);
    }

    private CurriculumResponseDto toShallowDto(Curriculum c) {
        return new CurriculumResponseDto(c.getId(), c.getName(), c.getSpecialization(),
                c.getProfile(), c.getAcademicYear(), null);
    }
}
