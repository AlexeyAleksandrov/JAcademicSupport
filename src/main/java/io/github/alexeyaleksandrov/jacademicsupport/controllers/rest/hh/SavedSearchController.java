package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.hh;

import io.github.alexeyaleksandrov.jacademicsupport.models.SavedSearch;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SavedSearchRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/saved-searches")
@AllArgsConstructor
public class SavedSearchController {
    private final SavedSearchRepository savedSearchRepository;

    // Получить все сохраненные поисковые запросы
    @GetMapping
    public ResponseEntity<List<SavedSearch>> getAllSavedSearches() {
        try {
            List<SavedSearch> searches = savedSearchRepository.findAll();
            return ResponseEntity.ok(searches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Получить конкретный поисковый запрос по ID
    @GetMapping("/{id}")
    public ResponseEntity<SavedSearch> getSavedSearchById(@PathVariable Long id) {
        try {
            Optional<SavedSearch> search = savedSearchRepository.findById(id);
            return search.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Создать новый поисковый запрос
    @PostMapping
    public ResponseEntity<SavedSearch> createSavedSearch(@RequestBody SavedSearch savedSearch) {
        try {
            // Проверка на пустой запрос
            if (savedSearch.getSearchQuery() == null || savedSearch.getSearchQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Проверка на уникальность
            if (savedSearchRepository.existsBySearchQuery(savedSearch.getSearchQuery())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            SavedSearch newSearch = savedSearchRepository.save(savedSearch);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSearch);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Обновить существующий поисковый запрос
    @PutMapping("/{id}")
    public ResponseEntity<SavedSearch> updateSavedSearch(@PathVariable Long id, @RequestBody SavedSearch savedSearch) {
        try {
            // Проверка на существование записи
            if (!savedSearchRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // Проверка на пустой запрос
            if (savedSearch.getSearchQuery() == null || savedSearch.getSearchQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Проверка на уникальность (исключая текущую запись)
            Optional<SavedSearch> existingWithSameQuery = savedSearchRepository.findBySearchQuery(savedSearch.getSearchQuery());
            if (existingWithSameQuery.isPresent() && !existingWithSameQuery.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            savedSearch.setId(id);
            SavedSearch updatedSearch = savedSearchRepository.save(savedSearch);
            return ResponseEntity.ok(updatedSearch);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Удалить поисковый запрос
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavedSearch(@PathVariable Long id) {
        try {
            if (!savedSearchRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            savedSearchRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
