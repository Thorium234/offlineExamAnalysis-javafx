# ER Diagram

```mermaid
erDiagram
    teachers ||--o{ teaching_assignments : "assigned to"
    teachers ||--o{ teacher_availability : "has"
    subjects ||--o{ teaching_assignments : "taught in"
    class_streams ||--o{ teaching_assignments : "receives"
    timetables ||--o{ timetable_entries : "contains"
    teaching_assignments ||--o{ timetable_entries : "scheduled as"

    teachers {
        bigint id PK
        text code UK
        text name
        int max_lessons_per_day
        int max_lessons_per_week
        boolean active
    }

    subjects {
        bigint id PK
        text code UK
        text name
        boolean examinable
        int cbc_default_lessons
        boolean allows_double_period
    }

    class_streams {
        bigint id PK
        text code UK
        int form
        text stream
        text display_name
    }

    teaching_assignments {
        bigint id PK
        bigint teacher_id FK
        bigint subject_id FK
        bigint class_stream_id FK
        int lessons_per_week
    }

    teacher_availability {
        bigint id PK
        bigint teacher_id FK
        text day_of_week
        int period_number
        boolean available
    }

    periods {
        bigint id PK
        int period_number UK
        text start_time
        text end_time
        text label
    }

    breaks {
        bigint id PK
        text name
        int after_period
        int duration_minutes
        int sort_order
    }

    constraints {
        bigint id PK
        text constraint_type
        boolean enabled
        text parameters
    }

    timetables {
        bigint id PK
        text name
        text status
        text created_at
        real quality_score
    }

    timetable_entries {
        bigint id PK
        bigint timetable_id FK
        bigint teaching_assignment_id FK
        text day_of_week
        int period_number
    }
```

## Relationships

- **teaching_assignments** is the central scheduling unit linking teacher, subject, and class.
- **teacher_availability** stores per-slot availability; `available = 0` means forbidden.
- **periods** and **breaks** define the school calendar (no FK to assignments).
- **timetable_entries** materialize a generated timetable; each row is one lesson placement.
