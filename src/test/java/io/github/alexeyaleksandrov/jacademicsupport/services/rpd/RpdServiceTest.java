package io.github.alexeyaleksandrov.jacademicsupport.services.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RpdService Unit Tests")
class RpdServiceTest {

    @Mock
    private RpdRepository rpdRepository;

    @Mock
    private CompetencyAchievementIndicatorRepository indicatorRepository;

    @InjectMocks
    private RpdService rpdService;

    private CreateRpdDTO createRpdDTO;
    private CompetencyAchievementIndicator indicator1;
    private CompetencyAchievementIndicator indicator2;
    private Rpd expectedRpd;

    @BeforeEach
    void setUp() {
        // Настройка тестовых данных
        createRpdDTO = new CreateRpdDTO();
        createRpdDTO.setDiscipline_name("Программирование на Java");
        createRpdDTO.setYear(2024);
        createRpdDTO.setCompetencyAchievementIndicators(Arrays.asList("ПК-1.1", "ПК-2.1"));

        // Создание mock объектов индикаторов
        indicator1 = new CompetencyAchievementIndicator();
        indicator1.setId(1L);
        indicator1.setNumber("ПК-1.1");
        indicator1.setDescription("Способен разрабатывать программное обеспечение");

        indicator2 = new CompetencyAchievementIndicator();
        indicator2.setId(2L);
        indicator2.setNumber("ПК-2.1");
        indicator2.setDescription("Способен тестировать программное обеспечение");

        // Создание ожидаемого результата
        expectedRpd = new Rpd();
        expectedRpd.setId(1L);
        expectedRpd.setDisciplineName("Программирование на Java");
        expectedRpd.setYear(2024);
        expectedRpd.setCompetencyAchievementIndicators(Arrays.asList(indicator1, indicator2));
    }

    @Test
    @DisplayName("Успешное создание РПД с валидными данными")
    void createRpd_WithValidData_ShouldReturnCreatedRpd() {
        // Arrange
        when(indicatorRepository.findByNumber("ПК-1.1")).thenReturn(indicator1);
        when(indicatorRepository.findByNumber("ПК-2.1")).thenReturn(indicator2);
        when(rpdRepository.saveAndFlush(any(Rpd.class))).thenReturn(expectedRpd);

        // Act
        Rpd result = rpdService.createRpd(createRpdDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDisciplineName()).isEqualTo("Программирование на Java");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getCompetencyAchievementIndicators()).hasSize(2);
        assertThat(result.getCompetencyAchievementIndicators()).containsExactly(indicator1, indicator2);

