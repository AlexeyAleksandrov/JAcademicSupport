import model.Cluster;
import model.SourceData;
import service.DSTAggregationService;

/**
 * Демонстрационное приложение с 16 примерами DST-агрегации.
 * Примеры 1-10: учебные сценарии с подобранными данными.
 * Примеры 11-16: реальные данные вакансий из БД JAcademicSupport
 *   (13 479 вакансий, HH API), остальные параметры — обоснованные предположения.
 *
 * Весь пошаговый вывод выполняется внутри DSTAggregationService (verbose=true).
 * Каждый runExampleN() только задаёт входные данные и вызывает сервис.
 *
 * Реальные данные вакансий по кластерам (из SELECT на БД, дата: 08.06.2026):
 *   Фреймворки        : 7125 / 13479
 *   Реляционные БД    : 7077 / 13479
 *   Python            : 4385 / 13479
 *   Linux             : 3897 / 13479
 *   Тестирование      : 2845 / 13479
 *   CI/CD             : 2715 / 13479
 *   Java              : 3131 / 13479
 *   Golang            : 866  / 13479
 *   C++               : 578  / 13479
 *   1С                : 191  / 13479
 *
 * averageScore для вакансий: 0.75-0.85 (экспертное предположение; реальных
 *   весов важности в БД нет — только бинарный факт упоминания навыка).
 * supply: предположение на основе типичной CS-программы университета.
 */
public class DemoApplication {
    
    private static final DSTAggregationService dstService = new DSTAggregationService();

    private static final String LINE =
        "\n╔══════════════════════════════════════════════════════════════╗";
    private static final String LINE_END =
        "╚══════════════════════════════════════════════════════════════╝";

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     DST-агрегация: 16 примеров (10 учебных + 6 реальных)    ║");
        System.out.println("║  Теория Демпстера-Шафера для обновления образовательных      ║");
        System.out.println("║  программ на основе данных рынка труда и экспертных оценок  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        sectionHeader("ЧАСТЬ 1: УЧЕБНЫЕ ПРИМЕРЫ (подобранные данные)");
        runExample1();
        runExample2();
        runExample3();
        runExample4();
        runExample5();
        runExample6();
        runExample7();
        runExample8();
        runExample9();
        runExample10();

        sectionHeader("ЧАСТЬ 2: РЕАЛЬНЫЕ ДАННЫЕ ВАКАНСИЙ (БД JAcademicSupport, 13 479 вакансий HH API)");
        runExample11();
        runExample12();
        runExample13();
        runExample14();
        runExample15();
        runExample16();
    }

    private static void sectionHeader(String title) {
        System.out.println();
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  %-60s║%n", title);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println();
    }

