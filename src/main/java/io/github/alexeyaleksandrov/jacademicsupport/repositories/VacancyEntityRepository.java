package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VacancyEntityRepository extends JpaRepository<VacancyEntity, Long> {
    boolean existsByHhId(long hhId);
    VacancyEntity findByHhId(long hhId);
    
    /**
     * Finds all vacancies (without filtering).
     */
    Page<VacancyEntity> findAll(Pageable pageable);
    
    /**
     * Finds all vacancies (without filtering).
     */
    List<VacancyEntity> findAll();
}