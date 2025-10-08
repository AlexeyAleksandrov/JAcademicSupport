package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertsRepository extends JpaRepository<ExpertEntity, Long> {
}
