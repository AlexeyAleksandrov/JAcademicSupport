package model;

/**
 * Навык внутри кластера
 * Например: в кластере "Python" навыки: "Django", "NumPy", "Pandas"
 */
public class Skill {
    private Long id;
    private String name;
    private int hoursInRpd;           // Количество часов в РПД
    private double importanceScore;   // Важность навыка (0-1)
    
    // Данные от источников на уровне навыка
    private SourceData vacancyData;
    private SourceData expertData;
    private SourceData forecastData;
    
    public Skill(Long id, String name, int hoursInRpd, double importanceScore,
                 SourceData vacancyData, SourceData expertData, SourceData forecastData) {
        this.id = id;
        this.name = name;
        this.hoursInRpd = hoursInRpd;
        this.importanceScore = importanceScore;
        this.vacancyData = vacancyData;
        this.expertData = expertData;
        this.forecastData = forecastData;
    }
    
    // Геттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getHoursInRpd() { return hoursInRpd; }
    public double getImportanceScore() { return importanceScore; }
    public SourceData getVacancyData() { return vacancyData; }
    public SourceData getExpertData() { return expertData; }
    public SourceData getForecastData() { return forecastData; }
}
