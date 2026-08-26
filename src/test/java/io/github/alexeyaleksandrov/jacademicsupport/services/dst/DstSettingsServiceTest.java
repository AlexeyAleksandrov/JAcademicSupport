package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DstSettingsRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ExpertOpinionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ForesightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DstSettingsServiceTest {

    private DstSettingsRepository settingsRepository;
    private ExpertOpinionRepository expertRepository;
    private ForesightRepository foresightRepository;
    private DstSettingsService service;

    @BeforeEach
    void setUp() {
        settingsRepository = mock(DstSettingsRepository.class);
        expertRepository = mock(ExpertOpinionRepository.class);
        foresightRepository = mock(ForesightRepository.class);
        when(settingsRepository.findById(DstSettings.SINGLETON_ID)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(DstSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new DstSettingsService(settingsRepository, expertRepository, foresightRepository);
    }

    @Test
    void factoryDefaultsUseAutomaticDatabaseDenominators() {
        DstSettings defaults = service.defaults();
        assertEquals(0, defaults.getTotalExperts());
        assertEquals(0, defaults.getTotalSources());
        assertTrue(defaults.getNegativeEvidenceEnabled());
        assertFalse(defaults.getStdEnabled());
        assertEquals(15.0, defaults.getLambdaVacL1());
        assertEquals(15.0, defaults.getLambdaVacL2());
        assertEquals(5.0, defaults.getLambdaExpL1());
        assertEquals(5.0, defaults.getLambdaExpL2());
    }

    @Test
    void automaticDenominatorsUseActualDatabaseCounts() {
        when(expertRepository.countDistinctExperts()).thenReturn(11L);
        when(foresightRepository.countDistinctSourceUrls()).thenReturn(8L);

        assertEquals(11, service.effectiveTotalExperts());
        assertEquals(8, service.effectiveTotalSources());
    }

    @Test
    void rejectsOutOfRangeWeightsAndUnknownModes() {
        DstSettings invalidWeight = new DstSettings();
        invalidWeight.setWVac(1.1);
        ResponseStatusException weightError = assertThrows(
                ResponseStatusException.class, () -> service.update(invalidWeight));
        assertEquals(HttpStatus.BAD_REQUEST, weightError.getStatusCode());

        DstSettings invalidMode = new DstSettings();
        invalidMode.setDomainMode("GUESS");
        ResponseStatusException modeError = assertThrows(
                ResponseStatusException.class, () -> service.update(invalidMode));
        assertEquals(HttpStatus.BAD_REQUEST, modeError.getStatusCode());
    }

    @Test
    void rejectsEnablingUnimplementedStdSource() {
        DstSettings settings = new DstSettings();
        settings.setStdEnabled(true);
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.update(settings));
        assertTrue(error.getReason().contains("STD"));
        verify(settingsRepository, never()).save(settings);
    }

    @Test
    void level1AndLevel2LambdasAreStoredIndependently() {
        DstSettings current = new DstSettings();
        when(settingsRepository.findById(DstSettings.SINGLETON_ID)).thenReturn(Optional.of(current));

        DstSettings incoming = new DstSettings();
        incoming.setLambdaVacL1(7.0);
        incoming.setLambdaVacL2(19.0);
        incoming.setLambdaExpL1(3.0);
        incoming.setLambdaExpL2(11.0);

        DstSettings saved = service.update(incoming);

        assertEquals(7.0, saved.getLambdaVacL1());
        assertEquals(19.0, saved.getLambdaVacL2());
        assertEquals(3.0, saved.getLambdaExpL1());
        assertEquals(11.0, saved.getLambdaExpL2());
    }

    @Test
    void migratesFormerSharedLambdaValuesIntoMissingLevel2Columns() {
        DstSettings legacy = new DstSettings();
        legacy.setLambdaVacL1(8.0);
        legacy.setLambdaExpL1(4.0);
        legacy.setLambdaFcL1(1.5);
        legacy.setLambdaStdL1(2.5);
        legacy.setLambdaVacL2(null);
        legacy.setLambdaExpL2(null);
        legacy.setLambdaFcL2(null);
        legacy.setLambdaStdL2(null);
        when(settingsRepository.findById(DstSettings.SINGLETON_ID)).thenReturn(Optional.of(legacy));

        DstSettings migrated = service.get();

        assertEquals(8.0, migrated.getLambdaVacL2());
        assertEquals(4.0, migrated.getLambdaExpL2());
        assertEquals(1.5, migrated.getLambdaFcL2());
        assertEquals(2.5, migrated.getLambdaStdL2());
        verify(settingsRepository).save(legacy);
    }
}
