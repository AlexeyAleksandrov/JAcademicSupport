package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstAggregationResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstAggregationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для расчета DST-агрегации
 */
@RestController
@RequestMapping("/api/dst-aggregation")
@AllArgsConstructor
public class DstAggregationController {
    
    private final DstAggregationService dstAggregationService;
    
    /**
     * Рассчитывает DST-агрегацию для указанного навыка
     * 
     * @param workSkillId ID навыка (WorkSkill)
     * @return результат агрегации с 4 метриками:
     *         - rpdCoveragePercentage: процент времени навыка от общего количества часов в РПД
     *         - marketDemand: востребованность на рынке
     *         - expertOpinionPercentage: процент экспертов, высказавшихся о навыке
     *         - foresightPercentage: процент источников прогнозов, рекомендующих навык
     */
    @GetMapping("/{workSkillId}")
    public ResponseEntity<DstAggregationResponseDto> getDstAggregation(@PathVariable Long workSkillId) {
        try {
            DstAggregationResponseDto result = dstAggregationService.calculateDstAggregation(workSkillId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // WorkSkill не найден
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Другие ошибки
            return ResponseEntity.internalServerError().build();
        }
    }
}
