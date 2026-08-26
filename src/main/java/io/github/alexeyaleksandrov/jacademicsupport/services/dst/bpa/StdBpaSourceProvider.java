package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * STD (ФГОС) source stub. It remains unavailable until compute() is implemented
 * from DisciplineCoverageService; the settings UI exposes this fact explicitly.
 */
@Component
@RequiredArgsConstructor
public class StdBpaSourceProvider extends AbstractBpaSourceProvider {

    private final DstSettingsService settingsService;

    @Override public String getName()    { return "STD"; }
    @Override public boolean isEnabled() { return false; }
    @Override public double getLambda()  { return settingsService.get().getLambdaStdL1(); }
    @Override public double getWeight()  { return settingsService.get().getWStd(); }

    @Override
    public BpaResult compute(DstContext ctx) {
        return BpaResult.disabled(getName());
    }
}