    private static void header(int n, String title, String cluster, String situation) {
        System.out.println(LINE);
        System.out.printf("║  ПРИМЕР %2d: %-50s║%n", n, title);
        System.out.printf("║  Кластер:  %-50s║%n", cluster);
        System.out.printf("║  Ситуация: %-50s║%n", situation);
        System.out.println(LINE_END);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 1: Идеальный сценарий — все источники согласны (Python)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample1() {
        header(1, "Идеальный сценарий — все источники согласны",
               "Python", "Рынок требует, эксперты подтверждают, прогнозы позитивны");

        SourceData vacData  = new SourceData(1000, 800, 0.9, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 15, 0.95, 0.9, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 4, 0.88, 1, 0.45, 0.6, 2);

        Cluster cluster = new Cluster(1L, "Python", "Язык программирования",
            vacData, expData, foreData, 0.25, 100, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 2: Конфликт источников — рынок vs эксперты (Blockchain)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample2() {
        header(2, "Конфликт источников — рынок vs эксперты",
               "Blockchain", "Вакансии говорят 'важно', эксперты говорят 'не важно'");

        SourceData vacData  = new SourceData(800, 150, 0.85, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 10, 0.15, 0.6, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 1, 0.50, 4, 0.75, 0.6, 2);

        Cluster cluster = new Cluster(2L, "Blockchain", "Блокчейн-технологии",
            vacData, expData, foreData, 0.05, 20, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 3: Недостаток данных (Rust)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample3() {
        header(3, "Недостаток данных",
               "Rust", "Мало вакансий, мало экспертов, нет прогнозов");

        SourceData vacData  = new SourceData(1000, 15, 0.9, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 2, 0.8, 0.7, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(0, 0, 0.0, 0, 0.0, 0.6, 2);

        Cluster cluster = new Cluster(3L, "Rust", "Системное программирование",
            vacData, expData, foreData, 0.02, 8, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 4: Кластер уже хорошо покрыт в РПД (Java)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample4() {
        header(4, "Кластер уже хорошо покрыт в РПД",
               "Java", "Высокая востребованность, но в РПД уже много часов");

        SourceData vacData  = new SourceData(1000, 450, 0.88, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 18, 0.92, 0.95, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 3, 0.80, 1, 0.50, 0.6, 2);

        Cluster cluster = new Cluster(4L, "Java", "Язык программирования",
            vacData, expData, foreData, 0.55, 220, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 5: Дефицит — нужно усилить (AI/ML)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample5() {
        header(5, "Дефицит — нужно усилить",
               "AI/ML", "Рынок активно требует AI, но в РПД почти нет часов");

        SourceData vacData  = new SourceData(1000, 320, 0.92, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 16, 0.88, 0.9, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 4, 0.95, 1, 0.40, 0.6, 2);

        Cluster cluster = new Cluster(5L, "AI/ML", "Искусственный интеллект",
            vacData, expData, foreData, 0.08, 32, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 6: Устаревающая технология (Flash/ActionScript)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample6() {
        header(6, "Устаревающая технология",
               "Flash/ActionScript", "Мало вакансий, эксперты против, все прогнозы — спад");

        SourceData vacData  = new SourceData(1000, 8, 0.20, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 5, 0.10, 0.8, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 0, 0.0, 5, 0.85, 0.6, 2);

        Cluster cluster = new Cluster(6L, "Flash/ActionScript", "Устаревшая технология",
            vacData, expData, foreData, 0.12, 48, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 7: Смешанные прогнозы (WebAssembly)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample7() {
        header(7, "Смешанные прогнозы (часть 'рост', часть 'спад')",
               "WebAssembly", "3 аналитика предсказывают рост, 2 — спад технологии");

        SourceData vacData  = new SourceData(1000, 25, 0.40, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 8, 0.55, 0.65, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 3, 0.80, 2, 0.70, 0.6, 2);

        Cluster cluster = new Cluster(7L, "WebAssembly", "Веб-технология будущего",
            vacData, expData, foreData, 0.00, 0, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 8: Надёжные эксперты, мало вакансий (Cybersecurity)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample8() {
        header(8, "Надёжные эксперты, мало вакансий",
               "Cybersecurity", "Вакансии закрытые, но эксперты высокого уровня");

        SourceData vacData  = new SourceData(1000, 45, 0.85, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(12, 10, 0.95, 0.95, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 3, 0.80, 0, 0.0, 0.6, 2);

        Cluster cluster = new Cluster(8L, "Cybersecurity", "Информационная безопасность",
            vacData, expData, foreData, 0.10, 40, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 9: Разногласие внутри источников (DevOps)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample9() {
        header(9, "Разногласие внутри источников",
               "DevOps", "Много вакансий, но эксперты сильно разошлись во мнениях");

        SourceData vacData  = new SourceData(1000, 280, 0.82, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 12, 0.60, 0.4, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 3, 0.75, 2, 0.60, 0.6, 2);

        Cluster cluster = new Cluster(9L, "DevOps", "DevOps практики",
            vacData, expData, foreData, 0.15, 60, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 10: Граничный случай — точно на пороге (Mobile Development)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample10() {
        header(10, "Граничный случай — точно на пороге",
               "Mobile Development", "Δ ≈ τΔ=0.15, K ≈ τK=0.4 — оба у границы");

        SourceData vacData  = new SourceData(1000, 180, 0.75, 1.0, 0.8, 15);
        SourceData expData  = new SourceData(20, 11, 0.72, 0.85, 0.9, 5);
        SourceData foreData = SourceData.mixedForecasts(5, 2, 0.65, 0, 0.0, 0.6, 2);

        Cluster cluster = new Cluster(10L, "Mobile Development", "Разработка мобильных приложений",
            vacData, expData, foreData, 0.35, 140, null);

        dstService.aggregateCluster(cluster);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ЧАСТЬ 2: РЕАЛЬНЫЕ ДАННЫЕ ВАКАНСИЙ (БД JAcademicSupport)
    // totalCount = 13 479 — реальное число вакансий в БД на дату 08.06.2026
    // relevantCount — реальное число вакансий с навыками из кластера
    // averageScore  — предположение 0.75–0.85 (в БД нет весов важности навыка,
    //                 только бинарный факт упоминания)
    // Эксперты/Прогнозы/Supply — обоснованные предположения
    // ═════════════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 11: Python — 4 385 вакансий, умеренное покрытие в РПД
    //   Ожидаем: высокий BetP, дефицит → УСИЛИТЬ
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample11() {
        header(11, "[РЕАЛЬНЫЕ ДАННЫЕ] Python — дефицит, нужна экспертиза",
               "Python",
               "4 385/13 479 вак. (32.5%); 2 сем. в РПД → supply=30% | ожидаем: ВАРИАТИВНОСТЬ");

        // VAC: реальные данные HH — 4385 вакансий упоминают Python из 13479
        SourceData vacData  = new SourceData(13479, 4385, 0.80, 1.0, 0.80, 15);

        // EXP: синтетические — 25 опросов работодателей, 20 включили Python
        // agreement=0.82: небольшой разброс оценок важности (0.7–1.0)
        // λ=3: умеренная скорость насыщения уверенности
        SourceData expData  = new SourceData(25, 20, 0.86, 0.82, 0.90, 3);

        // FC: 6 прогнозов — 4 роста (AI/ML), 2 умеренного снижения (насыщение рынка)
        SourceData foreData = SourceData.mixedForecasts(6, 4, 0.87, 2, 0.40, 0.60, 2);

        // supply=0.30: 2 семестра Python (чистый + ИИ) + 1 сем. FastAPI в РПД
        Cluster cluster = new Cluster(11L, "Python",
            "Язык программирования Python [реальные данные HH]",
            vacData, expData, foreData, 0.30, 120, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 12: CI/CD — 2 715 вакансий, слабое покрытие в РПД
    //   Ожидаем: дефицит → УСИЛИТЬ (DevOps недооценён в учебных планах)
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample12() {
        header(12, "[РЕАЛЬНЫЕ ДАННЫЕ] CI/CD — острый дефицит, усилить!",
               "CI/CD",
               "2 715/13 479 вак. (20.1%); 1 сем. в РПД → supply=12% | ожидаем: УСИЛИТЬ");

        // VAC: реальные данные HH — 2715 вакансий требуют CI/CD навыков
        SourceData vacData  = new SourceData(13479, 2715, 0.82, 1.0, 0.80, 15);

        // EXP: синтетические — 30 опросов, 27 упомянули CI/CD как обязательное
        // agreement=0.92: высокий консенсус среди работодателей
        // λ=1: быстрое насыщение — каждое мнение весомо (высококалиберные эксперты)
        SourceData expData  = new SourceData(30, 27, 0.90, 0.92, 0.90, 1);

        // FC: 8 прогнозов — все однозначно рост (DevOps/GitOps/Platform Engineering)
        SourceData foreData = SourceData.mixedForecasts(8, 8, 0.92, 0, 0.0, 0.60, 1);

        // supply=0.12: 1 семестр CI/CD в 3 курсе РПД
        Cluster cluster = new Cluster(12L, "CI/CD",
            "Непрерывная интеграция и доставка [реальные данные HH]",
            vacData, expData, foreData, 0.12, 48, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 13: Реляционные БД — 7 077 вакансий, хорошее покрытие
    //   Ожидаем: низкий или нулевой дефицит → СТАБИЛИЗАЦИЯ
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample13() {
        header(13, "[РЕАЛЬНЫЕ ДАННЫЕ] Реляционные БД — небольшой дефицит",
               "Реляционные БД",
               "7 077/13 479 вак. (52.5%); 2 сем. в РПД → supply=40% | ожидаем: ВАРИАТИВНОСТЬ");

        // VAC: реальные данные HH — 7077 вакансий, самый массовый кластер
        SourceData vacData  = new SourceData(13479, 7077, 0.72, 1.0, 0.80, 15);

        // EXP: синтетические — 20 опросов, 16 включили SQL/БД в требования
        // agreement=0.85: умеренно высокая согласованность, зрелая тема
        // λ=3: стандартное насыщение
        SourceData expData  = new SourceData(20, 16, 0.78, 0.85, 0.90, 3);

        // FC: 5 прогнозов — 2 умеренного роста (облачные БД), 3 снижения (NoSQL/NewSQL)
        SourceData foreData = SourceData.mixedForecasts(5, 2, 0.65, 3, 0.55, 0.60, 2);

        // supply=0.40: 2 семестра реляционных БД в РПД (2 курс)
        Cluster cluster = new Cluster(13L, "Реляционные БД",
            "SQL, PostgreSQL, MySQL и др. [реальные данные HH]",
            vacData, expData, foreData, 0.40, 160, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 14: Java — 3 131 вакансия, хорошее покрытие, зрелая технология
    //   Ожидаем: дефицит на грани / стабилизация
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample14() {
        header(14, "[РЕАЛЬНЫЕ ДАННЫЕ] Java — нет в РПД, но рынок требует!",
               "Java",
               "3 131/13 479 вак. (23.2%); Java ОТСУТСТВУЕТ в РПД → supply=2% | ожидаем: УСИЛИТЬ");

        // VAC: реальные данные HH — 3131 вакансия требуют Java
        SourceData vacData  = new SourceData(13479, 3131, 0.82, 1.0, 0.80, 15);

        // EXP: синтетические — 30 опросов, 28 назвали Java ключевым
        // agreement=0.93: корпоративный рынок единодушен в оценке Java
        // λ=1: быстрое насыщение — каждый опрошенный работодатель весом
        SourceData expData  = new SourceData(30, 28, 0.92, 0.93, 0.90, 1);

        // FC: 8 прогнозов — все рост (корпоративный сектор, стабильный спрос)
        SourceData foreData = SourceData.mixedForecasts(8, 8, 0.92, 0, 0.0, 0.60, 1);

        // supply=0.02: Java отсутствует в РПД (есть C++, Python, Go, C# — Java нет)
        Cluster cluster = new Cluster(14L, "Java",
            "Язык программирования Java [реальные данные HH]",
            vacData, expData, foreData, 0.02, 8, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 15: Golang — 866 вакансий, растёт, почти не в РПД
    //   Ожидаем: малый BetP из-за малого числа вакансий → стабилизация / вариативность
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample15() {
        header(15, "[РЕАЛЬНЫЕ ДАННЫЕ] Golang — малый рынок, есть 1 сем.",
               "Golang",
               "866/13 479 вак. (6.4%); 1 сем. Backend Go в РПД → supply=12% | ожидаем: ВАРИАТИВНОСТЬ");

        // VAC: реальные данные HH — 866 вакансий требуют Go-разработчика
        SourceData vacData  = new SourceData(13479, 866, 0.83, 1.0, 0.80, 15);

        // EXP: синтетические — 15 опрошенных, только 6 назвали Go (tech-компании)
        // agreement=0.72: мнения расходятся — одни за, другие скептичны
        // λ=3: небольшое число опрошенных → низкое доверие
        SourceData expData  = new SourceData(15, 6, 0.72, 0.72, 0.90, 3);

        // FC: 5 прогнозов — 4 роста (cloud-native), 1 осторожность
        SourceData foreData = SourceData.mixedForecasts(5, 4, 0.80, 1, 0.50, 0.60, 2);

        // supply=0.12: 1 семестр Backend Go в РПД (2 курс)
        Cluster cluster = new Cluster(15L, "Golang",
            "Язык программирования Go [реальные данные HH]",
            vacData, expData, foreData, 0.12, 48, null);

        dstService.aggregateCluster(cluster);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ПРИМЕР 16: 1С — 191 вакансия, узкая ниша, почти нет в программе
    //   Ожидаем: малый рыночный сигнал → стабилизация
    // ─────────────────────────────────────────────────────────────────────────
    private static void runExample16() {
        header(16, "[РЕАЛЬНЫЕ ДАННЫЕ] 1С — ниша, BetP ниже supply",
               "1С",
               "191/13 479 вак. (1.4%); 1 сем. в РПД → supply=15% | ожидаем: СТАБИЛИЗАЦИЯ");

        // VAC: реальные данные HH — 191 вакансия, преимущественно бухгалтерия/ERP
        SourceData vacData  = new SourceData(13479, 191, 0.88, 1.0, 0.80, 15);

        // EXP: синтетические — 3 из 12 опрошенных упомянули 1С (1С-ориентированные компании)
        // agreement=0.65: мнения расходятся
        SourceData expData  = new SourceData(12, 3, 0.60, 0.65, 0.90, 5);

        // FC: 1 рост (импортозамещение), 2 снижения (узкая ниша)
        SourceData foreData = SourceData.mixedForecasts(3, 1, 0.50, 2, 0.60, 0.60, 2);

        // supply=0.15: 1 семестр 1С в РПД (3 курс)
        // BetP ≈ 0.13 < supply=0.15 → дефицита нет, стабилизация
        Cluster cluster = new Cluster(16L, "1С",
            "Платформа 1С:Предприятие [реальные данные HH]",
            vacData, expData, foreData, 0.15, 60, null);

        dstService.aggregateCluster(cluster);
    }
}
