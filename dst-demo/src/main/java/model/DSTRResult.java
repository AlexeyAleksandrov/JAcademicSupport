package model;

/**
 * Результат DST-агрегации для кластера или навыка
 */
public class DSTRResult {
    // Итоговое распределение масс
    private double mT_final;      // Уверенность во включении
    private double mU_final;      // Неопределённость
    private double mF_final;      // Уверенность в исключении
    
    // Промежуточные результаты комбинирования
    private double mT_vac;        // После вакансий
    private double mT_exp;        // После экспертов
    private double mT_forecast;   // После прогнозов
    private double mT_12;         // После комбинации vac+exp
    private double mT_final_step; // Итог
    
    // Конфликт
    private double K;             // Показатель конфликта
    private boolean highConflict; // K > τK ?
    
    // Функции доверия и правдоподобия
    private double Bel;           // Функция доверия
    private double Pl;            // Функция правдоподобия
    private double BetP;          // Вероятностная мера
    
    // Показатель дефицита
    private double delta;         // Δi = BetP - supply
    private boolean hasDeficit;   // Δi > τΔ ?
    
    // Рекомендация
    private String recommendation;
    private String recommendationLevel; // "strong", "moderate", "preserve", "reduce", "exclude"
    
    public DSTRResult(double mT_final, double mU_final, double mF_final,
                      double mT_vac, double mT_exp, double mT_forecast,
                      double mT_12, double K, double Bel, double Pl, double BetP,
                      double delta, String recommendation, String recommendationLevel) {
        this.mT_final = mT_final;
        this.mU_final = mU_final;
        this.mF_final = mF_final;
        this.mT_vac = mT_vac;
        this.mT_exp = mT_exp;
        this.mT_forecast = mT_forecast;
        this.mT_12 = mT_12;
        this.mT_final_step = mT_final;
        this.K = K;
        this.Bel = Bel;
        this.Pl = Pl;
        this.BetP = BetP;
        this.delta = delta;
        this.recommendation = recommendation;
        this.recommendationLevel = recommendationLevel;
        this.highConflict = K > 0.4;
        this.hasDeficit = delta > 0.15;
    }
    
    @Override
    public String toString() {
        return String.format(
            "DST Result:\n" +
            "  m(T) = %.4f (%.2f%%) - Уверенность во включении\n" +
            "  m(U) = %.4f (%.2f%%) - Неопределённость\n" +
            "  m(F) = %.4f (%.2f%%) - Уверенность в исключении\n" +
            "  K = %.4f %s\n" +
            "  BetP = %.4f\n" +
            "  Δ = %.4f %s\n" +
            "  Рекомендация: %s",
            mT_final, mT_final * 100,
            mU_final, mU_final * 100,
            mF_final, mF_final * 100,
            K, highConflict ? "(ВЫСОКИЙ КОНФЛИКТ!)" : "",
            BetP,
            delta, hasDeficit ? "(ДЕФИЦИТ!)" : "",
            recommendation
        );
    }
    
    // Геттеры
    public double getMTFinal() { return mT_final; }
    public double getMUFinal() { return mU_final; }
    public double getMFFinal() { return mF_final; }
    public double getK() { return K; }
    public double getBetP() { return BetP; }
    public double getDelta() { return delta; }
    public boolean isHighConflict() { return highConflict; }
    public boolean hasDeficit() { return hasDeficit; }
    public String getRecommendation() { return recommendation; }
}
