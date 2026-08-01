package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcBpaSourceProvider extends AbstractBpaSourceProvider {

    private static final double LAMBDA = 2.0;
    private static final double WEIGHT = 0.6;

    private final DstQueryService dstQueryService;

    @Override public String getName()    { return "FC"; }
    @Override public boolean isEnabled() { return true; }
    @Override public double getLambda()  { return LAMBDA; }
    @Override public double getWeight()  { return WEIGHT; }

    @Override
    public BpaResult compute(DstContext ctx) {
        DstQueryService.BpaResult raw;
        if (ctx.getCanonicalId() != null) {
            raw = dstQueryService.getFcBpaByCanonical(ctx.getProfessionCode(), ctx.getCanonicalId());
        } else if (ctx.getTechFamily() != null) {
            raw = dstQueryService.getFcBpaByFamily(ctx.getProfessionCode(), ctx.getDomain(), ctx.getTechFamily());
        } else if (ctx.getDomain() != null) {
            raw = dstQueryService.getFcBpaByDomain(ctx.getProfessionCode(), ctx.getDomain());
        } else {
            return BpaResult.disabled(getName());
        }
        return buildResult(raw);
    }
}
