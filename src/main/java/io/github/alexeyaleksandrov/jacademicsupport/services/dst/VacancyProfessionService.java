package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.Profession;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyProfession;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ProfessionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyProfessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Classifies vacancies into one or more professions using keyword-matching on vacancy title.
 *
 * A vacancy can match multiple professions simultaneously with individual confidence scores
 * (e.g. "Full-Stack Developer" → backend:0.7, frontend:0.7).
 * Vacancies with no matches are assigned to the synthetic "other" profession for later analysis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyProfessionService {

    private final VacancyEntityRepository     vacancyRepository;
    private final ProfessionRepository        professionRepository;
    private final VacancyProfessionRepository vacancyProfessionRepository;

    private static final String OTHER_CODE = "other";

    /**
     * Shared confidence for a vacancy that matches keywords of multiple professions.
     * Configurable — reduce for strict single-profession assignment.
     */
    private static final BigDecimal SHARED_CONFIDENCE  = new BigDecimal("0.70");
    private static final BigDecimal EXACT_CONFIDENCE   = BigDecimal.ONE;
    private static final BigDecimal OTHER_CONFIDENCE   = BigDecimal.ONE;

    @Transactional
    public ClassificationReport classifyAll() {
        List<VacancyEntity> vacancies  = vacancyRepository.findAll();
        List<Profession>    professions = professionRepository.findAll();

        ensureOtherProfessionExists();

        log.info("Classifying {} vacancies into {} professions", vacancies.size(), professions.size());

        int classified = 0, skipped = 0, other = 0;
        int total = vacancies.size(), idx = 0;

        for (VacancyEntity vacancy : vacancies) {
            idx++;
            String title = vacancy.getName();
            if (title == null || title.isBlank()) {
                skipped++;
                continue;
            }

            Map<Profession, BigDecimal> matches = matchProfessions(title.toLowerCase(), professions);

            if (matches.isEmpty()) {
                assignProfession(vacancy, getOrCreateOtherProfession(), OTHER_CONFIDENCE);
                other++;
            } else {
                for (Map.Entry<Profession, BigDecimal> entry : matches.entrySet()) {
                    assignProfession(vacancy, entry.getKey(), entry.getValue());
                }
                classified++;
            }

            if (idx % 200 == 0)
                log.info("Classification progress: {}/{} | classified={}, other={}, skipped={}",
                        idx, total, classified, other, skipped);
        }

        log.info("Classification DONE: {}/{} | classified={}, other={}, skipped={}",
                total, total, classified, other, skipped);
        return new ClassificationReport(classified, other, skipped);
    }

    /**
     * Matches a lowercase vacancy title against all profession keyword lists.
     * Returns a map of matched professions → confidence.
     * If exactly one match: confidence = 1.0.
     * If multiple matches: confidence = SHARED_CONFIDENCE per match.
     */
    private Map<Profession, BigDecimal> matchProfessions(String titleLower, List<Profession> professions) {
        Map<Profession, BigDecimal> hits = new LinkedHashMap<>();

        for (Profession prof : professions) {
            if (OTHER_CODE.equals(prof.getCode())) continue;
            if (prof.getKeywords() == null) continue;

            String[] keywords = prof.getKeywords().split(",");
            for (String kw : keywords) {
                String keyword = kw.trim().toLowerCase();
                if (!keyword.isEmpty() && titleLower.contains(keyword)) {
                    hits.put(prof, null);
                    break;
                }
            }
        }

        if (hits.isEmpty()) return hits;

        BigDecimal confidence = hits.size() == 1 ? EXACT_CONFIDENCE : SHARED_CONFIDENCE;
        hits.replaceAll((p, v) -> confidence);
        return hits;
    }

    private void assignProfession(VacancyEntity vacancy, Profession profession, BigDecimal confidence) {
        if (vacancyProfessionRepository.existsByVacancyAndProfession(vacancy, profession)) return;

        VacancyProfession vp = new VacancyProfession();
        vp.setVacancy(vacancy);
        vp.setProfession(profession);
        vp.setConfidence(confidence);
        vacancyProfessionRepository.save(vp);
    }

    private void ensureOtherProfessionExists() {
        if (!professionRepository.existsByCode(OTHER_CODE)) {
            Profession other = new Profession();
            other.setCode(OTHER_CODE);
            other.setName("Other / Unclassified");
            other.setDescription("Vacancies that did not match any known profession keywords");
            other.setKeywords("");
            professionRepository.save(other);
        }
    }

    private Profession getOrCreateOtherProfession() {
        return professionRepository.findByCode(OTHER_CODE)
                .orElseGet(() -> {
                    ensureOtherProfessionExists();
                    return professionRepository.findByCode(OTHER_CODE).orElseThrow();
                });
    }

    public record ClassificationReport(int classified, int other, int skipped) {}
}
