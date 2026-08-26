package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;

public abstract class AbstractBpaSourceProvider implements BpaSourceProvider {

    /**
     * Single BPA construction path — delegates to {@link BpaSourceProvider#buildFromRaw}
     * so that m(T)/m(F)/m(Θ) and Shafer discounting are defined in exactly one place.
     */
    protected BpaResult buildResult(DstQueryService.BpaResult raw) {
        return buildFromRaw(raw);
    }
}
