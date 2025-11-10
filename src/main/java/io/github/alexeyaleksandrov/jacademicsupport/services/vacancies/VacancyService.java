package io.github.alexeyaleksandrov.jacademicsupport.services.vacancies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexeyaleksandrov.jacademicsupport.configuration.ItKeywordsConfig;
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
    private final ItKeywordsConfig itKeywordsConfig;

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

    /**
     * Delete all non-IT vacancies from the database.
     * A vacancy is considered IT-related if its name contains at least one keyword from it-keywords.yml (case-insensitive).
     * This method should be called periodically to clean up the database from irrelevant vacancies.
     * 
     * @return Number of deleted vacancies
     */
    @Transactional
    public long deleteNonItVacancies() {
        log.info("Starting cleanup of non-IT vacancies...");
        
        List<String> keywords = itKeywordsConfig.getItKeywords();
        if (keywords == null || keywords.isEmpty()) {
            log.warn("IT keywords list is empty. No vacancies will be deleted.");
            return 0;
        }
        
        List<VacancyEntity> allVacancies = vacancyRepository.findAll();
        List<VacancyEntity> nonItVacancies = new ArrayList<>();
        
        for (VacancyEntity vacancy : allVacancies) {
            if (!isItRelatedVacancy(vacancy, keywords)) {
                nonItVacancies.add(vacancy);
            }
        }
        
        long deletedCount = nonItVacancies.size();
        
        if (deletedCount > 0) {
            log.info("Found {} non-IT vacancies to delete", deletedCount);
            vacancyRepository.deleteAll(nonItVacancies);
            log.info("Successfully deleted {} non-IT vacancies", deletedCount);
        } else {
            log.info("No non-IT vacancies found");
        }
        
        return deletedCount;
    }

    /**
     * Checks if a vacancy is IT-related based on IT keywords from configuration.
     * 
     * @param vacancy The vacancy to check
     * @param keywords List of IT keywords
     * @return true if the vacancy name contains at least one IT keyword (case-insensitive)
     */
    private boolean isItRelatedVacancy(VacancyEntity vacancy, List<String> keywords) {
        if (vacancy == null || vacancy.getName() == null) {
            return false;
        }
        
        String lowercaseName = vacancy.getName().toLowerCase();
        return keywords.stream()
                .anyMatch(keyword -> lowercaseName.contains(keyword.toLowerCase()));
    }

    /**
     * Process skills for a newly added vacancy using GigaChat.
     * This method is designed to be called when a single new vacancy is added to the system.
     * It loads the system prompt and processes the vacancy's skills through GigaChat.
     * 
     * @param vacancy The vacancy entity to process
     * @throws RuntimeException if the system prompt cannot be loaded or processing fails
     */
    @Transactional
    public void processNewVacancySkills(VacancyEntity vacancy) {
        if (vacancy == null) {
            log.warn("Attempted to process null vacancy");
            return;
        }

        if (vacancy.getSkills() == null || vacancy.getSkills().isEmpty()) {
            log.debug("Vacancy ID: {} has no skills to process", vacancy.getId());
            return;
        }

        try {
            // Load system prompt from resources
            String systemPrompt = resourceFileReader.readResourceFile("prompts/vacancies_work_skill_extract_system_prompt.txt");
            log.info("Processing skills for new vacancy ID: {}, Name: {}", vacancy.getId(), vacancy.getName());

            // Process the vacancy's skills
            processVacancySkills(vacancy, systemPrompt);

            log.info("Successfully processed skills for vacancy ID: {}", vacancy.getId());

        } catch (IOException e) {
            log.error("Failed to read system prompt file for vacancy ID: {}", vacancy.getId(), e);
            // Don't throw exception - allow vacancy to be saved even if skill processing fails
        } catch (Exception e) {
            log.error("Error during skill processing for vacancy ID: {}", vacancy.getId(), e);
            // Don't throw exception - allow vacancy to be saved even if skill processing fails
        }
    }

    /**
     * Extract skills from text for a newly added vacancy that has NO skills.
     * This method is designed to be called when a single new vacancy without skills is added to the system.
     * It loads the text extraction system prompt and extracts skills from the vacancy's description.
     * 
     * @param vacancy The vacancy entity to process (must have no skills but have description)
     */
    @Transactional
    public void processNewVacancyWithoutSkills(VacancyEntity vacancy) {
        if (vacancy == null) {
            log.warn("Attempted to process null vacancy");
            return;
        }

        if (vacancy.getSkills() != null && !vacancy.getSkills().isEmpty()) {
            log.debug("Vacancy ID: {} already has skills, skipping text extraction", vacancy.getId());
            return;
        }

        if (vacancy.getDescription() == null || vacancy.getDescription().trim().isEmpty()) {
            log.debug("Vacancy ID: {} has no description to extract skills from", vacancy.getId());
            return;
        }

        try {
            // Load text extraction system prompt from resources
            String systemPrompt = resourceFileReader.readResourceFile("prompts/vacancies_text_work_skill_extract_system_prompt.txt");
            log.info("Extracting skills from text for new vacancy ID: {}, Name: {}", vacancy.getId(), vacancy.getName());

            // Extract skills from the vacancy's description text
            extractSkillsFromText(vacancy, systemPrompt);

            log.info("Successfully extracted skills from text for vacancy ID: {}", vacancy.getId());

        } catch (IOException e) {
            log.error("Failed to read text extraction system prompt file for vacancy ID: {}", vacancy.getId(), e);
            // Don't throw exception - allow vacancy to be saved even if skill extraction fails
        } catch (RuntimeException e) {
            // Check if this is a 402 Payment Required error
            if (e.getMessage() != null &&
                    (e.getMessage().contains("402") ||
                            e.getMessage().contains("Payment Required") ||
                            e.getMessage().contains("PAYMENT_REQUIRED"))) {
                log.error("GigaChat tokens exhausted (402) while processing vacancy ID: {}. Skipping this vacancy.", vacancy.getId());
                // Don't throw - just skip this vacancy and continue with others
                return;
            }
            log.error("Error during skill extraction from text for vacancy ID: {}", vacancy.getId(), e);
            // Don't throw exception - allow vacancy to be saved even if skill extraction fails
        }
    }

    @Transactional
    public void processVacancySkillsWithGigaChat() {
        try {
            // Load system prompt from resources
            String systemPrompt = resourceFileReader.readResourceFile("prompts/vacancies_work_skill_extract_system_prompt.txt");
            log.info("System prompt loaded successfully");

            // Get all vacancies that have at least one skill
            List<VacancyEntity> allVacancies = vacancyRepository.findAll();
            List<VacancyEntity> vacanciesToProcess = allVacancies.stream()
                    .filter(v -> v.getSkills() != null && !v.getSkills().isEmpty())
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
     * Extracts skills from vacancy description text using GigaChat.
     * This method processes vacancies that have NO skills by analyzing their description text
     * and extracting professional skills mentioned in the text.
     * Uses a different system prompt specifically designed for text analysis.
     */
    @Transactional
    public void extractSkillsFromVacancyText() {
        try {
            // Load system prompt for text extraction from resources
            String systemPrompt = resourceFileReader.readResourceFile("prompts/vacancies_text_work_skill_extract_system_prompt.txt");
            log.info("Text extraction system prompt loaded successfully");

            // Get all vacancies that have NO skills
            List<VacancyEntity> allVacancies = vacancyRepository.findAll();
            List<VacancyEntity> vacanciesToProcess = allVacancies.stream()
                    .filter(v -> v.getSkills() == null || v.getSkills().isEmpty())
                    .filter(v -> v.getDescription() != null && !v.getDescription().trim().isEmpty())
                    .toList();

            log.info("Found {} vacancies without skills to process", vacanciesToProcess.size());

            int processedCount = 0;
            for (VacancyEntity vacancy : vacanciesToProcess) {
                try {
                    log.info("Extracting skills from vacancy ID: {}, Name: {}", vacancy.getId(), vacancy.getName());
                    extractSkillsFromText(vacancy, systemPrompt);
                    processedCount++;
                } catch (RuntimeException e) {
                    // Check if this is a 402 Payment Required error
                    if (e.getMessage() != null &&
                            (e.getMessage().contains("402") ||
                                    e.getMessage().contains("Payment Required") ||
                                    e.getMessage().contains("PAYMENT_REQUIRED"))) {
                        log.error("GigaChat tokens exhausted (402 Payment Required). Stopping processing to avoid unnecessary API calls.");
                        log.info("Successfully processed {} out of {} vacancies before token exhaustion",
                                processedCount, vacanciesToProcess.size());
                        throw new RuntimeException("GigaChat API tokens exhausted. Please top up your balance and try again.", e);
                    }
                    // For other errors, log and continue
                    log.error("Error processing vacancy ID: {}, continuing with next vacancy", vacancy.getId(), e);
                }
            }

            log.info("Successfully extracted skills from {} vacancies", processedCount);

        } catch (IOException e) {
            log.error("Failed to read text extraction system prompt file", e);
            throw new RuntimeException("Failed to load system prompt for text skill extraction", e);
        } catch (RuntimeException e) {
            // Re-throw payment required errors and other critical errors
            if (e.getMessage() != null && e.getMessage().contains("tokens exhausted")) {
                throw e;
            }
            log.error("Error during vacancy text skill extraction", e);
            throw new RuntimeException("Failed to extract skills from vacancy text with GigaChat", e);
        }
    }

    /**
     * Extracts skills from a single vacancy's description text using GigaChat.
     * Sends the full description to GigaChat and creates WorkSkill entities from the response.
     * 
     * @throws RuntimeException if 402 Payment Required error occurs (tokens exhausted)
     */
    private void extractSkillsFromText(VacancyEntity vacancy, String systemPrompt) {
        String description = vacancy.getDescription();

        if (description == null || description.trim().isEmpty()) {
            log.warn("Vacancy ID: {} has no description to analyze", vacancy.getId());
            return;
        }

        try {
            log.debug("Sending vacancy description to GigaChat for skill extraction, vacancy ID: {}", vacancy.getId());

            // Call GigaChat with the vacancy description text
            String gigaChatResponse = gigaChatService.chat(
                    description,
                    systemPrompt,
                    "GigaChat"
            );

            log.debug("GigaChat response for vacancy ID {}: {}", vacancy.getId(), gigaChatResponse);

            // Parse the skills from GigaChat response
            List<String> extractedSkills = parseSkillsFromGigaChatResponse(gigaChatResponse);
            log.info("Extracted {} skills from vacancy ID: {}", extractedSkills.size(), vacancy.getId());

            // Create WorkSkill entities for each extracted skill
            List<WorkSkill> newSkills = new ArrayList<>();
            for (String skillName : extractedSkills) {
                if (!"NOT_SKILL".equalsIgnoreCase(skillName)) {
                    WorkSkill workSkill = findOrCreateWorkSkill(skillName);
                    newSkills.add(workSkill);
                }
            }

            // Set the new skills to the vacancy
            vacancy.setSkills(newSkills);
            vacancyRepository.save(vacancy);

            log.info("Successfully extracted and saved {} skills for vacancy ID: {}", newSkills.size(), vacancy.getId());

        } catch (RuntimeException e) {
            log.error("Failed to extract skills from text for vacancy ID: {}", vacancy.getId(), e);
            // Check if this is a payment required error - if so, propagate it up to stop all processing
            if (e.getMessage() != null &&
                    (e.getMessage().contains("402") ||
                            e.getMessage().contains("Payment Required") ||
                            e.getMessage().contains("PAYMENT_REQUIRED"))) {
                throw e; // Propagate 402 errors to stop processing
            }
            // For other errors, just log and return (don't stop processing other vacancies)
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
            List<String> skills = objectMapper.readValue(jsonString, new TypeReference<List<String>>() {
            });
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