        // Проверка взаимодействий с mock объектами
        verify(indicatorRepository).findByNumber("ПК-1.1");
        verify(indicatorRepository).findByNumber("ПК-2.1");
        verify(rpdRepository).saveAndFlush(any(Rpd.class));
    }

    @Test
    @DisplayName("Создание РПД с пустым списком индикаторов")
    void createRpd_WithEmptyIndicatorsList_ShouldReturnRpdWithEmptyIndicators() {
        // Arrange
        createRpdDTO.setCompetencyAchievementIndicators(Collections.emptyList());
        
        Rpd expectedEmptyRpd = new Rpd();
        expectedEmptyRpd.setId(1L);
        expectedEmptyRpd.setDisciplineName("Программирование на Java");
        expectedEmptyRpd.setYear(2024);
        expectedEmptyRpd.setCompetencyAchievementIndicators(Collections.emptyList());

        when(rpdRepository.saveAndFlush(any(Rpd.class))).thenReturn(expectedEmptyRpd);

        // Act
        Rpd result = rpdService.createRpd(createRpdDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDisciplineName()).isEqualTo("Программирование на Java");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getCompetencyAchievementIndicators()).isEmpty();

        // Проверка, что методы поиска индикаторов не вызывались
        verify(indicatorRepository, never()).findByNumber(anyString());
        verify(rpdRepository).saveAndFlush(any(Rpd.class));
    }

    @Test
    @DisplayName("Создание РПД с null списком индикаторов")
    void createRpd_WithNullIndicatorsList_ShouldThrowException() {
        // Arrange
        createRpdDTO.setCompetencyAchievementIndicators(null);

        // Act & Assert
        assertThatThrownBy(() -> rpdService.createRpd(createRpdDTO))
                .isInstanceOf(NullPointerException.class);

        // Проверка, что репозитории не вызывались
        verify(indicatorRepository, never()).findByNumber(anyString());
        verify(rpdRepository, never()).saveAndFlush(any(Rpd.class));
    }

    @Test
    @DisplayName("Создание РПД с одним индикатором")
    void createRpd_WithSingleIndicator_ShouldReturnRpdWithOneIndicator() {
        // Arrange
        createRpdDTO.setCompetencyAchievementIndicators(List.of("ПК-1.1"));
        
        Rpd expectedSingleRpd = new Rpd();
        expectedSingleRpd.setId(1L);
        expectedSingleRpd.setDisciplineName("Программирование на Java");
        expectedSingleRpd.setYear(2024);
        expectedSingleRpd.setCompetencyAchievementIndicators(List.of(indicator1));

        when(indicatorRepository.findByNumber("ПК-1.1")).thenReturn(indicator1);
        when(rpdRepository.saveAndFlush(any(Rpd.class))).thenReturn(expectedSingleRpd);

        // Act
        Rpd result = rpdService.createRpd(createRpdDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCompetencyAchievementIndicators()).hasSize(1);
        assertThat(result.getCompetencyAchievementIndicators().get(0)).isEqualTo(indicator1);

        verify(indicatorRepository).findByNumber("ПК-1.1");
        verify(rpdRepository).saveAndFlush(any(Rpd.class));
    }

    @Test
    @DisplayName("Проверка корректного маппинга полей DTO в сущность")
    void createRpd_ShouldCorrectlyMapDtoFieldsToEntity() {
        // Arrange
        when(indicatorRepository.findByNumber("ПК-1.1")).thenReturn(indicator1);
        when(indicatorRepository.findByNumber("ПК-2.1")).thenReturn(indicator2);
        when(rpdRepository.saveAndFlush(any(Rpd.class))).thenAnswer(invocation -> {
            Rpd rpd = invocation.getArgument(0);
            rpd.setId(1L); // Симуляция генерации ID
            return rpd;
        });

        // Act
        Rpd result = rpdService.createRpd(createRpdDTO);

        // Assert
        assertThat(result.getDisciplineName()).isEqualTo(createRpdDTO.getDiscipline_name());
        assertThat(result.getYear()).isEqualTo(createRpdDTO.getYear());
        
        // Проверка правильного вызова saveAndFlush с корректными данными
        verify(rpdRepository).saveAndFlush(argThat(rpd -> 
            rpd.getDisciplineName().equals("Программирование на Java") &&
            rpd.getYear() == 2024 &&
            rpd.getCompetencyAchievementIndicators().size() == 2
        ));
    }

    @Test
    @DisplayName("Проверка обработки дублирующихся индикаторов")
    void createRpd_WithDuplicateIndicators_ShouldHandleCorrectly() {
        // Arrange
        createRpdDTO.setCompetencyAchievementIndicators(Arrays.asList("ПК-1.1", "ПК-1.1", "ПК-2.1"));
        
        // Создаем ожидаемый результат с дубликатами
        Rpd expectedDuplicateRpd = new Rpd();
        expectedDuplicateRpd.setId(1L);
        expectedDuplicateRpd.setDisciplineName("Программирование на Java");
        expectedDuplicateRpd.setYear(2024);
        expectedDuplicateRpd.setCompetencyAchievementIndicators(Arrays.asList(indicator1, indicator1, indicator2));
        
        when(indicatorRepository.findByNumber("ПК-1.1")).thenReturn(indicator1);
        when(indicatorRepository.findByNumber("ПК-2.1")).thenReturn(indicator2);
        when(rpdRepository.saveAndFlush(any(Rpd.class))).thenReturn(expectedDuplicateRpd);

        // Act
        Rpd result = rpdService.createRpd(createRpdDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCompetencyAchievementIndicators()).hasSize(3); // Включая дубликаты

        // Проверка, что findByNumber вызывался для каждого элемента в списке
        verify(indicatorRepository, times(2)).findByNumber("ПК-1.1");
        verify(indicatorRepository, times(1)).findByNumber("ПК-2.1");
        verify(rpdRepository).saveAndFlush(any(Rpd.class));
    }

    @Test
    @DisplayName("Проверка обработки null значения в поле year")
    void createRpd_WithNullYear_ShouldThrowException() {
        // Arrange
        CreateRpdDTO dtoWithNullYear = new CreateRpdDTO();
        dtoWithNullYear.setDiscipline_name("Test Discipline");
        dtoWithNullYear.setYear(null);
        dtoWithNullYear.setCompetencyAchievementIndicators(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> rpdService.createRpd(dtoWithNullYear))
                .isInstanceOf(NullPointerException.class);

        // Проверка, что репозитории не вызывались
        verify(indicatorRepository, never()).findByNumber(anyString());
        verify(rpdRepository, never()).saveAndFlush(any(Rpd.class));
    }

    @Test
    @DisplayName("Проверка обработки null значения в поле discipline_name")
    void createRpd_WithNullDisciplineName_ShouldHandleGracefully() {
        // Arrange
        CreateRpdDTO dtoWithNullName = new CreateRpdDTO();
        dtoWithNullName.setDiscipline_name(null);
        dtoWithNullName.setYear(2024);
        dtoWithNullName.setCompetencyAchievementIndicators(Collections.emptyList());

        Rpd expectedNullNameRpd = new Rpd();
        expectedNullNameRpd.setId(1L);
        expectedNullNameRpd.setDisciplineName(null);
        expectedNullNameRpd.setYear(2024);
        expectedNullNameRpd.setCompetencyAchievementIndicators(Collections.emptyList());

        when(rpdRepository.saveAndFlush(any(Rpd.class))).thenReturn(expectedNullNameRpd);

        // Act
        Rpd result = rpdService.createRpd(dtoWithNullName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDisciplineName()).isNull();
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getCompetencyAchievementIndicators()).isEmpty();

        verify(rpdRepository).saveAndFlush(any(Rpd.class));
    }
}
