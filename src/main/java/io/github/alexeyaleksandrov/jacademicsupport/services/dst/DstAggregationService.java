package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstAggregationResponseDto;

/**
 * Сервис для расчета DST-агрегации на основе WorkSkill
 */
public interface DstAggregationService {
    
    /**
     * Рассчитывает DST-агрегацию для указанного навыка
     * @param workSkillId ID навыка
     * @return результат агрегации с 4 метриками
     */
    DstAggregationResponseDto calculateDstAggregation(Long workSkillId);
}
