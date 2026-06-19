package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.Profession;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyProfession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyProfessionRepository extends JpaRepository<VacancyProfession, VacancyProfession.VacancyProfessionId> {

    List<VacancyProfession> findByVacancy(VacancyEntity vacancy);

    List<VacancyProfession> findByProfession(Profession profession);

    @Query("SELECT vp FROM VacancyProfession vp WHERE vp.profession.code = :profCode")
    List<VacancyProfession> findByProfessionCode(@Param("profCode") String profCode);

    boolean existsByVacancyAndProfession(VacancyEntity vacancy, Profession profession);

    long countByProfession(Profession profession);
}
