package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.CurriculumProfession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurriculumProfessionRepository extends JpaRepository<CurriculumProfession, Long> {

    List<CurriculumProfession> findByCurriculumId(Long curriculumId);

    Optional<CurriculumProfession> findByCurriculumIdAndProfessionCode(Long curriculumId, String professionCode);

    void deleteByCurriculumIdAndProfessionCode(Long curriculumId, String professionCode);
}
