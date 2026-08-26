package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstSettingsDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dst/settings")
@RequiredArgsConstructor
public class DstSettingsController {

    private final DstSettingsService settingsService;

    @GetMapping
    public DstSettingsDto get() {
        return build(settingsService.get());
    }

    @GetMapping("/defaults")
    public DstSettings defaults() {
        return settingsService.defaults();
    }

    @PutMapping
    public DstSettingsDto update(@RequestBody DstSettings incoming) {
        return build(settingsService.update(incoming));
    }

    @PostMapping("/reset")
    public DstSettingsDto reset() {
        return build(settingsService.resetToDefaults());
    }

    private DstSettingsDto build(DstSettings current) {
        return new DstSettingsDto(
                current,
                settingsService.defaults(),
                settingsService.actualExpertCount(),
                settingsService.actualSourceCount(),
                settingsService.effectiveTotalExperts(),
                settingsService.effectiveTotalSources());
    }
}
