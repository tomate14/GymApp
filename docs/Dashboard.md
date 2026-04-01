# Dashboard

## Objetivo
Este documento resume como funciona hoy el dashboard del panel de administracion de Fitexa.

La idea es dejar trazadas:

- las reglas de negocio de cada KPI, grafico y tabla
- las consultas o estrategias usadas para recuperar y transformar datos
- las tablas y relaciones involucradas
- los criterios de fecha
- los puntos que pueden generar confusion o desalineacion funcional

## Punto de entrada

Backend:

- Controller: [apps/fitexa/src/main/java/com/fitexa/controler/DashboardControler.java](../apps/fitexa/src/main/java/com/fitexa/controler/DashboardControler.java)
- Service principal: [apps/fitexa/src/main/java/com/fitexa/services/impl/DashboardServiceImpl.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/DashboardServiceImpl.java)
- Frontend admin: [apps/admin-panel/src/app/components/dashboard/dashboard.component.ts](../apps/admin-panel/src/app/components/dashboard/dashboard.component.ts)

Endpoints principales:

- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/students-trend`
- `GET /api/v1/dashboard/renewals`
- `GET /api/v1/dashboard/classes`
- `GET /api/v1/dashboard/activity-ranking`
- `GET /api/v1/dashboard/plan-overloads`

## Criterios de fechas

### Rango global
El `summary` usa un rango global `fromDate` / `toDate`.

Si no se envian fechas:

- `toDate = hoy`
- `fromDate = toDate - 1 mes`

Si `fromDate > toDate`:

- el backend rehace el rango a `toDate - 1 mes` hasta `toDate`

Referencia:

- [DashboardServiceImpl.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/DashboardServiceImpl.java)

### Bloques que usan el rango global

- KPI `Altas de alumnos`
- KPI `Profesores con clases`
- KPI `Cuotas al dia`
- KPI `Vencimientos en periodo`
- KPI `Clases en periodo`
- KPI `Ocupacion promedio`
- KPI `Rutinas en periodo`
- KPI `Ingresos por renovaciones`
- Alertas del summary
- `Ocupacion promedio por actividad`
- `Top 10 ejercicios mas usados`
- `Top 10 maquinas con mayor presion por unidad`
- `Vencimientos de cuotas`
- `Ranking de actividades`
- `Sobreasignacion de planes`
- `Agenda de clases`

### Bloques que no usan el rango global

- `Alumnos totales`
- `Clases por actividad`
- `Alumnos por profesor`
- `Estado de cuotas`
- `Alumnos por plan`

### Bloque con rango propio

- `Altas de alumnos por mes / por ano`

## Modelo de datos involucrado

### Tablas principales

- `users`
- `plans`
- `user_payment_movements`
- `gym_classes`
- `gym_class_days`
- `gym_class_subscriptions`
- `activities`
- `routine_assignments`
- `routine_assignment_days`
- `routine_assignment_exercises`
- `exercises`
- `machines`

### Relaciones relevantes

Clases:

- `gym_classes.activity_id -> activities.id`
- `gym_classes.teacher_id -> users.id`
- `gym_class_days.gym_class_id -> gym_classes.id`
- `gym_class_subscriptions.gym_class_id -> gym_classes.id`
- `gym_class_subscriptions.user_id -> users.id`

Rutinas:

- `routine_assignments.user_id -> users.id`
- `routine_assignments.routine_template_id -> routine_templates.id`
- `routine_assignment_days.routine_assignment_id -> routine_assignments.id`
- `routine_assignment_exercises.assignment_id -> routine_assignments.id`
- `routine_assignment_exercises.exercise_id -> exercises.id`
- `exercises.machine_id -> machines.id`
- `exercises.activity_id -> activities.id`

Pagos:

- `user_payment_movements.user_id -> users.id`
- `users.plan_id -> plans.id`

## Como se construyen las ocurrencias de clases

Buena parte del dashboard ya no trabaja sobre la clase generica, sino sobre la idea de `ocurrencia diaria`.

La logica vive en:

- [DashboardClassMetricsService.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardClassMetricsService.java)

Regla:

- si la clase tiene `weekDays`, se genera una ocurrencia por cada fecha del rango que coincide con alguno de esos dias
- si no tiene `weekDays`, se toma como clase puntual en `gym_classes.date`
- para cada ocurrencia se calculan:
  - inscriptos
  - asistentes
  - capacidad maxima
  - ocupacion

Datos usados para cada ocurrencia:

- inscriptos: `gym_class_subscriptions`
- asistentes: `gym_class_subscriptions.attendance_present = true`
- cupo: `gym_classes.maxCapacity`

Formula actual de ocupacion:

- `attendees * 100 / maxCapacity`

Detalle importante:

- la ocupacion usa asistencia real
- la cantidad de inscriptos usa suscripciones del dia
- una ocurrencia puede existir aunque no tenga inscriptos

## KPI del summary

### Alumnos totales
Regla:

- cuenta todos los usuarios `ALUMNO`

Fuente:

- `userRepository.countByUserType(UserType.ALUMNO)`

Tablas:

- `users`

### Altas de alumnos
Regla:

- cuenta alumnos dados de alta entre `fromDate` y `toDate`

Fuente:

- `userRepository.countByUserTypeAndRegistrationDateBetween(...)`

Tablas:

- `users.registration_date`

### Profesores con clases
Regla:

- cuenta profesores distintos que aparecen en ocurrencias de clase del periodo
- solo cuenta clases con profesor asignado

Implementacion:

- `dashboardClassMetricsService.listOccurrences(fromDate, toDate)`
- luego `distinct()` por `teacherId`

Tablas:

- `gym_classes`
- `gym_class_days`
- `users`

### Cuotas al dia
Regla:

- cuenta alumnos distintos que registraron una renovacion cobrada en el periodo
- ademas el alumno debe quedar con `payment_status = AL_DIA`

Fuente:

- `user_payment_movements.detail = 'Renovacion de cuota'`
- `paid_at between fromDate and toDate`
- `m.user.paymentStatus = AL_DIA`

Implementacion:

- `userPaymentMovementRepository.countDistinctUsersByDetailAndPaymentStatusAndPaidAtBetween(...)`

Tablas:

- `user_payment_movements`
- `users`

### Vencimientos en periodo
Regla:

- cuenta alumnos cuya `renewal_date` cae dentro del periodo

Fuente:

- `userRepository.countByUserTypeAndRenewalDateBetween(...)`

Tablas:

- `users.renewal_date`

### Clases en periodo
Regla:

- cuenta ocurrencias de clase generadas dentro del rango

Implementacion:

- `dashboardClassMetricsService.listOccurrences(fromDate, toDate).size()`

Tablas:

- `gym_classes`
- `gym_class_days`

### Ocupacion promedio
Regla:

- promedio de ocupacion de las ocurrencias del periodo
- solo toma ocurrencias con `maxCapacity > 0`

Formula:

- promedio de `attendees * 100 / maxCapacity`

Tablas:

- `gym_classes`
- `gym_class_subscriptions`

### Rutinas en periodo
Regla:

- cuenta rutinas cuya vigencia se cruza con el rango

Fuente:

- `routine_assignments.start_date <= toDate`
- `routine_assignments.end_date >= fromDate`

Implementacion:

- `routineAssignmentRepository.countAssignmentsOverlappingRange(...)`

### Ingresos por renovaciones
Regla:

- suma importes cobrados por movimientos con detalle `Renovacion de cuota` dentro del periodo

Implementacion:

- `userPaymentMovementRepository.sumAmountByDetailAndPaidAtBetween(...)`

Tablas:

- `user_payment_movements.amount`
- `user_payment_movements.paid_at`

## Alertas del summary

### Alumnos sin plan
Regla:

- alumnos sin plan dados de alta dentro del periodo

Fuente:

- `countByUserTypeAndPlanIsNullAndRegistrationDateBetween(...)`

### Clases sin profesor
Regla:

- ocurrencias de clase del periodo que no tienen profesor asignado

### Clases al limite
Regla:

- ocurrencias de clase del periodo donde `enrolled >= maxCapacity`

Detalle importante:

- hoy esta alerta usa inscriptos del dia
- no usa asistentes

### Rutinas vencidas
Regla:

- rutinas cuya `endDate` cae dentro del periodo

## Graficos

### Altas de alumnos por mes / por ano
Archivo:

- [DashboardStudentTrendChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardStudentTrendChartStrategy.java)

Regla:

- cuenta altas de alumnos agrupadas por mes o por ano

Consulta usada:

- mensual: `countStudentRegistrationsByMonth`
- anual: `countStudentRegistrationsByYear`

SQL mensual:

```sql
select date_format(u.registration_date, '%Y-%m') as period, count(u.id) as total
from users u
where u.user_type = 'ALUMNO'
  and u.registration_date >= :fromDate
  and u.registration_date <= :toDate
