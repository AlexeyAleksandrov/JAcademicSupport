# DST — правила принятия решений и пороги

## 1. Где реализованы решения

Решения формируются в двух местах:

1. **Сервер (Java):** `services/dst/DstCombinationService.java` — метод `decide(...)`.
   - Возвращает одну из строк: `obsolete`, `strong`, `moderate`, `reduce`, `expertise`, `preserve`.
   - Вставляется в `DstTraceResponse.recommendation`.

2. **Клиент (JS):** `resources/static/curriculum.html` — функция `allocDecide(...)`.
   - Дополняет серверную рекомендацию до **пяти выходных формулировок**: `Удалить`, `Ввести`, `Усилить`, `Сократить`, `Сохранить`.
   - Именно здесь появляется правило **«Ввести»**.

---

## 2. Серверная логика: `DstCombinationService.decide`

Полный метод:

```java
private String decide(double mT, double mU, double mF, double K, double delta) {
    DstSettings s = settingsService.get();
    double tauDelta = s.getTauDelta();
    double tauK     = s.getTauK();
    double tauTheta = s.getTauTheta();

    if (mF > s.getObsoleteMf() && mT < s.getObsoleteMt()) return "obsolete";
    if (delta > tauDelta && K <= tauK) {
        boolean clearSignal = mU <= tauTheta || delta > s.getStrongSignalDelta();
        if (clearSignal)
            return delta > s.getStrongBoostDelta() ? "strong" : "moderate";
    }
    if (delta < -tauDelta) return "reduce";
    if (K > tauK || mU > tauTheta) return "expertise";
    return "preserve";
}
```

Переменные:

- `mT` / `mU` / `mF` — итоговые массы после комбинирования.
- `delta = BetP − supply` — абсолютный разрыв (доли, 0..1).
- `K` — максимальный конфликт при комбинировании.

### 2.1. Таблица серверных решений

| Условие | Пороги | Рекомендация (RU) | Код |
|---|---|---|---|
| `mF > OBSOLETE_MF` **и** `mT < OBSOLETE_MT` | `0.80` / `0.10` | Технология устарела, удалить | `obsolete` |
| `delta > TAU_DELTA` **и** `K ≤ TAU_K` **и** (`mU ≤ TAU_THETA` **или** `delta > STRONG_SIGNAL_DELTA`) **и** `delta > STRONG_BOOST_DELTA` | `0.15` / `0.40` / `0.15` / `0.35` / `0.50` | Значительно увеличить часы (срочно усилить) | `strong` |
| `delta > TAU_DELTA` **и** `K ≤ TAU_K` **и** (`mU ≤ TAU_THETA` **или** `delta > STRONG_SIGNAL_DELTA`) **и** `delta ≤ STRONG_BOOST_DELTA` | — | Небольшое усиление | `moderate` |
| `delta < -TAU_DELTA` | `0.15` | Сократить часы | `reduce` |
| `K > TAU_K` **или** `mU > TAU_THETA` | `0.40` / `0.15` | Требуется экспертиза | `expertise` |
| Остальные случаи | — | Часы в норме (сохранить) | `preserve` |

### 2.2. Важный нюанс: «Ввести» на сервере отсутствует

В `decide(...)` **нет** отдельного правила `introduce` / «Ввести». Вернуть `preserve` может быть и объект с `supply = 0` — клиент сам перекраивает это в «Ввести».

---

## 3. Клиентская логика: `curriculum.html allocDecide`

Функция переводит серверную рекомендацию и нормированный разрыв `Δ_norm = nBetP − supply` в итоговую надпись.

```javascript
function allocDecide(supplyHours, deltaNorm, serverRec) {
  const tau = tauAlloc();
  const isObsolete = serverRec === 'obsolete' && (supplyHours || 0) > 0;
  const isUncov    = (supplyHours || 0) === 0 && deltaNorm > 0;
  if (isObsolete) return {label: 'Удалить',   kind: 'obsolete'};
  if (isUncov)    return {label: 'Ввести',    kind: 'introduce'};
  if (deltaNorm >  tau) return {label: 'Усилить',   kind: 'boost'};
  if (deltaNorm < -tau) return {label: 'Сократить', kind: 'reduce'};
  return {label: 'Сохранить', kind: 'preserve'};
}
```

