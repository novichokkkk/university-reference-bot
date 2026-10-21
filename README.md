# university-reference-bot

Учебный проект по дисциплине «Конструирование программного обеспечения».

## Стек

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- JUnit

## Запуск

Убедись, что PostgreSQL запущен, затем выполни:

```bash
mvn spring-boot:run
```

Проверка доступна по адресу: http://localhost:8080/

Тесты используют встроенную H2-базу:

```bash
mvn test
```

## Структура для лабораторной работы №5

- `config/checkstyle/checkstyle.xml` — правила статического анализа Java-кода;
- `.github/workflows/ci.yml` — сборка, тесты, Checkstyle и JaCoCo;
- `.github/workflows/commitlint.yml` — проверка Conventional Commits;
- `scripts/collect-metrics.py` — сбор основных метрик проекта;
- `metrics/stage-metrics.csv` — накопление метрик по этапам разработки.
