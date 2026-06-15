package model;

/**
 * Данные от одного источника (вакансии, эксперты или прогнозы).
 *
 * Создание объектов:
 *   Вакансии  → new SourceData(total, relevant, avgScore, 1.0, weight, lambda)
 *   Эксперты  → new SourceData(total, relevant, avgScore, agreementLevel, weight, lambda)
 *   Прогнозы  → SourceData.mixedForecasts(total, growthN, growthConf, declineN, declineConf, weight, lambda)
 *
 * Прогнозы ВСЕГДА создаются через mixedForecasts — даже если все прогнозы одного знака.
 * Если growthN=0, то m(T)=0 (нет бычьих прогнозов).
 * Если declineN=0, то m(F)=0 (нет медвежьих прогнозов).
 * Если оба = 0 (нет прогнозов вообще), то kappa=0, m(T)=m(F)=0, m(U)=1.
 *
 * Эксперты дают сигнал через уровень score:
 *   score высокий (0.8-1.0) → высокий m(T) — "важно"
 *   score низкий  (0.0-0.3) → низкий  m(T) — "не важно"
 *   Конфликт между экспертами → низкая agreementLevel → низкий κ → меньше доверия.
 */
public class SourceData {
    // Исходные данные
    private int totalCount;           // Общее количество записей
    private int relevantCount;        // Количество записей про этот кластер
    private double averageScore;      // Средняя оценка (0-1)
    private double agreementLevel;    // Уровень согласованности (для экспертов, 0-1)
    private String direction;         // "positive", "decline" или "mixed" — только для прогнозов
    
    // Поля для смешанных прогнозов (direction="mixed")
    // Когда часть прогнозов говорит "рост", а часть — "спад"
    private int growthCount;          // Количество прогнозов роста
    private double avgGrowthScore;    // Средняя уверенность прогнозов роста
    private int declineCount;         // Количество прогнозов спада
    private double avgDeclineScore;   // Средняя уверенность прогнозов спада
    
    // Вычисленные параметры DST
    private double kappa;             // Коэффициент уверенности от объёма данных
    private double weight;            // Вес надёжности w (из статьи)
    private double lambda;            // Константа λ для вычисления κ
    
    // BPA - Базовое распределение масс (до дисконтирования)
    private double mT;                // Уверенность во включении
    private double mU;                // Неопределённость
    private double mF;                // Уверенность в исключении
    
    // BPA после дисконтирования
    private double mT_discounted;
    private double mU_discounted;
    private double mF_discounted;
    
    /**
     * Конструктор для вакансий и экспертов (direction всегда "positive").
     */
    public SourceData(int totalCount, int relevantCount, double averageScore,
                      double agreementLevel, double weight, double lambda) {
        this(totalCount, relevantCount, averageScore, agreementLevel, weight, lambda, "positive");
    }

    /**
     * Конструктор для прогнозов с явным direction.
     * direction = "positive" → масса идёт в m(T)
     * direction = "decline"  → масса идёт в m(F)
     */
    public SourceData(int totalCount, int relevantCount, double averageScore,
                      double agreementLevel, double weight, double lambda, String direction) {
        this.totalCount = totalCount;
        this.relevantCount = relevantCount;
        this.averageScore = averageScore;
        this.agreementLevel = agreementLevel;
        this.weight = weight;
        this.lambda = lambda;
        this.direction = direction;
        
        calculateKappa();
        calculateBPA();
        applyDiscounting();
    }

