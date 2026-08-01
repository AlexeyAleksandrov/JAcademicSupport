package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import org.springframework.stereotype.Component;

/**
 * STD (ФГОС) source stub — disabled until RPD coverage data is integrated.
 * To enable: set enabled=true and implement compute() using DisciplineCoverageService.
 */
@Component
public class StdBpaSourceProvider extends AbstractBpaSourceProvider {

    @Override public String getName()    { return "STD"; }
    @Override public boolean isEnabled() { return false; }
    @Override public double getLambda()  { return 3.0; }
    @Override public double getWeight()  { return 0.7; }

    @Override
    public BpaResult compute(DstContext ctx) {
        return BpaResult.disabled(getName());
    }
}
