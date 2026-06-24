package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillVersion;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillVersionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalises raw work_skill.description strings into canonical skill entries.
 *
 * Three-pass pipeline per raw string:
 *   Pass 1 – process parenthesised content (extract versions, strip qualifiers).
 *   Pass 2 – split on separators that lie outside brackets ( / , | ).
 *   Pass 3 – clean each token, classify, extract version, map to skill_canonical.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillNormalizationService {

    private final WorkSkillRepository       workSkillRepository;
    private final SkillCanonicalRepository  canonicalRepository;
    private final SkillVersionRepository    versionRepository;

    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern VERSION_PATTERN      = Pattern.compile("\\b(\\d{1,2}(?:[./]\\d{1,3})*)\\s*(\\+)?\\b");
    private static final Pattern TECH_WORD_PATTERN    = Pattern.compile("[a-zA-Z]{2,}");
    private static final Pattern ARTIFACTS_PATTERN    = Pattern.compile(
            "^[+]?\\s*(основы|основ|базовые знания|знание|знания|опыт работы с|опыт)\\s+",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Set<String> QUALIFIER_KEYWORDS = Set.of(
            "junior", "middle", "senior", "lead", "core", "hooks", "native",
            "standard", "enterprise", "community", "professional",
            "express", "compact", "lite", "classic", "pro"
    );

    @Transactional
    public NormalizationReport normalizeAll() {
        List<WorkSkill> skills = workSkillRepository.findAll();
        log.info("Starting normalization of {} work_skill entries", skills.size());

        int processed = 0, created = 0, linked = 0, unknown = 0;
        int total = skills.size();
        int idx = 0;

        for (WorkSkill skill : skills) {
            idx++;
            if (skill.getCanonicalId() != null) {
                if (idx % 100 == 0)
                    log.info("Normalization progress: {}/{} (skipped — already linked)", idx, total);
                continue;
            }
            String raw = skill.getDescription();
            if (raw == null || raw.isBlank()) {
                continue;
            }

            List<ParsedSkill> parsed = parseRawSkill(raw);

            if (parsed.isEmpty()) {
                unknown++;
                continue;
            }

            SkillCanonical firstCanonical = null;
            for (ParsedSkill ps : parsed) {
                if ("unknown".equals(ps.techType)) {
                    unknown++;
                    continue;
                }
                SkillCanonical canonical = findOrCreateCanonical(ps);
                saveVersionIfNeeded(canonical, raw, ps);

                if (firstCanonical == null) {
                    firstCanonical = canonical;
                    created++;
                }
                linked++;
            }

            if (firstCanonical != null) {
                skill.setCanonicalId(firstCanonical.getId());
                workSkillRepository.save(skill);
            }
            processed++;

            if (idx % 100 == 0)
                log.info("Normalization progress: {}/{} | processed={}, canonical={}, linked={}, unknown={}",
                        idx, total, processed, created, linked, unknown);
        }

        log.info("Normalization DONE: {}/{} | processed={}, canonical_created={}, linked={}, unknown={}",
                total, total, processed, created, linked, unknown);
        return new NormalizationReport(processed, created, linked, unknown);
    }

    /**
     * Parses a single raw skill description into one or more ParsedSkill tokens.
     */
    public List<ParsedSkill> parseRawSkill(String raw) {
        String working = raw.trim();

        // Pass 1: handle parenthesised content
        working = processParentheses(working);

        // Pass 2: split on separators outside brackets
        List<String> tokens = splitOnSeparators(working);

        // Pass 3: clean and classify each token
        List<ParsedSkill> result = new ArrayList<>();
        for (String token : tokens) {
            ParsedSkill ps = classifyToken(token, raw);
            if (ps != null) {
                result.add(ps);
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pass 1: parentheses
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Inspects every (...) group:
     *  - If it contains digits that look like versions → strip the group (version extracted in pass 3)
     *  - If it contains only qualifier words → strip the group
     *  - Otherwise keep as-is
     */
    private String processParentheses(String input) {
        Matcher m = PARENTHESES_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String content = m.group(1).trim();
            if (looksLikeVersionSpec(content) || looksLikeQualifier(content)) {
                m.appendReplacement(sb, "");
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(sb);
        return sb.toString().replaceAll("\\s{2,}", " ").trim();
    }

    private boolean looksLikeVersionSpec(String content) {
        return VERSION_PATTERN.matcher(content).find()
                || content.matches(".*\\d.*")
                || content.toLowerCase().contains("and above")
                || content.toLowerCase().contains("and higher");
    }

    private boolean looksLikeQualifier(String content) {
        String lower = content.toLowerCase().trim();
        for (String kw : QUALIFIER_KEYWORDS) {
            if (lower.equals(kw) || lower.startsWith(kw + " ") || lower.endsWith(" " + kw)) {
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pass 2: split
    // ─────────────────────────────────────────────────────────────────────────

    private List<String> splitOnSeparators(String input) {
        String[] parts = input.split("\\s*[/|]\\s*|,\\s+");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result.isEmpty() ? List.of(input.trim()) : result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pass 3: classify token
    // ─────────────────────────────────────────────────────────────────────────

    private ParsedSkill classifyToken(String token, String originalRaw) {
        // Remove leading artifacts like "+основы ", "знание "
        String cleaned = ARTIFACTS_PATTERN.matcher(token).replaceFirst("").trim();
        if (cleaned.isEmpty()) return null;

        // Must contain at least one Latin word of 2+ chars to be a technology
        if (!TECH_WORD_PATTERN.matcher(cleaned).find()) {
            return new ParsedSkill(cleaned, cleaned.toLowerCase(), "unknown", null, null, null, false);
        }

        // Extract version from name
        VersionInfo vi = extractVersion(cleaned);
        String baseName = vi.baseName.trim().replaceAll("\\s{2,}", " ");
        if (baseName.isEmpty()) baseName = cleaned;

        String normalized = baseName.toLowerCase().replaceAll("[^a-z0-9.#+\\-]", " ").trim()
                .replaceAll("\\s{2,}", " ");

        String techType = guessTechType(baseName);
        String versionGroup = guessVersionGroup(baseName);

        return new ParsedSkill(baseName, normalized, techType, versionGroup,
                vi.versionMin, vi.versionMax, vi.isPlus);
    }

    private VersionInfo extractVersion(String input) {
        Matcher m = VERSION_PATTERN.matcher(input);
        String versionMin = null, versionMax = null;
        boolean isPlus = false;
        String baseName = input;

        if (m.find()) {
            versionMin = m.group(1);
            isPlus = "+".equals(m.group(2));
            baseName = (input.substring(0, m.start()) + input.substring(m.end())).trim();
            baseName = baseName.replaceAll("[\\-–]\\s*$", "").trim();

            // range: "6-8" → min=6, max=8 (already split by now, but check for dash-range)
            if (!isPlus) {
                Pattern rangeP = Pattern.compile("(\\d{1,2})\\s*[-–]\\s*(\\d{1,2})");
                Matcher rm = rangeP.matcher(input);
                if (rm.find()) {
                    versionMin = rm.group(1);
                    versionMax = rm.group(2);
                    baseName   = (input.substring(0, rm.start()) + input.substring(rm.end())).trim();
                }
            }
        }
        return new VersionInfo(baseName, versionMin, versionMax, isPlus);
    }

    private String guessTechType(String name) {
        String lower = name.toLowerCase();
        if (lower.matches(".*(spring|django|flask|fastapi|express|laravel|rails|angular|react|vue|next|nest|quarkus|micronaut|hibernate|mybatis|junit|pytest|selenium|playwright|jest|mocha|struts|tapestry|grails).*"))
            return "framework";
        if (lower.matches(".*(python|java|kotlin|scala|c#|c\\+\\+|go|golang|rust|ruby|php|swift|typescript|javascript|perl|haskell|elixir|erlang|lua|r\\b|matlab|fortran|cobol|pascal|delphi|groovy|dart).*"))
            return "language";
        if (lower.matches(".*(postgresql|mysql|oracle|mongodb|redis|elasticsearch|cassandra|clickhouse|mssql|sqlite|neo4j|dynamodb|hbase|influxdb).*"))
            return "db";
        if (lower.matches(".*(docker|kubernetes|k8s|helm|terraform|ansible|jenkins|gitlab|github actions|teamcity|bamboo|grafana|prometheus|kibana|logstash|zabbix|splunk).*"))
            return "tool";
        if (lower.matches(".*(aws|azure|gcp|yandex cloud|google cloud|heroku|digitalocean|vk cloud).*"))
            return "platform";
        return "library";
    }

    private String guessVersionGroup(String name) {
        String lower = name.toLowerCase();
        if (lower.startsWith(".net")) return ".NET";
        if (lower.startsWith("java ") || lower.equals("java")) return "Java";
        if (lower.startsWith("python")) return "Python";
        if (lower.startsWith("node")) return "Node.js";
        if (lower.startsWith("angular")) return "Angular";
        if (lower.startsWith("react")) return "React";
        if (lower.startsWith("spring")) return "Spring";
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Persistence helpers
    // ─────────────────────────────────────────────────────────────────────────

    private SkillCanonical findOrCreateCanonical(ParsedSkill ps) {
        return canonicalRepository.findByNormalizedName(ps.normalizedName)
                .orElseGet(() -> {
                    SkillCanonical sc = new SkillCanonical();
                    sc.setName(ps.name);
                    sc.setNormalizedName(ps.normalizedName);
                    sc.setTechType(ps.techType);
                    sc.setVersionGroup(ps.versionGroup);
                    return canonicalRepository.save(sc);
                });
    }

    private void saveVersionIfNeeded(SkillCanonical canonical, String rawString, ParsedSkill ps) {
        if (ps.versionMin == null && !ps.isPlus) return;
        if (versionRepository.existsByCanonicalAndRawString(canonical, rawString)) return;

        SkillVersion sv = new SkillVersion();
        sv.setCanonical(canonical);
        sv.setRawString(rawString);
        sv.setVersionMin(ps.versionMin);
        sv.setVersionMax(ps.versionMax);
        sv.setPlus(ps.isPlus);
        versionRepository.save(sv);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner types
    // ─────────────────────────────────────────────────────────────────────────

    public record ParsedSkill(
            String name,
            String normalizedName,
            String techType,
            String versionGroup,
            String versionMin,
            String versionMax,
            boolean isPlus
    ) {}

    private record VersionInfo(String baseName, String versionMin, String versionMax, boolean isPlus) {}

    public record NormalizationReport(int processed, int canonicalCreated, int linked, int unknown) {}
}
