package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DstSettingsRepository extends JpaRepository<DstSettings, Long> {
}
