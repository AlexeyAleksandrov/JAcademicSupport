# DST — правила принятия решений и пороги

## 1. Где реализованы решения

Решение формируется **на сервере**; клиент только отображает результат.

1. **Сервер (Java):**
   - `services/dst/DstCombinationService.java` — собирает источники, вычисляет массы `mT/mU/mF`, конфликт `K`, `BetP`, `delta` и флаг `usedYager`.
   - `services/dst/DstDecisionResolver.java` — по массам, `delta` и фактическим часам `supplyHours` вычисляет:
     - `recommendation` — аллокационное действие;
     - `expertiseRequired` — флаг недостаточной надёжности данных.

2. **Клиент (JS):** `resources/static/curriculum.html` — функция `dstDecisionLabel(...)`.
   - Только мапит серверные поля `recommendation` + `expertiseRequired` в CSS-класс и русскую подпись.
   - Если `expertiseRequired == true`, в колонке «Решение» показывается **«Экспертиза»**, независимо от `recommendation`.

---

## 2. Серверная логика: `DstDecisionResolver.resolve`

Метод принимает `DstTraceResponse`, `int supplyHours` и сумму `totalBetP` по всему уровню:

```java
public void resolve(DstTraceResponse trace, int supplyHours, double totalBetP) {
    DstSettings s = settingsService.get();
    double tauDelta     = s.getTauDelta();     // 0.15
    double tauK         = s.getTauK();         // 0.40
    double obsoleteMf   = s.getObsoleteMf();   // 0.80
    double obsoleteMt   = s.getObsoleteMt();   // 0.10

    double mT = trace.getMT(), mF = trace.getMF();
    double K  = trace.getK();
    double betp   = trace.getBetp();
    double supply = trace.getSupply();

    // Решение принимается по нормированному разрыву Δ_norm = nBetP − supply,
    // который совпадает с Δ_norm на фронтенде, а не по абсолютному BetP − supply.
    double nBetP     = totalBetP > 0 ? betp / totalBetP : 0.0;
    double deltaNorm = nBetP - supply;

    String recommendation;
    if (mF > obsoleteMf && mT < obsoleteMt && supplyHours > 0) {
        recommendation = "delete";
    } else if (supplyHours == 0 && deltaNorm > tauDelta) {
        recommendation = "introduce";
    } else if (supplyHours > 0 && deltaNorm > tauDelta) {
        recommendation = "boost";
    } else if (deltaNorm < -tauDelta) {
        recommendation = "reduce";
    } else {
        recommendation = "preserve";
    }

    boolean expertiseRequired = K >= tauK;

    trace.setRecommendation(recommendation);
    trace.setExpertiseRequired(expertiseRequired);
}
```

### 2.1. Таблица аллокационных действий

| Условие | Пороги | Рекомендация (RU) | Код |
|---|---|---|---|
| `mF > OBSOLETE_MF` **и** `mT < OBSOLETE_MT` **и** `supplyHours > 0` | `0.80` / `0.10` | Удалить | `delete` |
| `supplyHours == 0` **и** `delta > TAU_DELTA` | `0.15` | Ввести | `introduce` |
| `supplyHours > 0` **и** `delta > TAU_DELTA` | `0.15` | Усилить | `boost` |
| `delta < -TAU_DELTA` | `0.15` | Сократить | `reduce` |
| Остальные случаи | — | Сохранить | `preserve` |

### 2.2. Флаг экспертизы

`expertiseRequired = true` только при `K >= TAU_K` (`0.40`).

Эта граница совпадает с переключением комбинирования на правило Ягера: если источники конфликтуют, рекомендуется экспертная оценка.

Флаг не зависит от аллокационного действия. В интерфейсе он заменяет действие предупреждением **«Экспертиза»**, но в трассе оба сигнала видны отдельно.

**Примечание.** Проверка `mU > TAU_THETA` была отключена, поскольку скомбинированная неопределённость почти всегда превышает разумный порог и приводит к ложным срабатываниям.

---

## 3. Клиентская логика: `curriculum.html dstDecisionLabel`

