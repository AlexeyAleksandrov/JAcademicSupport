package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacBpaSourceProvider extends AbstractBpaSourceProvider {

    private static final double LAMBDA = 15.0;
    private static final double WEIGHT = 0.8;

    private final DstQueryService dstQueryService;

    @Override public String getName()    { return "VAC"; }
    @Override public boolean isEnabled() { return true; }
    @Override public double getLambda()  { return LAMBDA; }
    @Override public double getWeight()  { return WEIGHT; }

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
