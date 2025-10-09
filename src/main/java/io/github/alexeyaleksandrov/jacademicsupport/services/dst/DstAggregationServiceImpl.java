package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstAggregationResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ExpertOpinionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ForesightRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Реализация сервиса для расчета DST-агрегации
 */
@Service
@AllArgsConstructor
public class DstAggregationServiceImpl implements DstAggregationService {
    
    private final RpdSkillRepository rpdSkillRepository;
    private final WorkSkillRepository workSkillRepository;
    private final ExpertOpinionRepository expertOpinionRepository;
    private final ForesightRepository foresightRepository;
    
    @Override
    public DstAggregationResponseDto calculateDstAggregation(Long workSkillId) {
        // Проверяем существование WorkSkill
        System.out.println("Проверяем существование WorkSkill");
        Optional<WorkSkill> workSkillOpt = workSkillRepository.findById(workSkillId);
        if (workSkillOpt.isEmpty()) {
            throw new IllegalArgumentException("WorkSkill с ID " + workSkillId + " не найден");
        }
        
        WorkSkill workSkill = workSkillOpt.get();
        
        // 1. Расчет покрытия в РПД (процент часов данного навыка от общего количества часов)
        System.out.println("1. Расчет покрытия в РПД (процент часов данного навыка от общего количества часов)");
        double rpdCoveragePercentage = calculateRpdCoveragePercentage(workSkillId);
        
        // 2. Получение востребованности на рынке
        System.out.println("2. Получение востребованности на рынке");
        double marketDemand = workSkill.getRoundedMarketDemand();
        
        // 3. Расчет процента экспертов, которые выразили мнение по данному навыку
        System.out.println("3. Расчет процента экспертов, которые выразили мнение по данному навыку");
        double expertOpinionPercentage = calculateExpertOpinionPercentage(workSkillId);
        
        // 4. Расчет процента источников прогнозов, которые рекомендуют данный навык
        System.out.println("4. Расчет процента источников прогнозов, которые рекомендуют данный навык");
        double foresightPercentage = calculateForesightPercentage(workSkillId);
        
        return new DstAggregationResponseDto(
            rpdCoveragePercentage,
            marketDemand,
            expertOpinionPercentage,
            foresightPercentage
        );
    }
    
    /**
     * Расчет процента времени данного WorkSkill от общего количества часов всех РПД
     */
    private double calculateRpdCoveragePercentage(Long workSkillId) {
        Long totalTimeForWorkSkill = rpdSkillRepository.getTotalTimeByWorkSkillId(workSkillId);
        Long totalTime = rpdSkillRepository.getTotalTime();
        
        if (totalTime == null || totalTime == 0) {
            return 0.0;
        }
        
        return ((double) totalTimeForWorkSkill / totalTime) * 100.0;
    }
    
    /**
     * Расчет процента уникальных экспертов, которые выразили мнение по данному навыку
     */
    private double calculateExpertOpinionPercentage(Long workSkillId) {
        Long expertsForSkill = expertOpinionRepository.countDistinctExpertsByWorkSkillId(workSkillId);
        Long totalExperts = expertOpinionRepository.countDistinctExperts();
        
        if (totalExperts == null || totalExperts == 0) {
            return 0.0;
        }
        
        return ((double) expertsForSkill / totalExperts) * 100.0;
    }
    
    /**
     * Расчет процента уникальных источников прогнозов, которые рекомендуют данный навык
     */
    private double calculateForesightPercentage(Long workSkillId) {
        Long sourcesForSkill = foresightRepository.countDistinctSourceUrlsByWorkSkillId(workSkillId);
        Long totalSources = foresightRepository.countDistinctSourceUrls();
        
        if (totalSources == null || totalSources == 0) {
            return 0.0;
        }
        
        return ((double) sourcesForSkill / totalSources) * 100.0;
    }
}
