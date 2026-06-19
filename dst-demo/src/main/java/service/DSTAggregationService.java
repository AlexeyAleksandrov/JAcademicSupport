package service;

import model.Cluster;
import model.DSTRResult;
import model.SourceData;

/**
 * Сервис для выполнения DST-агрегации данных
 * Реализация теории Демпстера-Шафера для агрегации разнородных источников
 */
public class DSTAggregationService {
    
    // Пороги для принятия решений (из статьи)
    private static final double TAU_DELTA = 0.15;   // Порог дефицита
    private static final double TAU_K = 0.4;         // Порог конфликта
    private static final double TAU_THETA = 0.15;    // Порог неопределённости

    // Количество кластеров — используется в формуле BetP
    private static final int N_CLUSTERS = 25;

    /** Включить пошаговую трассировку вычислений */
    private final boolean verbose;

    public DSTAggregationService() {
        this.verbose = true;
    }

    public DSTAggregationService(boolean verbose) {
        this.verbose = verbose;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Публичный метод агрегации
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Выполняет полную DST-агрегацию для кластера.
     * При verbose=true печатает каждый шаг по мере вычисления.
     */
    public DSTRResult aggregateCluster(Cluster cluster) {
        SourceData vacData  = cluster.getVacancyData();
        SourceData expData  = cluster.getExpertData();
        SourceData foreData = cluster.getForecastData();
        double supply = cluster.getCurrentRpdCoverage();

        // ── ШАГ 0: входные данные ────────────────────────────────────────────
        if (verbose) printInputTable(cluster);

        // ── ШАГ 1: BPA уже вычислен в SourceData при конструировании ─────────
        if (verbose) printBpaStep(vacData, expData, foreData);

        // ── ШАГ 2: дисконтирование уже применено в SourceData ────────────────
        if (verbose) printDiscountStep(vacData, expData, foreData);

        // ── ШАГ 3.1: комбинируем вакансии ⊕ эксперты ────────────────────────
        double[] m12 = combineAdaptive(
            vacData.getMTDiscounted(), vacData.getMUDiscounted(), vacData.getMFDiscounted(),
            expData.getMTDiscounted(), expData.getMUDiscounted(), expData.getMFDiscounted()
        );
        double K1 = m12[3];
        boolean usedYager1 = K1 >= TAU_K;
        if (verbose) printCombineStep(
            "ШАГ 3.1: Вакансии ⊕ Эксперты",
            vacData.getMTDiscounted(), vacData.getMUDiscounted(), vacData.getMFDiscounted(),
            expData.getMTDiscounted(), expData.getMUDiscounted(), expData.getMFDiscounted(),
            m12, K1, usedYager1,
            "Согласие источников усиливает уверенность (m(T) растёт, m(U) падает)"
        );

        // ── ШАГ 3.2: комбинируем результат ⊕ прогнозы ────────────────────────
        double[] mFinal = combineAdaptive(
            m12[0], m12[1], m12[2],
            foreData.getMTDiscounted(), foreData.getMUDiscounted(), foreData.getMFDiscounted()
        );
        double K2 = mFinal[3];
        boolean usedYager2 = K2 >= TAU_K;
        if (verbose) printCombineStep(
            "ШАГ 3.2: (Вак⊕Эксп) ⊕ Прогнозы",
            m12[0], m12[1], m12[2],
            foreData.getMTDiscounted(), foreData.getMUDiscounted(), foreData.getMFDiscounted(),
            mFinal, K2, usedYager2,
            "Прогнозы корректируют итоговое распределение масс"
        );

        double mT_final = mFinal[0];
        double mU_final = mFinal[1];
        double mF_final = mFinal[2];
        double K = Math.max(K1, K2);   // итоговый конфликт — максимум из двух шагов

        // ── ШАГ 4: итоговые показатели ───────────────────────────────────────
        double Bel  = mT_final;
        double Pl   = 1.0 - mF_final;
        double BetP = mT_final + (mU_final / N_CLUSTERS);
        double delta = BetP - supply;
        if (verbose) printFinalMetrics(mT_final, mU_final, mF_final, K1, K2, K, Bel, Pl, BetP, supply, delta);

        // ── ШАГ 5: решение ───────────────────────────────────────────────────
        String recommendation = generateRecommendation(mT_final, mU_final, mF_final, K, delta);
        String level = determineRecommendationLevel(mT_final, mU_final, mF_final, K, delta);
        if (verbose) printDecision(delta, K, mU_final, recommendation);

        return new DSTRResult(
            mT_final, mU_final, mF_final,
            vacData.getMTDiscounted(), expData.getMTDiscounted(), foreData.getMTDiscounted(),
            m12[0], K, Bel, Pl, BetP,
            delta, recommendation, level
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Вычисления DST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Комбинирует два источника по правилу Демпстера.
     * @return массив [mT, mU, mF, K]
     */
    private double[] combineDST(double m1T, double m1U, double m1F,
                                 double m2T, double m2U, double m2F) {
        double K    = m1T * m2F + m1F * m2T;
        double norm = 1.0 - K;
        if (norm < 0.0001) norm = 0.0001;

        double mT = (m1T * m2T + m1T * m2U + m1U * m2T) / norm;
        double mU = (m1U * m2U) / norm;
        double mF = (m1F * m2F + m1F * m2U + m1U * m2F) / norm;

        double sum = mT + mU + mF;
        if (sum > 0) { mT /= sum; mU /= sum; mF /= sum; }

        return new double[]{mT, mU, mF, K};
    }

    /**
     * Комбинирует два источника по правилу Ягера (при высоком конфликте).
     * Конфликтная масса K уходит в неопределённость m(U) — без нормировки.
     * Честно отражает противоречие источников, не скрывая его за нормой.
     * @return массив [mT, mU, mF, K]
     */
    private double[] combineYager(double m1T, double m1U, double m1F,
                                   double m2T, double m2U, double m2F) {
        double K  = m1T * m2F + m1F * m2T;

        double mT = m1T * m2T + m1T * m2U + m1U * m2T;
        double mU = m1U * m2U + K;   // конфликт переходит в неопределённость
        double mF = m1F * m2F + m1F * m2U + m1U * m2F;

        double sum = mT + mU + mF;
        if (sum > 0) { mT /= sum; mU /= sum; mF /= sum; }

        return new double[]{mT, mU, mF, K};
    }

    /**
     * Адаптивное комбинирование: Демпстер при K < τK, Ягер при K ≥ τK.
     * @return массив [mT, mU, mF, K]
     */
    private double[] combineAdaptive(double m1T, double m1U, double m1F,
                                      double m2T, double m2U, double m2F) {
        double K = m1T * m2F + m1F * m2T;
        return K >= TAU_K
            ? combineYager(m1T, m1U, m1F, m2T, m2U, m2F)
            : combineDST(m1T, m1U, m1F, m2T, m2U, m2F);
    }

    private String generateRecommendation(double mT, double mU, double mF, double K, double delta) {
        StringBuilder rec = new StringBuilder();
        if (K >= TAU_K) {
            rec.append("⚡ Применено правило Ягера (K=").append(f2(K)).append(" ≥ τK=").append(f2(TAU_K)).append("). ");
            rec.append("⚠️ ВЫСОКИЙ КОНФЛИКТ — источники противоречат друг другу. ");
        }
        if (mU > TAU_THETA) {
            rec.append("📊 Высокая неопределённость (m(U)=").append(f2(mU)).append("). ");
        }
        if (delta > TAU_DELTA) {
            if (K <= TAU_K && mU <= TAU_THETA) {
                rec.append(delta > 0.5
                    ? "🔴 УСИЛИТЬ ЗНАЧИТЕЛЬНО (+50-100% часов)"
                    : "🟡 УСИЛИТЬ УМЕРЕННО (+20-50% часов)");
            } else {
                rec.append("🔵 ВАРИАТИВНОСТЬ + ЭКСПЕРТИЗА (высокий риск)");
            }
        } else if (delta < -TAU_DELTA) {
            rec.append("🟢 СОКРАТИТЬ ИЛИ ИСКЛЮЧИТЬ (избыток в РПД)");
        } else {
            rec.append("✅ СОХРАНИТЬ (дефицита нет)");
        }
        return rec.toString();
    }

    private String determineRecommendationLevel(double mT, double mU, double mF, double K, double delta) {
        if (mF > 0.8 && mT < 0.1)                            return "exclude";
        if (delta > TAU_DELTA && K <= TAU_K && mU <= TAU_THETA) return delta > 0.5 ? "strong" : "moderate";
        if (delta < -TAU_DELTA)                               return "reduce";
        if (K > TAU_K || mU > TAU_THETA)                      return "expertise";
        return "preserve";
    }

    public boolean canProceedToLevel2(DSTRResult result) {
        return result.hasDeficit() && !result.isHighConflict() && result.getMUFinal() <= TAU_THETA;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verbose-вывод
    // ─────────────────────────────────────────────────────────────────────────

    private void printInputTable(Cluster cluster) {
        SourceData vac  = cluster.getVacancyData();
        SourceData exp  = cluster.getExpertData();
        SourceData fore = cluster.getForecastData();
        int supplyPct   = (int) Math.round(cluster.getCurrentRpdCoverage() * 100);

        p("");
        p("📥 ШАГ 0: ВХОДНЫЕ ДАННЫЕ");
        sep('-');
        p(String.format("  Кластер: %-20s | Покрытие в РПД: %d%% (%d ч.)",
            cluster.getName(), supplyPct, cluster.getTotalRpdHours()));
        p("");
        p("  Источник   │ Всего │ По кластеру │  Ср. оценка  │ Согласованность │  w   │  λ");
        p("  ───────────┼───────┼─────────────┼──────────────┼─────────────────┼──────┼────");
        p(String.format("  Вакансии   │%6d │%12d │%13.3f │%16.3f │%.2f  │%3.0f",
            vac.getTotalCount(), vac.getRelevantCount(), vac.getAverageScore(), vac.getAgreementLevel(),
            vac.getWeight(), vac.getLambda()));
        p(String.format("  Эксперты   │%6d │%12d │%13.3f │%16.3f │%.2f  │%3.0f",
            exp.getTotalCount(), exp.getRelevantCount(), exp.getAverageScore(), exp.getAgreementLevel(),
            exp.getWeight(), exp.getLambda()));
        if ("mixed".equals(fore.getDirection())) {
            p(String.format("  Прогнозы   │%6d │  %dр + %dс (mixed) │ р=%.2f / с=%.2f │       —         │%.2f  │%3.0f",
                fore.getTotalCount(), fore.getGrowthCount(), fore.getDeclineCount(),
                fore.getAvgGrowthScore(), fore.getAvgDeclineScore(),
                fore.getWeight(), fore.getLambda()));
        } else {
            p(String.format("  Прогнозы   │%6d │%12d │%13.3f │%16.3f │%.2f  │%3.0f",
                fore.getTotalCount(), fore.getRelevantCount(), fore.getAverageScore(), fore.getAgreementLevel(),
                fore.getWeight(), fore.getLambda()));
        }
    }

    private void printBpaStep(SourceData vac, SourceData exp, SourceData fore) {
        p("");
        p("📐 ШАГ 1: ВЫЧИСЛЕНИЕ BPA (Базовое Распределение Масс)");
        sep('-');
        p("  Формула: n_eff = relevant × agreement;  κ = n_eff / (n_eff + λ)");
        p("           m(T) = κ × avgScore × (relevant/total)   [для вакансий/экспертов]");
        p("           m(T) = κ × avgGrowthScore × (growth/total)  [для прогнозов, mixed]");
        p("           m(U) = 1 − m(T) − m(F)");
        p("");

        printBpaSource("Вакансии", vac);
        printBpaSource("Эксперты", exp);
        printBpaSourceForecast(fore);
    }

    private void printBpaSource(String name, SourceData sd) {
        double nEff = sd.getRelevantCount() * sd.getAgreementLevel();
        p(String.format("  [%s]", name));
        p(String.format("    n_eff = %d × %.3f = %.3f%s",
            sd.getRelevantCount(), sd.getAgreementLevel(), nEff,
            sd.getAgreementLevel() < 1.0 ? "  ← согласованность уменьшила объём!" : ""));
        p(String.format("    κ = %.3f / (%.3f + %.0f) = %.4f%s",
            nEff, nEff, sd.getLambda(), sd.getKappa(),
            sd.getKappa() > 0.8 ? "  ← высокое доверие (много данных)" :
            sd.getKappa() < 0.3 ? "  ← низкое доверие (мало данных!)" : ""));
        double relRatio = sd.getTotalCount() > 0 ? (double) sd.getRelevantCount() / sd.getTotalCount() : 0;
        p(String.format("    relRatio = %d / %d = %.4f",
            sd.getRelevantCount(), sd.getTotalCount(), relRatio));
        p(String.format("    m(T) = %.4f × %.3f × %.4f = %.4f",
            sd.getKappa(), sd.getAverageScore(), relRatio, sd.getMT()));
        p(String.format("    m(U) = %.4f,  m(F) = %.4f", sd.getMU(), sd.getMF()));
        p("");
    }

    private void printBpaSourceForecast(SourceData fore) {
        p("  [Прогнозы]");
        if ("mixed".equals(fore.getDirection())) {
            int total = fore.getGrowthCount() + fore.getDeclineCount();
            p(String.format("    Тип: СМЕШАННЫЕ (%dр рост + %dс спад = %d всего)",
                fore.getGrowthCount(), fore.getDeclineCount(), total));
            p(String.format("    n_eff = %d + %d = %d  (agreementLevel=1.0 для прогнозов)",
                fore.getGrowthCount(), fore.getDeclineCount(), total));
            p(String.format("    κ = %d / (%d + %.0f) = %.4f",
                total, total, fore.getLambda(), fore.getKappa()));
            if (total > 0) {
                p(String.format("    m(T) = κ × avgGrowth × (growth/total) = %.4f × %.3f × (%d/%d) = %.4f  ← сигнал роста",
                    fore.getKappa(), fore.getAvgGrowthScore(), fore.getGrowthCount(), total, fore.getMT()));
                p(String.format("    m(F) = κ × avgDecline × (decline/total) = %.4f × %.3f × (%d/%d) = %.4f  ← сигнал спада",
                    fore.getKappa(), fore.getAvgDeclineScore(), fore.getDeclineCount(), total, fore.getMF()));
            } else {
                p("    ⚠️  Нет прогнозов (growth=0, decline=0) → κ=0, m(T)=0, m(F)=0");
            }
        } else {
            double nEff = fore.getRelevantCount() * fore.getAgreementLevel();
            p(String.format("    Тип: %s (direction=%s)", fore.getDirection().toUpperCase(), fore.getDirection()));
            p(String.format("    n_eff = %d × 1.000 = %.3f", fore.getRelevantCount(), nEff));
            p(String.format("    κ = %.3f / (%.3f + %.0f) = %.4f", nEff, nEff, fore.getLambda(), fore.getKappa()));
            p(String.format("    m(T) = %.4f,  m(U) = %.4f,  m(F) = %.4f",
                fore.getMT(), fore.getMU(), fore.getMF()));
        }
        p(String.format("    m(U) = %.4f", fore.getMU()));
        p("");
    }

    private void printDiscountStep(SourceData vac, SourceData exp, SourceData fore) {
        p("📐 ШАГ 2: ДИСКОНТИРОВАНИЕ (учёт надёжности источников)");
        sep('-');
        p("  Формула: m'(T) = w × m(T);  m'(F) = w × m(F);  m'(U) = 1 − m'(T) − m'(F)");
        p("  Смысл: часть уверенности «штрафуется» за возможные ошибки в данных источника");
        p("");
        printDiscountSource("Вакансии", vac);
        printDiscountSource("Эксперты", exp);
        printDiscountSource("Прогнозы", fore);
    }

    private void printDiscountSource(String name, SourceData sd) {
        double wasted = (1.0 - sd.getWeight()) * sd.getMT() * 100;
        p(String.format("  [%s]  w = %.2f  (штраф за ненадёжность: %s)",
            name, sd.getWeight(), interpretWeight(sd.getWeight())));
        p(String.format("    m'(T) = %.2f × %.4f = %.4f  (−%.1f%% ушло в неопределённость)",
            sd.getWeight(), sd.getMT(), sd.getMTDiscounted(), wasted));
        p(String.format("    m'(F) = %.2f × %.4f = %.4f",
            sd.getWeight(), sd.getMF(), sd.getMFDiscounted()));
        p(String.format("    m'(U) = 1 − %.4f − %.4f = %.4f",
            sd.getMTDiscounted(), sd.getMFDiscounted(), sd.getMUDiscounted()));
        p("");
    }

    private String interpretWeight(double w) {
        if (w >= 0.9)  return "высокая надёжность";
        if (w >= 0.75) return "хорошая надёжность";
        if (w >= 0.6)  return "умеренная надёжность";
        return "низкая надёжность";
    }

    private void printCombineStep(String title,
                                   double m1T, double m1U, double m1F,
                                   double m2T, double m2U, double m2F,
                                   double[] result, double K, boolean usedYager, String comment) {
        String ruleLabel = usedYager ? "  [⚡ ПРАВИЛО ЯГЕРА]" : "";
        p("📐 " + title + ruleLabel);
        sep('-');
        p(String.format("  Вход 1:  m(T)=%.4f  m(U)=%.4f  m(F)=%.4f", m1T, m1U, m1F));
        p(String.format("  Вход 2:  m(T)=%.4f  m(U)=%.4f  m(F)=%.4f", m2T, m2U, m2F));
        p("");
        p("  Конфликт K = m1(T)×m2(F) + m1(F)×m2(T)");
        p(String.format("           K = %.4f×%.4f + %.4f×%.4f = %.4f",
            m1T, m2F, m1F, m2T, K));
        if (usedYager) {
            p(String.format("  🔴 K = %.4f ≥ τK = %.2f  → ПРАВИЛО ЯГЕРА: конфликт уходит в неопределённость", K, TAU_K));
            p("");
            double rawT = m1T * m2T + m1T * m2U + m1U * m2T;
            double rawU = m1U * m2U;
            double rawF = m1F * m2F + m1F * m2U + m1U * m2F;
            p("  Сырые массы (без нормировки, как у Демпстера):");
            p("  raw(T) = m1(T)×m2(T) + m1(T)×m2(U) + m1(U)×m2(T)");
            p(String.format("         = %.4f×%.4f + %.4f×%.4f + %.4f×%.4f = %.4f",
                m1T, m2T, m1T, m2U, m1U, m2T, rawT));
            p("  raw(U) = m1(U)×m2(U)");
            p(String.format("         = %.4f×%.4f = %.4f", m1U, m2U, rawU));
            p("  raw(F) = m1(F)×m2(F) + m1(F)×m2(U) + m1(U)×m2(F)");
            p(String.format("         = %.4f×%.4f + %.4f×%.4f + %.4f×%.4f = %.4f",
                m1F, m2F, m1F, m2U, m1U, m2F, rawF));
            p("");
            p("  ⚡ Ягер: K не нормирует — конфликт честно переходит в m(U):");
            p(String.format("  m_Y(T) = raw(T)           = %.4f", rawT));
            p(String.format("  m_Y(U) = raw(U) + K = %.4f + %.4f = %.4f  ← конфликт здесь!", rawU, K, rawU + K));
            p(String.format("  m_Y(F) = raw(F)           = %.4f", rawF));
        } else {
            p(String.format("  ✅  K = %.4f ≤ τK = %.2f  → применяем правило Демпстера", K, TAU_K));
            p("");
            double norm = 1.0 - K;
            p(String.format("  Нормировка: 1 − K = %.4f", norm));
            p("  m_out(T) = [m1(T)×m2(T) + m1(T)×m2(U) + m1(U)×m2(T)] / (1−K)");
            p(String.format("           = [%.4f×%.4f + %.4f×%.4f + %.4f×%.4f] / %.4f",
                m1T, m2T, m1T, m2U, m1U, m2T, norm));
            p(String.format("           = %.4f", result[0]));
            p("  m_out(U) = [m1(U)×m2(U)] / (1−K)");
            p(String.format("           = [%.4f×%.4f] / %.4f = %.4f", m1U, m2U, norm, result[1]));
            p("  m_out(F) = [m1(F)×m2(F) + m1(F)×m2(U) + m1(U)×m2(F)] / (1−K)");
            p(String.format("           = [%.4f×%.4f + %.4f×%.4f + %.4f×%.4f] / %.4f",
                m1F, m2F, m1F, m2U, m1U, m2F, norm));
            p(String.format("           = %.4f", result[2]));
        }
        p("");
        p(String.format("  Итог: m(T)=%.4f  m(U)=%.4f  m(F)=%.4f  (сумма=%.4f)",
            result[0], result[1], result[2], result[0]+result[1]+result[2]));
        p("  📌 " + comment);
        p("");
    }

    private void printFinalMetrics(double mT, double mU, double mF,
                                    double K1, double K2, double K,
                                    double Bel, double Pl, double BetP,
                                    double supply, double delta) {
        p("📊 ШАГ 4: ИТОГОВЫЕ ПОКАЗАТЕЛИ");
        sep('-');
        p(String.format("  Финальное BPA:  m*(T) = %.4f  m*(U) = %.4f  m*(F) = %.4f",
            mT, mU, mF));
        p(String.format("  Итоговый конфликт K = max(K1=%.4f, K2=%.4f) = %.4f", K1, K2, K));
        p("");
        p("  Функция доверия:      Bel(T) = m*(T) = " + f4(Bel));
        p("    → гарантированная поддержка (минимум того, насколько кластер важен)");
        p("  Функция правдоподобия: Pl(T) = 1 − m*(F) = 1 − " + f4(mF) + " = " + f4(Pl));
        p("    → потенциальная поддержка (максимум возможной важности)");
        p("");
        p(String.format("  BetP = m*(T) + m*(U)/%d", N_CLUSTERS));
        p(String.format("       = %.4f + %.4f/%d = %.4f + %.4f = %.4f",
            mT, mU, N_CLUSTERS, mT, mU / N_CLUSTERS, BetP));
        p("    → вероятностная мера: неопределённость равномерно распределяется по всем кластерам");
        p("");
        p(String.format("  supply (покрытие в РПД) = %.4f  (%.0f%%)", supply, supply * 100));
        p(String.format("  Δ = BetP − supply = %.4f − %.4f = %.4f", BetP, supply, delta));
        if (delta > 0) {
            p(String.format("    → Рынок/эксперты требуют %.0f%%, в РПД только %.0f%% — ДЕФИЦИТ %.0f пп.",
                BetP * 100, supply * 100, delta * 100));
        } else if (delta < 0) {
            p(String.format("    → В РПД %.0f%%, требуется %.0f%% — ИЗБЫТОК %.0f пп.",
                supply * 100, BetP * 100, Math.abs(delta) * 100));
        } else {
            p("    → Баланс между требованиями рынка и покрытием РПД.");
        }
        p("");
    }

    private void printDecision(double delta, double K, double mU, String recommendation) {
        p("🎯 ШАГ 5: ПРИНЯТИЕ РЕШЕНИЯ");
        sep('=');
        p("  Проверяем 3 условия:");
        p("");
        String d1 = delta > TAU_DELTA ? "✅ ДА — есть дефицит" : "✗ НЕТ — дефицита нет";
        String d2 = K <= TAU_K       ? "✅ ДА — конфликт в норме" : "✗ НЕТ — высокий конфликт!";
        String d3 = mU <= TAU_THETA  ? "✅ ДА — неопределённость в норме" : "✗ НЕТ — высокая неопределённость!";
        p(String.format("  1. Δ = %.4f > τΔ = %.2f?  %s", delta, TAU_DELTA, d1));
        p(String.format("  2. K = %.4f ≤ τK = %.2f?  %s", K, TAU_K, d2));
        p(String.format("  3. m*(U) = %.4f ≤ τΘ = %.2f?  %s", mU, TAU_THETA, d3));
        p("");
        boolean hasDeficit   = delta > TAU_DELTA;
        boolean lowConflict  = K <= TAU_K;
        boolean lowUncertain = mU <= TAU_THETA;
        if (hasDeficit && lowConflict && lowUncertain) {
            p("  → Все 3 условия ✅ → ЖЁСТКОЕ УПРАВЛЯЮЩЕЕ ВОЗДЕЙСТВИЕ");
        } else if (hasDeficit) {
            p("  → Дефицит есть, НО данные ненадёжны → ОГРАНИЧЕННОЕ РЕШЕНИЕ");
        } else if (delta < -TAU_DELTA) {
            p("  → Δ < 0 и |Δ| > τΔ → ИЗБЫТОК в РПД, можно сокращать");
        } else {
            p("  → Дефицита нет → СТАБИЛИЗАЦИЯ");
        }
        p("");
        p("  ══ РЕКОМЕНДАЦИЯ ══════════════════════════════");
        p("  " + recommendation);
        sep('═');
        p("");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Вспомогательные методы форматирования
    // ─────────────────────────────────────────────────────────────────────────

    private void p(String line) { System.out.println(line); }

    private void sep(char ch) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < 60; i++) sb.append(ch);
        System.out.println(sb.toString());
    }

    private String f2(double v) { return String.format("%.2f", v); }
    private String f4(double v) { return String.format("%.4f", v); }
}