### 3.1. Таблица клиентских решений

| Условие | Порог | Рекомендация | kind |
|---|---|---|---|
| `serverRec === 'obsolete'` **и** `supplyHours > 0` | — | Удалить | `obsolete` |
| `supplyHours === 0` **и** `deltaNorm > 0` | — | Ввести | `introduce` |
| `deltaNorm > TAU_ALLOC` | `0.03` | Усилить | `boost` |
| `deltaNorm < -TAU_ALLOC` | `0.03` | Сократить | `reduce` |
| Остальные | — | Сохранить | `preserve` |

`TAU_ALLOC` берётся из `dstSettings.settings.tauAlloc`, fallback `0.03` (3 п.п. от бюджета уровня).

---

## 4. Полный путь «от сервера к экрану»

1. Java: `DstCombinationService.decide` → `recommendation` (`strong` / `moderate` / `reduce` / `obsolete` / `expertise` / `preserve`).
2. Клиент: `curriculum.html` `allocDecide` → финальная надпись (`Удалить` / `Ввести` / `Усилить` / `Сократить` / `Сохранить`).
3. В окне трасса (`curriculum.html` строка 1604) серверная `recommendation` отображается как:

| Серверный код | Подпись в трассе |
|---|---|
| `strong` | Срочно усилить |
| `moderate` | Усилить умеренно |
| `preserve` | Баланс |
| `reduce` | Сократить |
| `obsolete` | Устарело (m(F)↑) |
| `expertise` | Экспертиза |

---

## 5. Все пороги: умолчания и текущие значения

Источники:

- `DstSettingsDefaults.java` — заводские значения.
- `dst_settings` — текущая строка настроек в БД.

| Порог | Параметр | Default | Текущее в БД | Описание |
|---|---|---|---|---|
| `τΔ` | `tauDelta` | `0.15` | `0.15` | Абсолютный разрыв `BetP − supply` для усиления/сокращения |
| `τK` | `tauK` | `0.40` | `0.40` | Переключение с Демпстера на Ягер |
| `τΘ` | `tauTheta` | `0.15` | `0.15` | Неопределённость, при которой сразу экспертиза |
| Сильный сигнал | `strongSignalDelta` | `0.35` | `0.35` | `delta > 0.35` — считать сигнал чистым без проверки `mU` |
| Сильное усиление | `strongBoostDelta` | `0.50` | `0.50` | Граница между `moderate` и `strong` |
| Устаревание m(F) | `obsoleteMf` | `0.80` | `0.80` | `mF` выше — технология устарела |
| Устаревание m(T) | `obsoleteMt` | `0.10` | `0.10` | `mT` ниже — технология не востребована |
| Аллокационный τ | `tauAlloc` | `0.03` | `0.03` | Порог для `Δ_norm = nBetP − supply` в клиенте |
| N(BetP) L0 | `nClustersL0` | `25` | `25` | Знаменатель BetP на уровне L0 |
| w(VAC) | `wVac` | `0.8` | `0.8` | Вес источника VAC |
| w(EXP) | `wExp` | `0.9` | `0.9` | Вес источника EXP |
| w(FC) | `wFc` | `0.6` | `0.6` | Вес источника FC |

---

## 6. Что добавить в статью (Table 2)

Для полноты в таблицу статьи нужно включить:

1. **«Ввести»** — `supplyHours === 0` и `Δ_norm > 0` (клиентская `allocDecide`).
2. **Разделение «Усилить»** на две ступени:
   - `strong` (сервер: `delta > 0.5` при чистом сигнале).
   - `moderate` (сервер: `0.15 < delta ≤ 0.5` при чистом сигнале).
3. **Уточнение, что `expertise` срабатывает по двум признакам**: `K > 0.4` (правило Ягера) **или** `mU > 0.15` (высокая неопределённость).
4. **Уточнение условия для `obsolete`**: `mF > 0.8` **и одновременно** `mT < 0.1`.
5. **Клиентский аллокационный порог** `TAU_ALLOC = 0.03` — именно он управляет финальными надписями `Усилить` / `Сократить` / `Сохранить` в таблицах уровней L0–L2.
