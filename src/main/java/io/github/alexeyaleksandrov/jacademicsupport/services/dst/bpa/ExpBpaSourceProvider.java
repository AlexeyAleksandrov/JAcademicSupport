package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpBpaSourceProvider extends AbstractBpaSourceProvider {

    private static final double LAMBDA = 5.0;
    private static final double WEIGHT = 0.9;

    private final DstQueryService dstQueryService;

    @Override public String getName()    { return "EXP"; }
    @Override public boolean isEnabled() { return true; }
    @Override public double getLambda()  { return LAMBDA; }
    @Override public double getWeight()  { return WEIGHT; }

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