group by date_format(u.registration_date, '%Y-%m')
order by period asc;
```

SQL anual:

```sql
select cast(year(u.registration_date) as char) as period, count(u.id) as total
from users u
where u.user_type = 'ALUMNO'
  and u.registration_date >= :fromDate
  and u.registration_date <= :toDate
group by cast(year(u.registration_date) as char)
order by cast(year(u.registration_date) as char) asc;
```

### Clases por actividad
Archivo:

- [DashboardClassesByActivityChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardClassesByActivityChartStrategy.java)

Regla actual:

- cuenta ocurrencias de clase por actividad
- no cuenta alumnos
- no cuenta asistencia
- no usa el rango global del dashboard
- toma desde la primera clase registrada hasta hoy

Implementacion:

- `gymClassRepository.findEarliestClassDate()`
- `dashboardClassMetricsService.listOccurrences(fromDate, today)`
- agrupacion por `activityName`

Importante:

- este grafico mide presencia de agenda, no demanda ni asistencia

### Ocupacion promedio por actividad
Archivo:

- [DashboardOccupancyByActivityChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardOccupancyByActivityChartStrategy.java)

Regla:

- agrupa las ocurrencias del rango por actividad
- promedia `occupancyPercent`
- `occupancyPercent` ya viene calculado con asistencia real

Formula base por ocurrencia:

- `attendees * 100 / maxCapacity`

### Alumnos por profesor
Archivo:

- [DashboardStudentsByTeacherChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardStudentsByTeacherChartStrategy.java)

Regla:

- cuenta alumnos activos distintos por profesor
- alumno activo significa:
  - `payment_status = AL_DIA`
  - `renewal_date >= hoy`
  - `registration_date <= hoy`
- toma suscripciones a clases hasta la fecha actual
- no usa el rango global del dashboard

Consulta usada:

- `GymClassSubscriptionRepository.countActiveStudentsByTeacher(referenceDate)`

JPQL:

```sql
select concat(t.firstName, ' ', t.lastName) as teacherName,
       count(distinct s.user.id) as studentsCount
