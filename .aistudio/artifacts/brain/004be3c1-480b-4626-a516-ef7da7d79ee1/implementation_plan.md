# Пользовательские базы продуктов (Custom Food Databases)

Добавление полноценной системы управления пользовательскими базами продуктов в Chompass: импорт баз из файлов CSV и JSON с гибким автоопределением колонок, просмотр и полнофункциональное редактирование (добавление, изменение и удаление продуктов внутри базы), экспорт баз, а также интеграция в общий поиск еды с отдельным бейджем источника.

## User Review & Critical Decisions

> [!IMPORTANT]
> На основе ваших ответов согласованы следующие ключевые решения:
> - **Формат загрузки**: Поддержка файлов CSV и JSON с автоматическим распознаванием колонок (русские и английские заголовки для названия, калорий, белков, жиров, углеводов, размера порции и бренда).
> - **Возможности редактирования**: Полный цикл CRUD — как на уровне самой базы (создание, переименование, описание, включение/выключение, экспорт, удаление базы), так и на уровне отдельных продуктов (ручное добавление новых продуктов, редактирование всех макросов и порций, удаление продуктов).
> - **Отображение в поиске**: Пользовательские базы интегрируются в единый поиск продуктов на экране добавления еды с приоритетным ранжированием и фирменным бейджем источника (с указанием названия пользовательской базы).

---

## 1. Overview & Core Concept

- **Что делает функция**: Позволяет пользователям создавать собственные базы данных продуктов, загружать готовые справочники и таблицы (например, базы спортивного питания, специфических брендов, диетических продуктов или ресторанных меню) из файлов CSV/JSON, вручную пополнять и корректировать их, а также мгновенно находить эти продукты в общем поиске приложения для быстрого логирования в дневник питания.
- **Целевая аудитория**: Пользователи, ведущие учет питания и желающие иметь собственные автономные списки продуктов с точными макронутриентами без привязки к внешним сервисам, а также те, кто импортирует свои списки из других приложений или электронных таблиц.
- **Ключевая ценность**: Полная свобода в управлении пищевыми базами данных, локальная приватность и мгновенный доступ к своим продуктам при поиске.

---

## 2. User Experience & Visual Design

### Ключевые пользовательские сценарии

1. **Экран управления базами данных (Настройки → Продукты и ввод → Пользовательские базы данных)**:
   - Список существующих пользовательских баз с отображением названия, количества продуктов в базе, даты обновления и переключателя активности (включена/отключена в поиске).
   - Кнопка добавления базы:
     - *«Импортировать файл (CSV / JSON)»* с открытием системного выбора файлов (`ActivityResultContracts.GetContent` / `OpenDocument`).
     - *«Создать пустую базу»* для ручного наполнения продуктами с нуля.
     - *«Вставить текст (CSV / JSON)»* — удобная альтернатива для быстрой вставки скопированной таблицы или JSON без сохранения файла на диск.
   - Меню действий для базы: переименовать, экспортировать в CSV/JSON, удалить.

2. **Экран содержимого базы продуктов (детальный просмотр и редактирование)**:
   - Заголовок с названием базы, описанием и быстрым поиском/фильтром по списку продуктов внутри базы.
   - Список карточек продуктов: название, бренд (если указан), порция (например, 100 г или 1 порция) и компактная строка макронутриентов (Ккал, Б, Ж, У).
   - Кнопка быстрого добавления продукта (+ FAB) — открывает аккуратный Material 3 диалог / лист с полями ввода (название, бренд, калории, белки, жиры, углеводы, вес порции, единица измерения).
   - Клик по продукту открывает диалог редактирования для изменения любых параметров продукта.
   - Свайп или иконка корзины для удаления продукта из базы.
   - Кнопка дополнительного импорта (дозагрузка в существующую базу).

3. **Интеграция в поиск продуктов (Add Food Sheet)**:
   - При вводе поискового запроса продукты из активных пользовательских баз ищутся параллельно с локальными индексами (USDA/Swiss) и Open Food Facts.
   - В результатах поиска отображается уникальный бейдж с иконкой и именем базы (например: `[Моя база] · 100 г`).
   - Выбор продукта из пользовательской базы сразу заполняет форму добавления еды со всеми макросами и правильным источником данных (`NutrientSourceKind.CUSTOM`), сохраняя прозрачный учет provenance.

### Визуальный стиль и компоненты
- **Дизайн-система**: Material Design 3 в строгом соответствии с визуальной темой Chompass (`AppTheme`, закругления карточек `RoundedCornerShape(16.dp)`, контрастные акцентные цвета макросов).
- **Цветовая палитра макросов**: Белки — фиолетовый/синий акцент, Углеводы — теплый янтарный, Жиры — кораллово-красный, Калории — чистый primary.
- **Индикация состояний**: Понятные пустые экраны (Empty states) с иллюстративной иконкой `Icons.Outlined.Storage` и кнопкой быстрого импорта первого файла; индикаторы прогресса при разборе больших CSV файлов.

---

## 3. Key Product Decisions & Trade-Offs

- **Устойчивое локальное хранилище**:
  - *Выбранный подход*: Метаданные баз сохраняются через репозиторий в DataStore, а списки продуктов каждой базы сохраняются в формате JSON в приватном каталоге приложения (`context.filesDir/custom_food_dbs/{dbId}.json`).
  - *Почему*: Это изолирует базы друг от друга, обеспечивает быстрое атомарное чтение/запись, исключает лимиты размера DataStore Preferences и позволяет легко архивировать или экспортировать отдельные базы.