    /**
     * Фабричный метод для смешанных прогнозов (часть говорит «рост», часть «спад»).
     *
     * Формулы:
     *   n_eff = (growthCount + declineCount)   // согласованность=1.0 для прогнозов
     *   κ = n_eff / (n_eff + λ)
     *   m(T) = κ × avgGrowthScore × (growthCount / n_eff)
     *   m(F) = κ × avgDeclineScore × (declineCount / n_eff)
     *   m(U) = 1 − m(T) − m(F)
     *
     * @param totalForecasts  общее число прогнозов по этому кластеру (growth + decline)
     * @param growthCount     сколько прогнозируют рост
     * @param avgGrowthScore  средняя уверенность прогнозов роста (0-1)
     * @param declineCount    сколько прогнозируют спад
     * @param avgDeclineScore средняя уверенность прогнозов спада (0-1)
     * @param weight          вес надёжности источника
     * @param lambda          константа сглаживания
     */
    public static SourceData mixedForecasts(int totalForecasts,
                                            int growthCount, double avgGrowthScore,
                                            int declineCount, double avgDeclineScore,
                                            double weight, double lambda) {
        SourceData sd = new SourceData();
        sd.totalCount = totalForecasts;
        sd.relevantCount = growthCount + declineCount;
        sd.averageScore = 0;            // не используется для mixed
        sd.agreementLevel = 1.0;        // прогнозы не имеют agreementLevel
        sd.weight = weight;
        sd.lambda = lambda;
        sd.direction = "mixed";
        sd.growthCount = growthCount;
        sd.avgGrowthScore = avgGrowthScore;
        sd.declineCount = declineCount;
        sd.avgDeclineScore = avgDeclineScore;

        sd.calculateKappa();
        sd.calculateBPA();
        sd.applyDiscounting();
        return sd;
    }

    // Приватный конструктор для фабричного метода
    private SourceData() {}
    
    private void calculateKappa() {
        // Эффективный объём данных (с учётом согласованности для экспертов)
        double effectiveCount = relevantCount * agreementLevel;
        this.kappa = effectiveCount / (effectiveCount + lambda);
    }
    
    private void calculateBPA() {
        if ("mixed".equals(direction)) {
            // Смешанные прогнозы: часть говорит «рост», часть «спад»
            // Один κ на весь блок прогнозов (n_eff = growthCount + declineCount)
            // m(T) и m(F) вычисляются параллельно, пропорционально доле каждой группы
            int total = growthCount + declineCount;
            if (total == 0) {
                this.mT = 0; this.mF = 0; this.mU = 1; return;
            }
            double growthRatio  = (double) growthCount  / total;
            double declineRatio = (double) declineCount / total;

            this.mT = Math.max(0, Math.min(1, kappa * avgGrowthScore  * growthRatio));
            this.mF = Math.max(0, Math.min(1, kappa * avgDeclineScore * declineRatio));
        } else {
            // Доля записей про этот кластер
            double relevanceRatio = totalCount > 0 ? (double) relevantCount / totalCount : 0;

            // Базовая масса: κ × средняя оценка × доля релевантных
            double mass = Math.max(0, Math.min(1, kappa * averageScore * relevanceRatio));

            if ("decline".equals(direction)) {
                // Прогноз спада: масса идёт в m(F) — "кластер будет неважен"
                this.mT = 0;
                this.mF = mass;
            } else {
                // Вакансии, эксперты, прогнозы роста: масса идёт в m(T)
                this.mT = mass;
                this.mF = 0;
            }
        }

        // Неопределённость — всё, что не распределено
        this.mU = Math.max(0, 1 - this.mT - this.mF);
    }
    
    private void applyDiscounting() {
        // Дисконтирование масс на вес надёжности источника w:
        //   m'(T) = w × m(T)
        //   m'(F) = w × m(F)
        //   m'(U) = 1 - w×m(T) - w×m(F)  (неопределённость поглощает «скидку»)
        this.mT_discounted = weight * mT;
        this.mF_discounted = weight * mF;
        this.mU_discounted = 1.0 - this.mT_discounted - this.mF_discounted;
    }
    
    // Геттеры
    public double getKappa() { return kappa; }
    public double getWeight() { return weight; }
    public double getMT() { return mT; }
    public double getMU() { return mU; }
    public double getMF() { return mF; }
    public double getMTDiscounted() { return mT_discounted; }
    public double getMUDiscounted() { return mU_discounted; }
    public double getMFDiscounted() { return mF_discounted; }
    public int getTotalCount() { return totalCount; }
    public int getRelevantCount() { return relevantCount; }
    public double getAverageScore() { return averageScore; }
    public String getDirection() { return direction; }
    public int getGrowthCount() { return growthCount; }
    public int getDeclineCount() { return declineCount; }
    public double getAvgGrowthScore() { return avgGrowthScore; }
    public double getAvgDeclineScore() { return avgDeclineScore; }
    public double getAgreementLevel() { return agreementLevel; }
    public double getLambda() { return lambda; }
}
