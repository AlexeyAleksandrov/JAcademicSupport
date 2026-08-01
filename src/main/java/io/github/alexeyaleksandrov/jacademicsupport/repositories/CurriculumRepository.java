package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
}
