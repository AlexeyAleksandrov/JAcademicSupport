package io.github.alexeyaleksandrov.jacademicsupport.dto.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;

/**
 * Settings payload for the UI: the current values, the factory defaults
 * (so the interface can highlight what was changed) and the actual DB counts
 * behind the "auto" denominators.
 */
public record DstSettingsDto(
        DstSettings settings,
        DstSettings defaults,
        long        actualExpertCount,
        long        actualSourceCount,
        long        effectiveTotalExperts,
        long        effectiveTotalSources
) {}
