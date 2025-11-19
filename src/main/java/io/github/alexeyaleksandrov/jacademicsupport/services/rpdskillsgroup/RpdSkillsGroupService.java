package io.github.alexeyaleksandrov.jacademicsupport.services.rpdskillsgroup;

import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.models.RpdSkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdSkillsGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RpdSkillsGroupService {

    private final RpdSkillsGroupRepository rpdSkillsGroupRepository;

    public List<RpdSkillsGroup> findAll() {
        return rpdSkillsGroupRepository.findAll();
    }

    public Optional<RpdSkillsGroup> findById(Long id) {
        return rpdSkillsGroupRepository.findById(id);
    }

    public RpdSkillsGroup save(RpdSkillsGroup rpdSkillsGroup) {
        return rpdSkillsGroupRepository.save(rpdSkillsGroup);
    }

    public void deleteById(Long id) {
        rpdSkillsGroupRepository.deleteById(id);
    }

    public List<RpdSkillsGroup> findByRpdId(Long rpdId) {
        return rpdSkillsGroupRepository.findByRpdId(rpdId);
    }

    public List<RpdSkillsGroup> findBySkillsGroup(SkillsGroup skillsGroup) {
        return rpdSkillsGroupRepository.findBySkillsGroup(skillsGroup);
    }

    public List<RpdSkillsGroup> findBySkillsGroupId(Long skillsGroupId) {
        return rpdSkillsGroupRepository.findBySkillsGroupId(skillsGroupId);
    }

    public boolean existsByRpdIdAndSkillsGroupId(Long rpdId, Long skillsGroupId) {
        return rpdSkillsGroupRepository.existsByRpdIdAndSkillsGroupId(rpdId, skillsGroupId);
    }

    public Optional<RpdSkillsGroup> findByRpdIdAndSkillsGroupId(Long rpdId, Long skillsGroupId) {
        return Optional.ofNullable(rpdSkillsGroupRepository.findByRpdIdAndSkillsGroupId(rpdId, skillsGroupId));
    }

    public List<RpdSkillsGroup> findByTimeGreaterThanEqual(Integer minTime) {
        return rpdSkillsGroupRepository.findByTimeGreaterThanEqual(minTime);
    }

    public List<RpdSkillsGroup> findByRpdIdAndTimeBetween(Long rpdId, Integer minTime, Integer maxTime) {
        return rpdSkillsGroupRepository.findByRpdIdAndTimeBetween(rpdId, minTime, maxTime);
    }

    public boolean existsById(Long id) {
        return rpdSkillsGroupRepository.existsById(id);
    }

    public List<RpdSkillsGroup> findAllById(List<Long> ids) {
        return rpdSkillsGroupRepository.findAllById(ids);
    }

    public List<RpdSkillsGroup> findByRpd(Rpd rpd) {
        return rpdSkillsGroupRepository.findByRpd(rpd);
    }

    public boolean existsByRpdAndSkillsGroup(Rpd rpd, SkillsGroup skillsGroup) {
        return rpdSkillsGroupRepository.existsByRpdAndSkillsGroup(rpd, skillsGroup);
    }

    public Optional<RpdSkillsGroup> findByRpdAndSkillsGroup(Rpd rpd, SkillsGroup skillsGroup) {
        return Optional.ofNullable(rpdSkillsGroupRepository.findByRpdAndSkillsGroup(rpd, skillsGroup));
    }
}