- **Интеллектуальный парсинг CSV**:
  - *Выбранный подход*: Автодетект разделителей (запятая `,`, точка с запятой `;`, табуляция `\t`), нормализация названий колонок без учета регистра, поддержка синонимов (русских и английских: `name`, `название`, `наименование`, `calories`, `ккал`, `калории`, `protein`, `белки`, `carbs`, `углеводы`, `fat`, `жиры`, `serving`, `порция`).
  - *Почему*: Пользователи загружают выгрузки из самых разных систем (Excel, MyFitnessPal, FatSecret, таблиц Google); автоматическое распознавание колонок предотвращает ошибки ручной настройки.
- **Поиск без блокировки UI**:
  - *Выбранный подход*: Индексация в памяти с токенизацией названий при открытии поиска через корутины (`Dispatchers.Default`).
  - *Почему*: Обеспечивает мгновенную фильтрацию со скоростью <5 мс даже при базах на тысячи записей, сохраняя плавность анимации интерфейса.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        User Interface (Compose)                        │
├─────────────────────────────────┬──────────────────────────────────────┤
│ AddFoodSearchSection            │ CustomFoodDatabasesScreen            │
│ (unified search with badge)     │ (manage, import, export, delete)     │
│                                 │ CustomFoodDatabaseDetailScreen       │
│                                 │ (browse, add, edit, delete food)     │
└────────────────┬────────────────┴──────────────────┬───────────────────┘
                 │                                   │
                 ▼                                   ▼
┌─────────────────────────────────┐ ┌────────────────────────────────────┐
│ FoodDatabaseSearch              │ │ CustomFoodDatabaseRepository       │
│ - queries active custom dbs     │ │ - manages DB metadata & disk JSON  │
│ - scores & normalizes hits      │ │ - CSV/JSON parser & serializer     │
│ - maps to DatabaseSearchResult  │ │ - CRUD operations on foods & DBs   │
└────────────────┬────────────────┘ └────────────────┬───────────────────┘
                 │                                   │
                 ▼                                   ▼
┌─────────────────────────────────┐ ┌────────────────────────────────────┐
│ toAnalysis() Converter          │ │ App Files Directory                │
│ - turns Custom hit into         │ │ /custom_food_dbs/{db_id}.json      │
│   verified FoodAnalysis         │ │ + PreferencesStore (registry)      │
└─────────────────────────────────┘ └────────────────────────────────────┘
```

### Модели данных

```kotlin
// app/chompass/models/CustomFoodDatabase.kt
@Serializable
data class CustomFoodDatabase(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val enabled: Boolean = true,
    val itemCount: Int = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
)

@Serializable
data class CustomFoodItem(
    val id: String = UUID.randomUUID().toString(),
    val databaseId: String,
    val name: String,
    val brand: String? = null,
    val servingSizeGrams: Double = 100.0,
    val servingUnitName: String = "g",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodiumMg: Double? = null,
    val barcode: String? = null,
    val notes: String? = null,
)
```

### Основные компоненты реализации

1. **`CustomFoodDatabaseRepository`**:
   - `databases: StateFlow<List<CustomFoodDatabase>>`
   - `getDatabaseFoods(databaseId: String): List<CustomFoodItem>`
   - `saveDatabase(database: CustomFoodDatabase, foods: List<CustomFoodItem>)`
   - `deleteDatabase(databaseId: String)`
   - `addFoodItem(databaseId: String, item: CustomFoodItem)`
   - `updateFoodItem(databaseId: String, item: CustomFoodItem)`
   - `deleteFoodItem(databaseId: String, itemId: String)`
   - `parseCsv(content: String, defaultDbName: String): Pair<CustomFoodDatabase, List<CustomFoodItem>>`
   - `parseJson(content: String, defaultDbName: String): Pair<CustomFoodDatabase, List<CustomFoodItem>>`
   - `exportToCsv(database: CustomFoodDatabase, foods: List<CustomFoodItem>): String`
   - `exportToJson(database: CustomFoodDatabase, foods: List<CustomFoodItem>): String`

2. **Расширение `NutrientSourceKind` и `FoodDatabaseSearch`**:
   - Добавление `CUSTOM` в `NutrientSourceKind`.
   - В `FoodDatabaseSearch`: добавление поиска по активным базам из `CustomFoodDatabaseRepository`.
   - Обработка `NutrientSourceKind.CUSTOM` в методе `toAnalysis(result: DatabaseSearchResult)` для создания `FoodAnalysis`.
   - Отображение бейджа с названием базы в результатах поиска.

3. **UI экраны и навигация**:
   - Маршрут `custom_food_databases` в `ChompassRoutes` / `ChompassNavHost`.
   - Пункт в `FoodEntrySettingsScreen`: «Пользовательские базы продуктов» с бейджем количества баз.
   - `CustomFoodDatabasesScreen`: список баз, переключатели, диалог создания/импорта, меню экспорта и удаления.
   - `CustomFoodDatabaseDetailScreen`: список продуктов базы, поиск, диалог добавления/редактирования продукта (`AddEditFoodItemDialog`), подтверждение удаления.

4. **Тесты**:
   - Тестирование парсинга CSV с различными разделителями и русскими/английскими заголовками.
   - Тестирование JSON парсинга.
   - Тестирование CRUD операций и поиска в `FoodDatabaseSearchTest`.
