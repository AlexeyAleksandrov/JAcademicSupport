package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    List<SavedSearch> findAll();

    boolean existsBySearchQuery(String searchQuery);

    Optional<SavedSearch> findBySearchQuery(String searchQuery);
}