from GymClassSubscriptionEntity s
join s.gymClass gc
join gc.teacher t
join s.user u
where u.userType = ALUMNO
  and u.paymentStatus = AL_DIA
  and u.renewalDate >= :referenceDate
  and u.registrationDate <= :referenceDate
  and s.targetDate <= :referenceDate
group by t.id, t.firstName, t.lastName
order by count(distinct s.user.id) desc
```

### Estado de cuotas
Archivo:

- [DashboardPaymentStatusChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardPaymentStatusChartStrategy.java)

Regla:

- agrupa alumnos por estado de pago
- solo considera alumnos con `registrationDate <= hoy`
- no usa el rango global

Fuente:

- `userRepository.countUsersByPaymentStatusUpToDate(...)`

### Alumnos por plan
Archivo:

- [DashboardPlanDistributionChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardPlanDistributionChartStrategy.java)

Regla:

- agrupa alumnos activos por plan
- alumno activo significa:
  - `payment_status = AL_DIA`
  - `renewal_date >= hoy`
  - `registration_date <= hoy`
- no usa el rango global

Consulta usada:

- `userRepository.countActiveUsersByPlan(...)`

### Top 10 ejercicios mas usados
Archivo:

- [DashboardTopExercisesChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardTopExercisesChartStrategy.java)

Regla actual:

- cuenta cuantas veces aparece cada ejercicio en rutinas cuya vigencia se cruza con el rango
- no cuenta ejecuciones reales por dia
- no cuenta si el alumno efectivamente completo ese entrenamiento

JPQL:

```sql
select item.exercise.id as exerciseId,
       item.exerciseName as exerciseName,
       count(item.id) as usageCount
from RoutineAssignmentExerciseEntity item
where item.assignment.startDate <= :toDate
  and item.assignment.endDate >= :fromDate
