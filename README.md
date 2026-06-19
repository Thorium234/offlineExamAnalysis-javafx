# Thorium Timetable Generator

Offline desktop school timetable generator built with Java 21, JavaFX, SQLite, and Clean Architecture.

## Architecture

| Module | Responsibility |
|--------|----------------|
| `thorium-domain` | Entities, constraints, scheduling engine (greedy + backtracking) |
| `thorium-application` | Use cases and repository ports |
| `thorium-infrastructure` | SQLite persistence, PDF/Excel export |
| `thorium-ui` | JavaFX views and controllers |

Design documentation: `.ai/design/`

## Requirements

- Java 21+
- Maven 3.9+

## Build

```bash
mvn clean install
```

## Run

```bash
cd thorium-ui
mvn javafx:run
```

Database file: `~/.thorium/timetable.db`

## Features

- Teacher, subject, class, and assignment management
- Period and break configuration
- Teacher availability (unavailable slots)
- Timetable generation with hard constraints:
  - No teacher/class clashes
  - Teacher availability respected
  - Weekly lesson counts enforced
  - CBC no-double-lesson rule
- Soft constraint scoring (spread, workload, consecutive lessons)
- PDF and Excel export
- Phase 3 optimization extension points (stubs only)

## Scheduling Phases

1. **Greedy** — places hardest assignments first using soft-constraint scoring
2. **Backtracking** — resolves conflicts when greedy placement is incomplete
3. **Optimization** — extension points prepared for GA, Tabu Search, Simulated Annealing
