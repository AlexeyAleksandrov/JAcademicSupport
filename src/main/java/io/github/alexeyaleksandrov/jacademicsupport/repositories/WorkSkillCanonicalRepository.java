package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkillCanonical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkSkillCanonicalRepository
        extends JpaRepository<WorkSkillCanonical, WorkSkillCanonical.WorkSkillCanonicalId> {

    List<WorkSkillCanonical> findByWorkSkillId(Long workSkillId);

    List<WorkSkillCanonical> findByCanonicalId(Long canonicalId);

    List<WorkSkillCanonical> findByWorkSkillIdIn(List<Long> workSkillIds);

    @Query("""
            SELECT wsc.canonicalId
            FROM WorkSkillCanonical wsc
            WHERE wsc.workSkillId IN :workSkillIds
            """)
    List<Long> findCanonicalIdsByWorkSkillIds(@Param("workSkillIds") List<Long> workSkillIds);
}
