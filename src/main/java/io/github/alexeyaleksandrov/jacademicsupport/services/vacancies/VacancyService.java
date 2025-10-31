package io.github.alexeyaleksandrov.jacademicsupport.services.vacancies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.gigachat.GigaChatService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillService;
import io.github.alexeyaleksandrov.jacademicsupport.utils.OffsetBasedPageRequest;
import io.github.alexeyaleksandrov.jacademicsupport.utils.ResourceFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyService {

    private final VacancyEntityRepository vacancyRepository;
    private final GigaChatService gigaChatService;
    private final WorkSkillService workSkillService;
    private final ObjectMapper objectMapper;
    private final ResourceFileReader resourceFileReader;

    public List<VacancyEntity> findAll() {
        return vacancyRepository.findAll();
    }

    public Optional<VacancyEntity> findById(Long id) {
        return vacancyRepository.findById(id);
    }

    public Optional<VacancyEntity> findByHhId(Long hhId) {
        return Optional.ofNullable(vacancyRepository.findByHhId(hhId));
    }

    public VacancyEntity save(VacancyEntity vacancy) {
        return vacancyRepository.save(vacancy);
    }

    public void deleteById(Long id) {
        vacancyRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return vacancyRepository.existsById(id);
    }

    public Page<VacancyEntity> findAllPaginated(int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        return vacancyRepository.findAll(pageable);
    }

    public long count() {
        return vacancyRepository.count();
    }

    @Transactional
    public void processVacancySkillsWithGigaChat() {
        try {
            // Load system prompt from resources
            String systemPrompt = resourceFileReader.readResourceFile("prompts/vacancies_work_skill_extract_system_prompt.txt");
            log.info("System prompt loaded successfully");
            
            // Get first 5 vacancies that have at least one skill
            List<VacancyEntity> allVacancies = vacancyRepository.findAll();
            List<VacancyEntity> vacanciesToProcess = allVacancies.stream()
                    .filter(v -> v.getSkills() != null && !v.getSkills().isEmpty())
                    .limit(5)
                    .toList();
            
            log.info("Found {} vacancies to process", vacanciesToProcess.size());
            
            for (VacancyEntity vacancy : vacanciesToProcess) {
                log.info("Processing vacancy ID: {}, Name: {}", vacancy.getId(), vacancy.getName());
                processVacancySkills(vacancy, systemPrompt);
            }
            
            log.info("Successfully processed {} vacancies", vacanciesToProcess.size());
            
        } catch (IOException e) {
            log.error("Failed to read system prompt file", e);
            throw new RuntimeException("Failed to load system prompt for skill processing", e);
        } catch (Exception e) {
            log.error("Error during vacancy skills processing", e);
            throw new RuntimeException("Failed to process vacancy skills with GigaChat", e);
        }
    }
    
    /**
     * Processes skills for a single vacancy using GigaChat.
     * Replaces old skills with normalized ones from GigaChat.
     */
    private void processVacancySkills(VacancyEntity vacancy, String systemPrompt) {
        List<WorkSkill> oldSkills = new ArrayList<>(vacancy.getSkills());
        List<WorkSkill> newSkills = new ArrayList<>();
        
        log.info("Processing {} skills for vacancy ID: {}", oldSkills.size(), vacancy.getId());
        
        for (WorkSkill oldSkill : oldSkills) {
            String skillDescription = oldSkill.getDescription();
            
            if (skillDescription == null || skillDescription.trim().isEmpty()) {
                log.warn("Skipping skill with empty description, ID: {}", oldSkill.getId());
                continue;
            }
            
            try {
                log.debug("Sending skill to GigaChat: {}", skillDescription);
                
                // Call GigaChat with custom system prompt and model
                String gigaChatResponse = gigaChatService.chat(
                    skillDescription, 
                    systemPrompt, 
                    "GigaChat"
                );
                
                log.debug("GigaChat response: {}", gigaChatResponse);
                
                // Parse JSON array from response
                List<String> extractedSkills = parseSkillsFromGigaChatResponse(gigaChatResponse);
                
                // Create new WorkSkill entities for each extracted skill
                for (String extractedSkill : extractedSkills) {
                    if (!"NOT_SKILL".equals(extractedSkill)) {
                        WorkSkill newSkill = findOrCreateWorkSkill(extractedSkill);
                        newSkills.add(newSkill);
                        log.info("Added skill: {} for vacancy ID: {}", extractedSkill, vacancy.getId());
                    } else {
                        log.info("Skipping NOT_SKILL for vacancy ID: {}", vacancy.getId());
                    }
                }
                
            } catch (Exception e) {
                log.error("Error processing skill '{}' for vacancy ID: {}", skillDescription, vacancy.getId(), e);
                // Continue processing other skills even if one fails
            }
        }
        
        // Replace old skills with new ones
        vacancy.setSkills(newSkills);
        vacancyRepository.save(vacancy);
        
        log.info("Replaced {} old skills with {} new skills for vacancy ID: {}", 
                oldSkills.size(), newSkills.size(), vacancy.getId());
    }
    
    /**
     * Parses skills from GigaChat JSON response.
     * Extracts JSON array from response which may contain markdown code blocks.
     */
    private List<String> parseSkillsFromGigaChatResponse(String response) {
        try {
            // Remove markdown code blocks if present
            String jsonString = response.trim();
            
            // Pattern to extract JSON array from markdown code block
            Pattern pattern = Pattern.compile("```json\\s*\\n([\\s\\S]*?)\\n```");
            Matcher matcher = pattern.matcher(jsonString);
            
            if (matcher.find()) {
                jsonString = matcher.group(1).trim();
            } else {
                // Try to find JSON array without markdown
                int startIdx = jsonString.indexOf('[');
                int endIdx = jsonString.lastIndexOf(']');
                if (startIdx != -1 && endIdx != -1) {
                    jsonString = jsonString.substring(startIdx, endIdx + 1);
                }
            }
            
            // Parse JSON array
            List<String> skills = objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
            log.debug("Parsed {} skills from GigaChat response", skills.size());
            return skills;
            
        } catch (Exception e) {
            log.error("Failed to parse skills from GigaChat response: {}", response, e);
            return List.of(); // Return empty list on parse error
        }
    }
    
    /**
     * Finds existing WorkSkill by description or creates a new one.
     */
    private WorkSkill findOrCreateWorkSkill(String description) {
        // Check if skill already exists
        WorkSkill existingSkill = workSkillService.findByDescriptionContaining(description)
                .stream()
                .filter(s -> s.getDescription().equalsIgnoreCase(description))
                .findFirst()
                .orElse(null);
        
        if (existingSkill != null) {
            log.debug("Found existing skill: {}", description);
            return existingSkill;
        }
        
        // Create new skill
        WorkSkill newSkill = new WorkSkill();
        newSkill.setDescription(description);
        newSkill.setMarketDemand(0.0); // Default value
        WorkSkill savedSkill = workSkillService.save(newSkill);
        log.debug("Created new skill: {}", description);
        return savedSkill;
    }
}