group by item.exercise.id, item.exerciseName
order by count(item.id) desc
```

Tablas:

- `routine_assignment_exercises`
- `routine_assignments`
- `exercises`

### Top 10 maquinas con mayor presion por unidad
Archivo:

- [DashboardTopMachinesChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardTopMachinesChartStrategy.java)
- [RoutineAssignmentExerciseRepository.java](../apps/fitexa/src/main/java/com/fitexa/repository/RoutineAssignmentExerciseRepository.java)

Regla actual:

- usa solo rutinas, no clases
- toma dias efectivos de rutina ya transcurridos
- no cuenta hoy ni dias futuros
- para cada dia de rutina y maquina cuenta una sola ocurrencia por asignacion y fecha
- divide el total por la `quantity` de la maquina

Formula:

- `presion = usos_de_dias_efectivos / cantidad_de_unidades`

Detalle importante:

- si `toDate` es hoy o futuro, el backend usa `hoy - 1`
- si una maquina tiene `quantity = 0`, se fuerza a `1`

SQL nativo:

```sql
with recursive routine_days as (
    select ra.id as assignment_id,
           greatest(ra.start_date, :fromDate) as target_date,
           least(ra.end_date, :toDate) as end_date
    from routine_assignments ra
    where ra.start_date <= :toDate
      and ra.end_date >= :fromDate

    union all

    select rd.assignment_id,
           date_add(rd.target_date, interval 1 day) as target_date,
           rd.end_date
    from routine_days rd
    where rd.target_date < rd.end_date
)
select machine_stats.machine_id as machineId,
       machine_stats.machine_name as machineName,
       round(count(*) / machine_stats.machine_quantity, 2) as usageCount
from (
    select distinct
        rd.assignment_id,
        rd.target_date,
        m.id as machine_id,
        m.name as machine_name,
        greatest(m.quantity, 1) as machine_quantity
    from routine_days rd
    join routine_assignment_days rad
        on rad.routine_assignment_id = rd.assignment_id
    join routine_assignment_exercises rae
        on rae.assignment_id = rd.assignment_id
    join exercises e
        on e.id = rae.exercise_id
    join machines m
        on m.id = e.machine_id
    where e.machine_id is not null
      and rad.day = case dayofweek(rd.target_date)
          when 1 then 'DOMINGO'
          when 2 then 'LUNES'
          when 3 then 'MARTES'
          when 4 then 'MIERCOLES'
          when 5 then 'JUEVES'
          when 6 then 'VIERNES'
          when 7 then 'SABADO'
      end
) machine_stats
group by machine_stats.machine_id, machine_stats.machine_name, machine_stats.machine_quantity
order by usageCount desc, machine_stats.machine_name asc
limit 10;
```

## Tablas del dashboard

### Vencimientos de cuotas
Metodo:

- `DashboardServiceImpl.findRenewals(...)`

Criterio:

- solo alumnos
- filtros por nombre, DNI, plan, estado de pago y rango de renovacion

Fuente:

- `DashboardUserSpecifications.buildRenewalsFilter(...)`
- `UserRepository.findAll(specification, pageable)`

Tablas:

- `users`
- `plans`

### Agenda de clases
Metodo:

- `DashboardServiceImpl.findClasses(...)`

Criterio:

- primero genera ocurrencias diarias reales dentro del rango
- luego aplica filtros de nombre de clase, actividad y profesor
- despues pagina en memoria

Fuente de filtros:

- `DashboardGymClassSpecifications.buildClassScheduleFilter(...)`

Tablas:

- `gym_classes`
- `gym_class_days`
- `gym_class_subscriptions`
- `activities`
- `users`

### Ranking de actividades
Metodo:

- `DashboardServiceImpl.findActivityRanking(...)`

Regla:

- arma ocurrencias diarias de clases del rango
- agrupa por actividad
- cuenta:
  - `classesCount`: cantidad de ocurrencias
  - `totalEnrolled`: suma de inscriptos del dia
  - `averageOccupancyPercent`: promedio de ocupacion por ocurrencia

Importante:

- `totalEnrolled` usa inscriptos del dia
- `averageOccupancyPercent` usa asistencia real

### Sobreasignacion de planes
Metodo:

- `DashboardServiceImpl.findPlanOverloads(...)`

Regla:

- detecta alumnos que tienen mas dias de rutina asignados que los permitidos por su plan
- compara `count(distinct rad.day)` contra `plans.max_days_per_week`

SQL base:

```sql
select u.id as userId,
       u.first_name as firstName,
       u.last_name as lastName,
       u.dni as dni,
       p.name as planName,
       p.max_days_per_week as maxDaysPerWeek,
       count(distinct rad.day) as assignedDays
from users u
join plans p on p.id = u.plan_id
join routine_assignments ra on ra.user_id = u.id
join routine_assignment_days rad on rad.routine_assignment_id = ra.id
where u.user_type = 'ALUMNO'
  and ra.start_date <= :toDate
  and ra.end_date >= :fromDate
