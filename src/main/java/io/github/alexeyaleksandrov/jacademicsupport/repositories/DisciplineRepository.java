package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    List<Discipline> findByCurriculumId(Long curriculumId);

    List<Discipline> findByCurriculumIdOrderBySemesterAscNameAsc(Long curriculumId);
}
