# Domain Model

## Entities

### Teacher
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| code | String | Unique identifier (e.g. T001) |
| name | String | Full name |
| maxLessonsPerDay | int | Soft workload limit |
| maxLessonsPerWeek | int | Soft workload limit |
| active | boolean | Whether available for scheduling |

### Subject
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| code | String | Unique code (e.g. S001) |
| name | String | Subject name |
| examinable | boolean | CBC examinable flag |
| cbcDefaultLessons | int | Default weekly lessons when examinable |
| allowsDoublePeriod | boolean | Whether double periods are permitted |

### ClassStream
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| code | String | Unique code (e.g. F1E) |
| form | int | Form/grade level |
| stream | String | Stream name |
| displayName | String | Human-readable label |

### TeachingAssignment
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| teacherId | Long | FK → teachers |
| subjectId | Long | FK → subjects |
| classStreamId | Long | FK → class_streams |
| lessonsPerWeek | int | Required weekly lesson count |

### TeacherAvailability
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| teacherId | Long | FK → teachers |
| dayOfWeek | DayOfWeek | Day |
| periodNumber | int | Period index (1-based) |
| available | boolean | false = unavailable slot |

### Period
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| periodNumber | int | Order within day (1-based) |
| startTime | LocalTime | Period start |
| endTime | LocalTime | Period end |
| label | String | Display label (P1, P2, …) |

### BreakPeriod
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| name | String | Break name |
| afterPeriod | int | Inserted after this period (0 = before P1) |
| durationMinutes | int | Break length |
| sortOrder | int | Display order |

### Constraint (school-level rules)
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| constraintType | ConstraintType | Rule identifier |
| enabled | boolean | Active flag |
| parameters | String | JSON parameters |

### Timetable
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| name | String | Timetable name |
| status | TimetableStatus | DRAFT, GENERATED, PUBLISHED |
| createdAt | LocalDateTime | Creation timestamp |
| qualityScore | double | Soft-constraint score |

### TimetableEntry
| Attribute | Type | Description |
|-----------|------|-------------|
| id | Long | Primary key |
| timetableId | Long | FK → timetables |
| teachingAssignmentId | Long | FK → teaching_assignments |
| dayOfWeek | DayOfWeek | Scheduled day |
| periodNumber | int | Scheduled period |

## Value Objects

### ScheduleSlot
Immutable pair of `(DayOfWeek, periodNumber)` representing a schedulable slot.

### DayOfWeek
Enum: MONDAY … FRIDAY (configurable working days).

### TimetableStatus
Enum: DRAFT, GENERATED, PUBLISHED, ARCHIVED.

## Domain Services

- **HardConstraintValidator** — validates teacher clash, class clash, availability, lesson counts, CBC no-double rules.
- **SoftConstraintScorer** — scores spread, workload balance, consecutive-lesson penalty.
- **TimetableGenerator** — orchestrates greedy + backtracking phases.
