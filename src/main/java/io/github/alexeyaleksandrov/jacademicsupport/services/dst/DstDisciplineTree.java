package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DisciplineCoverageTreeDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.Discipline;
import io.github.alexeyaleksandrov.jacademicsupport.models.DisciplineCoverage;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineCoverageRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DstDisciplineTree {
    private final DisciplineRepository disciplineRepository;
    private final DisciplineCoverageRepository coverageRepository;
    private final SkillCanonicalRepository skillCanonicalRepository;

    @Transactional(readOnly = true)
    public DisciplineCoverageTreeDto build(Long disciplineId) {
        Discipline discipline = disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена: " + disciplineId));
        return build(discipline, coverageRepository.findByDisciplineId(disciplineId));
    }

    public DisciplineCoverageTreeDto build(Discipline discipline, List<DisciplineCoverage> coverage) {
        Set<Long> ids = coverage.stream().map(DisciplineCoverage::getCanonicalId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SkillCanonical> skills = skillCanonicalRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(SkillCanonical::getId, Function.identity()));
        Set<String> families = coverage.stream().map(DisciplineCoverage::getTechFamily)
                .filter(this::present).collect(Collectors.toSet());
        skills.values().stream().map(SkillCanonical::getTechFamily).filter(this::present).forEach(families::add);
        Map<String, String> familyDomains = families.isEmpty() ? Map.of() :
                skillCanonicalRepository.findDomainsByTechFamilies(families).stream()
                        .collect(Collectors.toMap(r -> (String) r[0], r -> (String) r[1], (a, b) -> a));

        Map<String, DomainAccumulator> domains = new LinkedHashMap<>();
        for (DisciplineCoverage row : coverage) {
            int hours = Math.max(0, row.getHours() == null ? 0 : row.getHours());
            SkillCanonical skill = row.getCanonicalId() == null ? null : skills.get(row.getCanonicalId());
            String familyDomain = present(row.getTechFamily()) ? familyDomains.get(row.getTechFamily()) : null;
            String domain = first(row.getDomain(), skill == null ? null : skill.getDomain(), familyDomain);
            if (!present(domain)) continue;
            DomainAccumulator d = domains.computeIfAbsent(domain, DomainAccumulator::new);
            if (row.getCanonicalId() != null) {
                String family = first(row.getTechFamily(), skill == null ? null : skill.getTechFamily(), "Прочее");
                FamilyAccumulator f = d.families.computeIfAbsent(family, FamilyAccumulator::new);
                f.skills.computeIfAbsent(row.getCanonicalId(), id -> new SkillAccumulator(id,
                        skill == null ? "Навык #" + id : skill.getName())).hours += hours;
            } else if (present(row.getTechFamily())) {
                d.families.computeIfAbsent(row.getTechFamily(), FamilyAccumulator::new).explicitHours += hours;
            } else {
                d.explicitHours += hours;
            }
        }

        DisciplineCoverageTreeDto result = new DisciplineCoverageTreeDto();
        result.setDisciplineId(discipline.getId());
        result.setDisciplineName(discipline.getName());
        result.setTotalHours(Math.max(0, discipline.getTotalHours() == null ? 0 : discipline.getTotalHours()));
        for (DomainAccumulator domain : domains.values()) result.getDomains().add(toDomain(domain, result));
        int allocated = result.getDomains().stream().mapToInt(DisciplineCoverageTreeDto.Node::getTotalHours).sum();
        result.setAllocatedHours(allocated);
        result.setUnallocatedHours(Math.max(0, result.getTotalHours() - allocated));
        result.setExcessHours(Math.max(0, allocated - result.getTotalHours()));
        if (allocated > result.getTotalHours()) addViolation(result, null, "DISCIPLINE", discipline.getName(),
                result.getTotalHours(), allocated);
        return result;
    }

    public void validate(DisciplineCoverageTreeDto tree) {
        if (!tree.getViolations().isEmpty()) throw new DstHierarchyViolationException(tree.getViolations());
    }

    private DisciplineCoverageTreeDto.Node toDomain(DomainAccumulator value, DisciplineCoverageTreeDto tree) {
        DisciplineCoverageTreeDto.Node node = node(value.name, value.name, "DOMAIN", null, value.explicitHours);
        for (FamilyAccumulator family : value.families.values()) node.getChildren().add(toFamily(family, tree, value.name));
        node.setChildrenSum(node.getChildren().stream().mapToInt(DisciplineCoverageTreeDto.Node::getTotalHours).sum());
        finish(node, "семейств/навыков");
        if (node.getExplicitHours() > 0 && node.getChildrenSum() > node.getExplicitHours())
            addViolation(tree, node, "DOMAIN", value.name, node.getExplicitHours(), node.getChildrenSum());
        return node;
    }

    private DisciplineCoverageTreeDto.Node toFamily(FamilyAccumulator value, DisciplineCoverageTreeDto tree, String domain) {
        DisciplineCoverageTreeDto.Node node = node(domain + "/" + value.name, value.name, "FAMILY", null, value.explicitHours);
        for (SkillAccumulator skill : value.skills.values()) {
            DisciplineCoverageTreeDto.Node child = node(String.valueOf(skill.id), skill.name, "SKILL", skill.id, skill.hours);
            child.setTotalHours(skill.hours);
            node.getChildren().add(child);
        }
        node.setChildrenSum(node.getChildren().stream().mapToInt(DisciplineCoverageTreeDto.Node::getTotalHours).sum());
        finish(node, "навыков");
        if (node.getExplicitHours() > 0 && node.getChildrenSum() > node.getExplicitHours())
            addViolation(tree, node, "FAMILY", domain + " / " + value.name,
                    node.getExplicitHours(), node.getChildrenSum());
        return node;
    }

    private DisciplineCoverageTreeDto.Node node(String key, String label, String level, Long id, int explicitHours) {
        DisciplineCoverageTreeDto.Node node = new DisciplineCoverageTreeDto.Node();
        node.setKey(key); node.setLabel(label); node.setLevel(level); node.setCanonicalId(id);
        node.setExplicitHours(explicitHours); node.setImplicit(explicitHours == 0); return node;
    }

    private void finish(DisciplineCoverageTreeDto.Node node, String source) {
        boolean derived = node.getExplicitHours() == 0;
        node.setImplicitHours(derived ? node.getChildrenSum() : 0);
        node.setTotalHours(derived ? node.getChildrenSum() : node.getExplicitHours());
        node.setDerivedFrom(derived && node.getImplicitHours() > 0 ? source : null);
    }

    private void addViolation(DisciplineCoverageTreeDto tree, DisciplineCoverageTreeDto.Node node,
                              String level, String parent, int parentHours, int childrenSum) {
        DisciplineCoverageTreeDto.Violation v = new DisciplineCoverageTreeDto.Violation();
        v.setDisciplineId(tree.getDisciplineId()); v.setDisciplineName(tree.getDisciplineName());
        v.setLevel(level); v.setParent(parent); v.setParentHours(parentHours); v.setChildrenSum(childrenSum);
        v.setExcess(childrenSum - parentHours);
        v.setMessage(parent + ": дочерние элементы превышают доступные часы на " + v.getExcess() + " ч.");
        tree.getViolations().add(v); if (node != null) node.setViolation(v);
    }

    private boolean present(String value) { return value != null && !value.isBlank(); }
    private String first(String... values) { return Arrays.stream(values).filter(this::present).findFirst().orElse(null); }

    private static class DomainAccumulator { final String name; int explicitHours; final Map<String, FamilyAccumulator> families = new LinkedHashMap<>(); DomainAccumulator(String name) { this.name = name; } }
    private static class FamilyAccumulator { final String name; int explicitHours; final Map<Long, SkillAccumulator> skills = new LinkedHashMap<>(); FamilyAccumulator(String name) { this.name = name; } }
    private static class SkillAccumulator { final long id; final String name; int hours; SkillAccumulator(long id, String name) { this.id = id; this.name = name; } }
}
