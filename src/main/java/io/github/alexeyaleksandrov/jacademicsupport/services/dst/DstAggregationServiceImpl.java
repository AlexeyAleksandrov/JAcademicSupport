package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstAggregationResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillCanonicalRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для расчета DST-агрегации.
 * EXP и FC источники теперь используют DST BPA (κ × avgScore) вместо простых процентов.
 */
@Service
@AllArgsConstructor
public class DstAggregationServiceImpl implements DstAggregationService {

    private final RpdSkillRepository         rpdSkillRepository;
    private final WorkSkillRepository         workSkillRepository;
    private final WorkSkillCanonicalRepository workSkillCanonicalRepository;
    private final DstQueryService             dstQueryService;

    @Override
    public DstAggregationResponseDto calculateDstAggregation(Long workSkillId) {
        Optional<WorkSkill> workSkillOpt = workSkillRepository.findById(workSkillId);
        if (workSkillOpt.isEmpty()) {
            throw new IllegalArgumentException("WorkSkill с ID " + workSkillId + " не найден");
        }
        WorkSkill workSkill = workSkillOpt.get();

        // 1. Покрытие в РПД (% часов навыка от общего количества часов)
        double rpdCoveragePercentage = calculateRpdCoveragePercentage(workSkillId);

        // 2. Востребованность на рынке (VAC-источник, уже посчитан)
        double marketDemand = workSkill.getRoundedMarketDemand();

        // 3. EXP → DST BPA по canonical_id навыка
        double expMT = calculateExpBpa(workSkillId);

        // 4. FC → DST BPA по canonical_id навыка
        double fcMT = calculateFcBpa(workSkillId);

        return new DstAggregationResponseDto(rpdCoveragePercentage, marketDemand, expMT, fcMT);
    }

    private double calculateRpdCoveragePercentage(Long workSkillId) {
        Long totalTimeForWorkSkill = rpdSkillRepository.getTotalTimeByWorkSkillId(workSkillId);
        Long totalTime = rpdSkillRepository.getTotalTime();
        if (totalTime == null || totalTime == 0) return 0.0;
        return ((double) totalTimeForWorkSkill / totalTime) * 100.0;
    }

    /**
     * Получить первый canonical_id для данного work_skill (via work_skill_canonical).
     * Если маппинга нет — вернуть null.
     */
    private Long resolveCanonicalId(Long workSkillId) {
        List<io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkillCanonical> links =
                workSkillCanonicalRepository.findByWorkSkillIdIn(List.of(workSkillId));
        return links.isEmpty() ? null : links.get(0).getCanonicalId();
    }

    private double calculateExpBpa(Long workSkillId) {
        Long canonicalId = resolveCanonicalId(workSkillId);
        if (canonicalId == null) return 0.0;
        DstQueryService.BpaResult bpa = dstQueryService.getExpBpaByCanonical(null, canonicalId);
        return bpa.mT() * 100.0;
    }

    private double calculateFcBpa(Long workSkillId) {
        Long canonicalId = resolveCanonicalId(workSkillId);
        if (canonicalId == null) return 0.0;
        DstQueryService.BpaResult bpa = dstQueryService.getFcBpaByCanonical(null, canonicalId);
        return bpa.mT() * 100.0;
    }
}
