package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DisciplineCoverageTreeDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.Discipline;
import io.github.alexeyaleksandrov.jacademicsupport.models.DisciplineCoverage;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineCoverageRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DstHierarchyTest {
    private SkillCanonicalRepository skillRepository;
    private DstDisciplineTree tree;
    private Discipline discipline;

    @BeforeEach
    void setUp() {
        skillRepository = mock(SkillCanonicalRepository.class);
        tree = new DstDisciplineTree(mock(DisciplineRepository.class),
                mock(DisciplineCoverageRepository.class), skillRepository);
        discipline = new Discipline();
        discipline.setId(1L);
        discipline.setName("Тестовая дисциплина");
        discipline.setTotalHours(180);
    }

    @Test
    void derivesFamilyAndDomainFromSkill() {
        when(skillRepository.findAllById(anySet())).thenReturn(List.of(skill(1L, "PostgreSQL", "DATABASE", "Реляционные БД")));
        when(skillRepository.findDomainsByTechFamilies(anySet())).thenReturn(List.<Object[]>of(new Object[]{"Нереляционные БД", "DATABASE"}));

        DisciplineCoverageTreeDto result = tree.build(discipline, List.of(
                coverage("BACKEND", null, null, 80),
                coverage("FRONTEND", null, null, 60),
                coverage(null, "Нереляционные БД", null, 20),
                coverage(null, null, 1L, 40)));

        DisciplineCoverageTreeDto.Node database = result.getDomains().stream()
                .filter(node -> "DATABASE".equals(node.getLabel())).findFirst().orElseThrow();
        assertTrue(database.isImplicit());
        assertEquals(60, database.getTotalHours());
        assertEquals(40, database.getChildren().stream()
                .filter(node -> "Реляционные БД".equals(node.getLabel())).findFirst().orElseThrow().getTotalHours());
        assertEquals(200, result.getAllocatedHours());
        assertEquals(20, result.getExcessHours());
    }

    @Test
    void keepsExplicitParentAsAuthoritativeBudget() {
        when(skillRepository.findAllById(anySet())).thenReturn(List.of(skill(1L, "PostgreSQL", "DATABASE", "Реляционные БД")));
        when(skillRepository.findDomainsByTechFamilies(anySet())).thenReturn(List.of());
        DisciplineCoverageTreeDto result = tree.build(discipline, List.of(
                coverage("DATABASE", null, null, 80), coverage(null, null, 1L, 40)));
        DisciplineCoverageTreeDto.Node database = result.getDomains().get(0);
        assertEquals(80, database.getExplicitHours());
        assertEquals(0, database.getImplicitHours());
        assertEquals(80, database.getTotalHours());
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void doesNotDoubleCountExplicitDomainAndFamily() {
        when(skillRepository.findAllById(anySet())).thenReturn(List.of(skill(1L, "React", "FRONTEND", "JavaScript")));
        when(skillRepository.findDomainsByTechFamilies(anySet())).thenReturn(List.<Object[]>of(new Object[]{"JavaScript", "FRONTEND"}));
        DisciplineCoverageTreeDto result = tree.build(discipline, List.of(
                coverage("FRONTEND", null, null, 180),
                coverage(null, "JavaScript", null, 140),
                coverage(null, null, 1L, 36)));
        DisciplineCoverageTreeDto.Node frontend = result.getDomains().get(0);
        assertEquals(180, frontend.getTotalHours());
        assertEquals(140, frontend.getChildren().get(0).getTotalHours());
        assertEquals(180, result.getAllocatedHours());
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void rejectsChildrenThatExceedExplicitParent() {
        when(skillRepository.findAllById(Set.of())).thenReturn(List.of());
        when(skillRepository.findDomainsByTechFamilies(anySet())).thenReturn(List.<Object[]>of(new Object[]{"Java", "BACKEND"}));
        DisciplineCoverageTreeDto result = tree.build(discipline, List.of(
                coverage("BACKEND", null, null, 50), coverage(null, "Java", null, 60)));
        assertFalse(result.getViolations().isEmpty());
        assertThrows(DstHierarchyViolationException.class, () -> tree.validate(result));
    }

    @Test
    void buildsSkillWithoutCanonicalFamily() {
        when(skillRepository.findAllById(anySet())).thenReturn(List.of(skill(1L, "Алгоритмы", "GENERAL", null)));
        DisciplineCoverageTreeDto result = tree.build(discipline, List.of(coverage(null, null, 1L, 108)));
        assertEquals("GENERAL", result.getDomains().get(0).getLabel());
        assertEquals("Прочее", result.getDomains().get(0).getChildren().get(0).getLabel());
    }

    @Test
    void parsesLegacyModesIntoNewTreeMode() {
        assertEquals(DstCalcOptions.TreeMode.FULL_TREE,
                new DstCalcOptions(DstCalcOptions.CoverageMode.DERIVED, DstCalcOptions.CoverageMode.DERIVED,
                        DstCalcOptions.CoverageMode.DERIVED, DstCalcOptions.HoursBase.CURRICULUM,
                        DstCalcOptions.BudgetMode.INDEPENDENT, null, null).treeMode());
        assertEquals(DstCalcOptions.TreeMode.EXPLICIT_ONLY,
                new DstCalcOptions(DstCalcOptions.CoverageMode.EXPLICIT, DstCalcOptions.CoverageMode.EXPLICIT,
                        DstCalcOptions.CoverageMode.EXPLICIT, DstCalcOptions.HoursBase.CURRICULUM,
                        DstCalcOptions.BudgetMode.INDEPENDENT, null, null).treeMode());
    }

    private DisciplineCoverage coverage(String domain, String family, Long skillId, int hours) {
        DisciplineCoverage row = new DisciplineCoverage();
        row.setDisciplineId(1L); row.setDomain(domain); row.setTechFamily(family);
        row.setCanonicalId(skillId); row.setHours(hours); return row;
    }

    private SkillCanonical skill(Long id, String name, String domain, String family) {
        SkillCanonical skill = new SkillCanonical();
        skill.setId(id); skill.setName(name); skill.setNormalizedName(name.toLowerCase());
        skill.setDomain(domain); skill.setTechFamily(family); return skill;
    }
}
