package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.Profession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, Long> {

    Optional<Profession> findByCode(String code);

    boolean existsByCode(String code);

    List<Profession> findAllByOrderByNameAsc();
}
