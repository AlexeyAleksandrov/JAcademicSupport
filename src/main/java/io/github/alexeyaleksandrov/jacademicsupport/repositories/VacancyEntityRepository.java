package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyEntityRepository extends JpaRepository<VacancyEntity, Long> {
    boolean existsByHhId(long hhId);
    VacancyEntity findByHhId(long hhId);
}