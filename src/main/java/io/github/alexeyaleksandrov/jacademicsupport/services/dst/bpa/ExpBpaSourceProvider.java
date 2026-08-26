package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpBpaSourceProvider extends AbstractBpaSourceProvider {

    private final DstQueryService    dstQueryService;
    private final DstSettingsService settingsService;

    @Override public String getName()    { return "EXP"; }
    @Override public boolean isEnabled() { return Boolean.TRUE.equals(settingsService.get().getExpEnabled()); }
    @Override public double getLambda()  { return settingsService.get().getLambdaExpL1(); }
    @Override public double getWeight()  { return settingsService.get().getWExp(); }

    @Override
    public BpaResult compute(DstContext ctx) {
        DstQueryService.BpaResult raw;
        if (ctx.getCanonicalId() != null) {
            raw = dstQueryService.getExpBpaByCanonical(ctx.getProfessionCode(), ctx.getCanonicalId());
        } else if (ctx.getTechFamily() != null) {
            raw = dstQueryService.getExpBpaByFamily(ctx.getProfessionCode(), ctx.getDomain(), ctx.getTechFamily());
        } else if (ctx.getDomain() != null) {
            raw = dstQueryService.getExpBpaByDomain(ctx.getProfessionCode(), ctx.getDomain());
        } else {
            return BpaResult.disabled(getName());
        }
        return buildResult(raw);
    }
}
