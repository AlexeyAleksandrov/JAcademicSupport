package io.github.alexeyaleksandrov.jacademicsupport.services.vacancies;

import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyEntityRepository vacancyRepository;

    public List<VacancyEntity> findAll() {
        return vacancyRepository.findAll();
    }

    public Optional<VacancyEntity> findById(Long id) {
        return vacancyRepository.findById(id);
    }

    public Optional<VacancyEntity> findByHhId(Long hhId) {
        return Optional.ofNullable(vacancyRepository.findByHhId(hhId));
    }

    public VacancyEntity save(VacancyEntity vacancy) {
        return vacancyRepository.save(vacancy);
    }

    public void deleteById(Long id) {
        vacancyRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return vacancyRepository.existsById(id);
    }

    public Page<VacancyEntity> findAllPaginated(int offset, int limit) {
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        return vacancyRepository.findAll(pageable);
    }

    public long count() {
        return vacancyRepository.count();
    }
}
