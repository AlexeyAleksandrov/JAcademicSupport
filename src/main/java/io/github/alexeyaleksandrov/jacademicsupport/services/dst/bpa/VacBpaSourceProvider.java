package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacBpaSourceProvider extends AbstractBpaSourceProvider {

    private final DstQueryService   dstQueryService;
    private final DstSettingsService settingsService;

    @Override public String getName()    { return "VAC"; }
    @Override public boolean isEnabled() { return Boolean.TRUE.equals(settingsService.get().getVacEnabled()); }
    @Override public double getLambda()  { return settingsService.get().getLambdaVacL1(); }
    @Override public double getWeight()  { return settingsService.get().getWVac(); }

    @Override
    public BpaResult compute(DstContext ctx) {
        DstQueryService.BpaResult raw;
        if (ctx.getCanonicalId() != null) {
            raw = dstQueryService.getVacBpaByCanonical(ctx.getProfessionCode(), ctx.getCanonicalId());
        } else if (ctx.getTechFamily() != null) {
            raw = dstQueryService.getVacBpaByFamily(ctx.getProfessionCode(), ctx.getDomain(), ctx.getTechFamily());
        } else if (ctx.getDomain() != null) {
            raw = dstQueryService.getVacBpaByDomain(ctx.getProfessionCode(), ctx.getDomain());
        } else {
            return BpaResult.disabled(getName());
        }
        return buildResult(raw);
    }
}