group by u.id, u.first_name, u.last_name, u.dni, p.name, p.max_days_per_week
having count(distinct rad.day) > p.max_days_per_week
```

## Orden de los graficos en el summary

El `summary` arma la lista de charts con `DashboardChartStrategy`, ordenados por `order()`.

Orden actual:

- `students-by-plan` -> 10
- `payment-status` -> 20
- `students-trend` -> 30
- `classes-by-activity` -> 40
- `occupancy-by-activity` -> 50
- `students-by-teacher` -> 60
- `top-exercises` -> 70
- `top-machines` -> 80

Referencia:

- [DashboardChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/DashboardChartStrategy.java)

## Diferencias importantes entre metricas

Hay dos conceptos distintos en el dashboard y conviene no mezclarlos:

- `inscriptos`
- `asistentes`

Inscriptos:

- salen de `gym_class_subscriptions`
- cuentan a quien reservo la clase de ese dia

Asistentes:

- salen de `gym_class_subscriptions.attendance_present = true`
- cuentan a quien fue marcado como presente

Hoy:

- `Ranking de actividades.totalEnrolled` usa inscriptos
- `Ocupacion promedio` y `Ocupacion por actividad` usan asistentes
- `Clases por actividad` no usa ni inscriptos ni asistentes

## Asunciones y limites actuales

### Clases por actividad
No mide asistencia ni demanda. Solo mide ocurrencias de agenda por actividad.

### Top ejercicios mas usados
No mide ejecucion diaria real. Mide presencia del ejercicio en rutinas cuya vigencia se cruza con el rango.

### Top maquinas
Mide presion por unidad usando dias efectivos de rutina ya transcurridos. No incluye clases.

### Agenda de clases
Se pagina en memoria despues de generar la lista completa de ocurrencias. Si la cantidad de clases creciera mucho, este punto puede merecer optimizacion.

### Vencimientos y pagos
El dashboard considera `Renovacion de cuota` como texto de negocio fijo para identificar renovaciones cobradas.

Constante actual:

- `RENEWAL_DETAIL = "Renovacion de cuota"`

Si ese texto cambia en los movimientos, se rompe el calculo de:

- `Cuotas al dia`
- `Ingresos por renovaciones`

## Archivos mas relevantes

- [apps/fitexa/src/main/java/com/fitexa/controler/DashboardControler.java](../apps/fitexa/src/main/java/com/fitexa/controler/DashboardControler.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/DashboardServiceImpl.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/DashboardServiceImpl.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardClassMetricsService.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardClassMetricsService.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardClassesByActivityChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardClassesByActivityChartStrategy.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardOccupancyByActivityChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardOccupancyByActivityChartStrategy.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardStudentsByTeacherChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardStudentsByTeacherChartStrategy.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardPaymentStatusChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardPaymentStatusChartStrategy.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardPlanDistributionChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardPlanDistributionChartStrategy.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardTopExercisesChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardTopExercisesChartStrategy.java)
- [apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardTopMachinesChartStrategy.java](../apps/fitexa/src/main/java/com/fitexa/services/impl/dashboard/DashboardTopMachinesChartStrategy.java)
- [apps/fitexa/src/main/java/com/fitexa/repository/UserRepository.java](../apps/fitexa/src/main/java/com/fitexa/repository/UserRepository.java)
- [apps/fitexa/src/main/java/com/fitexa/repository/UserPaymentMovementRepository.java](../apps/fitexa/src/main/java/com/fitexa/repository/UserPaymentMovementRepository.java)
- [apps/fitexa/src/main/java/com/fitexa/repository/GymClassRepository.java](../apps/fitexa/src/main/java/com/fitexa/repository/GymClassRepository.java)
- [apps/fitexa/src/main/java/com/fitexa/repository/GymClassSubscriptionRepository.java](../apps/fitexa/src/main/java/com/fitexa/repository/GymClassSubscriptionRepository.java)
- [apps/fitexa/src/main/java/com/fitexa/repository/RoutineAssignmentRepository.java](../apps/fitexa/src/main/java/com/fitexa/repository/RoutineAssignmentRepository.java)
- [apps/fitexa/src/main/java/com/fitexa/repository/RoutineAssignmentExerciseRepository.java](../apps/fitexa/src/main/java/com/fitexa/repository/RoutineAssignmentExerciseRepository.java)

## Recomendacion para soporte futuro

Si aparece un desvio en algun numero del dashboard, conviene revisar en este orden:

1. que criterio funcional esperaba el usuario
2. si el bloque usa rango global, rango propio o estado actual
3. si la metrica usa inscriptos o asistentes
4. si el calculo sale de JPQL/SQL directo o de ocurrencias armadas en memoria
5. si el texto del popup del frontend sigue representando correctamente la regla de negocio actual
