# Thorium Package Structure

## Maven Modules

```
thorium/
├── pom.xml                          # Parent POM
├── thorium-domain/
├── thorium-application/
├── thorium-infrastructure/
└── thorium-ui/
```

## thorium-domain

```
com.thorium.domain
├── model
│   ├── Teacher
│   ├── Subject
│   ├── ClassStream
│   ├── TeachingAssignment
│   ├── TeacherAvailability
│   ├── Period
│   ├── BreakPeriod
│   ├── Constraint
│   ├── Timetable
│   ├── TimetableEntry
│   └── ScheduleSlot
├── constraint
│   ├── HardConstraintValidator
│   └── SoftConstraintScorer
├── scheduling
│   ├── SchedulingContext
│   ├── GreedyScheduler
│   ├── BacktrackingScheduler
│   ├── TimetableGenerator
│   └── optimization
│       ├── OptimizationStrategy      # extension point
│       ├── GeneticAlgorithmStrategy  # stub for Phase 3
│       ├── TabuSearchStrategy        # stub for Phase 3
│       └── SimulatedAnnealingStrategy # stub for Phase 3
├── exception
│   └── SchedulingException
└── value
    ├── DayOfWeek
    └── TimetableStatus
```

## thorium-application

```
com.thorium.application
├── port
│   ├── TeacherRepository
│   ├── SubjectRepository
│   ├── ClassStreamRepository
│   ├── TeachingAssignmentRepository
│   ├── TeacherAvailabilityRepository
│   ├── PeriodRepository
│   ├── BreakRepository
│   ├── ConstraintRepository
│   ├── TimetableRepository
│   └── TimetableExporter
├── dto
│   ├── TeacherDto
│   ├── SubjectDto
│   └── ...
├── usecase
│   ├── teacher
│   ├── subject
│   ├── classstream
│   ├── assignment
│   ├── period
│   ├── break
│   ├── availability
│   ├── timetable
│   └── export
└── service
    └── ApplicationServiceFactory
```

## thorium-infrastructure

```
com.thorium.infrastructure
├── persistence
│   ├── SQLiteConnectionProvider
│   ├── DatabaseInitializer
│   └── repository
│       ├── SqliteTeacherRepository
│       └── ...
└── export
    ├── PdfTimetableExporter
    └── ExcelTimetableExporter
```

## thorium-ui

```
com.thorium.ui
├── ThoriumApp
├── di
│   └── AppContext
├── controller
│   ├── DashboardController
│   ├── TeacherManagementController
│   └── ...
├── view
│   └── (FXML resources under resources/fxml/)
└── validation
    └── FormValidators
```
