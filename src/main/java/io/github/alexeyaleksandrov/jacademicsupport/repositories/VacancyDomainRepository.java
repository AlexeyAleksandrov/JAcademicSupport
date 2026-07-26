package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VacancyDomainRepository extends JpaRepository<VacancyDomain, Long> {

    Optional<VacancyDomain> findByVacancyId(Long vacancyId);
}
