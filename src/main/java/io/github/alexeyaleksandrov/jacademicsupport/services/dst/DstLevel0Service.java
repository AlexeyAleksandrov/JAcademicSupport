package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0.DstL0DomainResult;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0.DstL0Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.models.Curriculum;
import io.github.alexeyaleksandrov.jacademicsupport.models.CurriculumProfession;
import io.github.alexeyaleksandrov.jacademicsupport.models.Discipline;
import io.github.alexeyaleksandrov.jacademicsupport.models.DisciplineCoverage;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CurriculumProfessionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CurriculumRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineCoverageRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DstLevel0Service {

    private final CurriculumRepository         curriculumRepository;
    private final CurriculumProfessionRepository professionRepository;
    private final DisciplineRepository         disciplineRepository;
    private final DisciplineCoverageRepository coverageRepository;
    private final DstQueryService              dstQueryService;
    private final DstCombinationService        combinationService;

    @Transactional(readOnly = true)
    public DstL0Response analyzeLevel0(Long curriculumId) {
        DstL0Response resp = new DstL0Response();
        resp.setCurriculumId(curriculumId);

        Optional<Curriculum> optCurr = curriculumRepository.findById(curriculumId);
        if (optCurr.isEmpty()) {
            resp.setError("Учебный план не найден: " + curriculumId);
            return resp;
        }
        resp.setCurriculumName(optCurr.get().getName());

        List<CurriculumProfession> cpList = professionRepository.findByCurriculumId(curriculumId);
        if (cpList.isEmpty()) {
            resp.setError("В учебном плане не указаны профессии. Добавьте профессии с весами.");
            return resp;
        }

        double totalWeight = cpList.stream().mapToDouble(CurriculumProfession::getWeight).sum();
        List<ProfessionWeight> profs = cpList.stream()
                .map(cp -> new ProfessionWeight(
                        cp.getProfessionCode(),
                        cp.getProfessionName(),
                        totalWeight > 0 ? cp.getWeight() / totalWeight : 1.0 / cpList.size()))
                .collect(Collectors.toList());
        resp.setProfessions(profs);

        List<Discipline> allInCurriculum = disciplineRepository.findByCurriculumId(curriculumId);
        List<Long> allDiscIds = allInCurriculum.stream().map(Discipline::getId).collect(Collectors.toList());

        int totalHours = allInCurriculum.stream()
                .mapToInt(d -> d.getTotalHours() != null ? d.getTotalHours() : 0)
                .sum();
        resp.setTotalCurriculumHours(totalHours);

        Set<String> vacDomains = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getDomainsForProfession(pw.professionCode()).stream()
                    .map(DstQueryService.DomainClusterInfo::domain)
                    .filter(Objects::nonNull)
                    .forEach(vacDomains::add);
        }

        Set<String> rpdDomains = allDiscIds.isEmpty() ? Set.of() :
                coverageRepository.findByDisciplineIdIn(allDiscIds).stream()
                        .map(DisciplineCoverage::getDomain)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        Set<String> allDomains = new LinkedHashSet<>();
        allDomains.addAll(vacDomains);
        allDomains.addAll(rpdDomains);

        Set<Long> discIdsWithDomainBlocks = allDiscIds.isEmpty() ? Set.of() :
                coverageRepository.findDisciplineIdsWithExplicitDomainBlocks(allDiscIds);
        List<Long> discIdsWithoutDomainBlocks = allDiscIds.stream()
                .filter(id -> !discIdsWithDomainBlocks.contains(id))
                .collect(Collectors.toList());

        List<DstL0DomainResult> results = new ArrayList<>();
        for (String domain : allDomains) {
            int supplyHours = 0;
            if (totalHours > 0 && !allDiscIds.isEmpty()) {
                supplyHours += coverageRepository.sumHoursByDisciplineIdsAndDomain(allDiscIds, domain);
                if (!discIdsWithoutDomainBlocks.isEmpty()) {
                    supplyHours += coverageRepository.sumSkillInferredDomainHours(discIdsWithoutDomainBlocks, domain);
                }
            }
            double supply = (totalHours > 0) ? (double) supplyHours / totalHours : 0.0;

            String primaryProfCode = profs.get(0).professionCode();
            DstContext ctx = new DstContext(primaryProfCode, domain, null, null);
            DstTraceResponse trace = combinationService.computeWeighted(ctx, profs, supply, dstQueryService);

            DstL0DomainResult dr = new DstL0DomainResult();
            dr.setDomain(domain);
            dr.setMT(trace.getMT());
            dr.setMU(trace.getMU());
            dr.setMF(trace.getMF());
            dr.setK(trace.getK());
            dr.setBetp(trace.getBetp());
            dr.setSupply(supply);
            dr.setSupplyHours(supplyHours);
            dr.setDelta(trace.getDelta());
            dr.setUsedYager(trace.isUsedYager());
            dr.setRecommendation(trace.getRecommendation());
            dr.setSources(trace.getSources());
            dr.setCombinations(trace.getCombinations());
            results.add(dr);
        }

        results.sort(Comparator.comparingDouble(DstL0DomainResult::getBetp).reversed());
        resp.setDomains(results);
        return resp;
    }
}
