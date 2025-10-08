package io.github.alexeyaleksandrov.jacademicsupport.services.rpdskill;

import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.models.RpdSkill;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RpdSkillService {

    private final RpdSkillRepository rpdSkillRepository;

    public List<RpdSkill> findAll() {
        return rpdSkillRepository.findAll();
    }

    public Optional<RpdSkill> findById(Long id) {
        return rpdSkillRepository.findById(id);
    }

    public RpdSkill save(RpdSkill rpdSkill) {
        return rpdSkillRepository.save(rpdSkill);
    }

    public void deleteById(Long id) {
        rpdSkillRepository.deleteById(id);
    }

    public List<RpdSkill> findByRpdId(Long rpdId) {
        return rpdSkillRepository.findByRpdId(rpdId);
    }

    public List<RpdSkill> findByWorkSkill(WorkSkill workSkill) {
        return rpdSkillRepository.findByWorkSkill(workSkill);
    }

    public List<RpdSkill> findByWorkSkillId(Long workSkillId) {
        return rpdSkillRepository.findByWorkSkillId(workSkillId);
    }

    public boolean existsByRpdIdAndWorkSkillId(Long rpdId, Long workSkillId) {
        return rpdSkillRepository.existsByRpdIdAndWorkSkillId(rpdId, workSkillId);
    }

    public Optional<RpdSkill> findByRpdIdAndWorkSkillId(Long rpdId, Long workSkillId) {
        return Optional.ofNullable(rpdSkillRepository.findByRpdIdAndWorkSkillId(rpdId, workSkillId));
    }

    public List<RpdSkill> findByTimeGreaterThanEqual(Integer minTime) {
        return rpdSkillRepository.findByTimeGreaterThanEqual(minTime);
    }

    public List<RpdSkill> findByRpdIdAndTimeBetween(Long rpdId, Integer minTime, Integer maxTime) {
        return rpdSkillRepository.findByRpdIdAndTimeBetween(rpdId, minTime, maxTime);
    }

    public boolean existsById(Long id) {
        return rpdSkillRepository.existsById(id);
    }

    public List<RpdSkill> findAllById(List<Long> ids) {
        return rpdSkillRepository.findAllById(ids);
    }

    public List<RpdSkill> findByRpd(Rpd rpd) {
        return rpdSkillRepository.findByRpd(rpd);
    }

    public boolean existsByRpdAndWorkSkill(Rpd rpd, WorkSkill workSkill) {
        return rpdSkillRepository.existsByRpdAndWorkSkill(rpd, workSkill);
    }

    public Optional<RpdSkill> findByRpdAndWorkSkill(Rpd rpd, WorkSkill workSkill) {
        return Optional.ofNullable(rpdSkillRepository.findByRpdAndWorkSkill(rpd, workSkill));
    }
}