```javascript
function dstDecisionLabel(recommendation, expertiseRequired) {
  if (expertiseRequired) return {cls: 'rec-expertise', label: 'Экспертиза', kind: 'expertise'};
  switch (recommendation) {
    case 'delete':    return {cls: 'rec-reduce',    label: 'Удалить',   kind: 'delete'};
    case 'introduce': return {cls: 'rec-introduce', label: 'Ввести',    kind: 'introduce'};
    case 'boost':     return {cls: 'rec-moderate',  label: 'Усилить',   kind: 'boost'};
    case 'reduce':    return {cls: 'rec-strong',    label: 'Сократить', kind: 'reduce'};
    case 'preserve':
    default:          return {cls: 'rec-preserve', label: 'Сохранить', kind: 'preserve'};
  }
}
```

### 3.1. Отображение в таблицах L0–L2

| `expertiseRequired` | `recommendation` | Подпись в колонке «Решение» | kind |
|---|---|---|---|
| `true` | любое | **Экспертиза** | `expertise` |
| `false` | `delete` | Удалить | `delete` |
| `false` | `introduce` | Ввести | `introduce` |
| `false` | `boost` | Усилить | `boost` |
| `false` | `reduce` | Сократить | `reduce` |
| `false` | `preserve` | Сохранить | `preserve` |

На фронтенде больше **нет** самостоятельного порога `TAU_ALLOC` для принятия решения. Порог `tauAlloc` остаётся только для окраски колонки `Δ_norm` и для совместимости настроек.

---

## 4. Полный путь «от сервера к экрану»

1. Java: `DstCombinationService` → `DstTraceResponse` с `mT/mU/mF/K/delta/usedYager`.
2. Java: `DstDecisionResolver.resolve(trace, supplyHours)` → заполняет `recommendation` (`delete` / `introduce` / `boost` / `reduce` / `preserve`) и `expertiseRequired`.
3. `DstLevel0Service`, `DstLevel1Service`, `DstLevel2Service` копируют оба поля в результирующие DTO.
4. Клиент: `dstDecisionLabel(...)` → финальная надпись и CSS-класс.

---

## 5. Все пороги: умолчания и текущие значения

Источники:

- `DstSettingsDefaults.java` — заводские значения.
- `dst_settings` — текущая строка настроек в БД.

| Порог | Параметр | Default | Текущее в БД | Описание |
|---|---|---|---|---|
| `τΔ` | `tauDelta` | `0.03` | `0.03` | Нормированный разрыв `nBetP − supply` (Δ_norm) для усиления/сокращения/введения |
| `τK` | `tauK` | `0.40` | `0.40` | Переключение с Демпстера на Ягер; граница флага экспертизы (`K >= τK`) |
| `τΘ` | `tauTheta` | `0.50` | `0.50` | В настоящий момент не используется для экспертизы (флаг основан только на `K`); сохранён для совместимости |
| Устаревание m(F) | `obsoleteMf` | `0.80` | `0.80` | `mF` выше — технология устарела |
| Устаревание m(T) | `obsoleteMt` | `0.10` | `0.10` | `mT` ниже — технология не востребована |
| Аллокационный τ (устарел на клиенте) | `tauAlloc` | `0.03` | `0.03` | Теперь используется только для окраски `Δ_norm`; решение считается на сервере |
| N(BetP) L0 | `nClustersL0` | `25` | `25` | Знаменатель BetP на уровне L0 |
| w(VAC) | `wVac` | `0.8` | `0.8` | Вес источника VAC |
| w(EXP) | `wExp` | `0.9` | `0.9` | Вес источника EXP |
| w(FC) | `wFc` | `0.6` | `0.6` | Вес источника FC |

**Примечание.** Параметры `strongSignalDelta` и `strongBoostDelta` больше не используются для деления «Усилить» на подвиды.

---

## 6. Исправленные расхождения

1. **Единый источник истины.** Решение считается только в `DstDecisionResolver`; `allocDecide()` удалена.
2. **«Экспертиза» достижима.** Она появляется в основной колонке, когда `expertiseRequired == true`, а не скрывается в трассе.
3. **«Ввести» с порогом.** Срабатывает только при `supplyHours == 0` **и** `delta > τΔ` (`0.15`), а не при любом положительном `Δ_norm`.
4. **Граница `K` выровнена.** Экспертиза требуется при `K >= 0.40`, что совпадает с переключением на правило Ягера.
5. **«Удалить» достижимо.** Достаточно доминирующей негативной массы (`mF > 0.8`, `mT < 0.1`) при ненулевых часах в УП; исчезновение «Удалить» при отсутствии EXP-негативов больше не является проблемой логики отображения.
