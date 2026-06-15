package model;

import java.util.List;

/**
 * Кластер компетенций (Группа технологий)
 * Например: "Python", "Java", "AI/ML", "Blockchain"
 */
public class Cluster {
    private Long id;
    private String name;
    private String description;
    
    // Данные по источникам
    private SourceData vacancyData;      // Данные из вакансий
    private SourceData expertData;       // Данные от экспертов
    private SourceData forecastData;     // Данные из прогнозов
    
    // Текущее покрытие в РПД (supply)
    private double currentRpdCoverage;   // Доля часов в учебной программе (0-1)
    private int totalRpdHours;           // Общее количество часов по кластеру
    
    // Список навыков в кластере (для уровня 2)
    private List<Skill> skills;
    
    public Cluster(Long id, String name, String description,
                   SourceData vacancyData, SourceData expertData, SourceData forecastData,
                   double currentRpdCoverage, int totalRpdHours, List<Skill> skills) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.vacancyData = vacancyData;
        this.expertData = expertData;
        this.forecastData = forecastData;
        this.currentRpdCoverage = currentRpdCoverage;
        this.totalRpdHours = totalRpdHours;
        this.skills = skills;
    }
    
    // Геттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SourceData getVacancyData() { return vacancyData; }
    public SourceData getExpertData() { return expertData; }
    public SourceData getForecastData() { return forecastData; }
    public double getCurrentRpdCoverage() { return currentRpdCoverage; }
    public int getTotalRpdHours() { return totalRpdHours; }
    public List<Skill> getSkills() { return skills; }
}
