package io.github.alexeyaleksandrov.jacademicsupport.dto.dst;

/**
 * DTO для результата DST-агрегации по навыку
 */
public class DstAggregationResponseDto {
    
    /**
     * Процент времени данного WorkSkill от общего количества часов всех РПД
     */
    private double rpdCoveragePercentage;
    
    /**
     * Востребованность навыка на рынке (от 0 до 1)
     */
    private double marketDemand;
    
    /**
     * Процент уникальных экспертов, которые выразили мнение по данному навыку
     */
    private double expertOpinionPercentage;
    
    /**
     * Процент уникальных источников прогнозов, которые рекомендуют данный навык
     */
    private double foresightPercentage;
    
    public DstAggregationResponseDto() {
    }
    
    public DstAggregationResponseDto(double rpdCoveragePercentage, double marketDemand, 
                                      double expertOpinionPercentage, double foresightPercentage) {
        this.rpdCoveragePercentage = rpdCoveragePercentage;
        this.marketDemand = marketDemand;
        this.expertOpinionPercentage = expertOpinionPercentage;
        this.foresightPercentage = foresightPercentage;
    }
    
    public double getRpdCoveragePercentage() {
        return rpdCoveragePercentage;
    }
    
    public void setRpdCoveragePercentage(double rpdCoveragePercentage) {
        this.rpdCoveragePercentage = rpdCoveragePercentage;
    }
    
    public double getMarketDemand() {
        return marketDemand;
    }
    
    public void setMarketDemand(double marketDemand) {
        this.marketDemand = marketDemand;
    }
    
    public double getExpertOpinionPercentage() {
        return expertOpinionPercentage;
    }
    
    public void setExpertOpinionPercentage(double expertOpinionPercentage) {
        this.expertOpinionPercentage = expertOpinionPercentage;
    }
    
    public double getForesightPercentage() {
        return foresightPercentage;
    }
    
    public void setForesightPercentage(double foresightPercentage) {
        this.foresightPercentage = foresightPercentage;
    }
}
