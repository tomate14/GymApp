# Asumpciones Del Sistema

Este documento resume las decisiones y asumpciones que quedaron implementadas en Fitexa y que pueden explicar comportamientos del sistema, especialmente cuando algo no coincide con la expectativa funcional.

## Roles Y Acceso

- Existe una tabla `roles` con permisos por modulo del panel de administracion.
- Los modulos posibles hoy son:
  - `Dashboard`
  - `Agenda`
  - `Usuarios`
  - `Planes`
  - `Actividades`
  - `Clases`
  - `Rutinas`
  - `Ejercicios`
  - `Maquinas`
  - `Roles y permisos`
- El menu lateral del admin-panel se muestra segun los `menuItems` que devuelve el login.
- La navegacion por URL del admin-panel tambien se filtra por esos `menuItems`.
- Hoy la restriccion por rol esta aplicada en el frontend del panel y en sus guards de rutas.
- Todavia no esta implementado un control fino por rol y endpoint en todo el backend. Eso sigue pendiente.

## Roles Base

- El bootstrap crea por defecto estos roles base:
  - `Administrador`
  - `Profesor`
  - `Alumno`
- `Administrador` puede ver todos los modulos del panel.
- `Profesor` puede ver todos los modulos excepto `Dashboard` y `Roles y permisos`.
- `Alumno` no tiene ningun modulo visible en el panel.
- Se pueden crear roles custom adicionales desde la pantalla `Roles y permisos`.

## Alta De Usuarios

- En alta y modificacion de usuarios, el dato principal es el `Rol`.
- El sistema ya no pide simultaneamente `Rol` y `Perfil del sistema` en la UI.
- El backend deriva automaticamente el perfil interno desde el rol elegido.

## Derivacion Interna Del Perfil

- Si el rol elegido se llama `Alumno`, el usuario queda internamente como `ALUMNO`.
- Si el rol elegido se llama `Administrador`, el usuario queda internamente como `ADMIN`.
- Cualquier otro rol queda internamente como `PROFESOR`.
- Esta derivacion es intencional para no romper la logica existente de clases, rutinas, pagos, dashboard y mobile.
- Esta regla hoy depende del nombre del rol, comparado sin distinguir mayusculas/minusculas.

## Alumno Vs Panel Admin

- La asumpcion actual es que un `Alumno` no usa el panel de administracion.
- Un `Alumno` esta pensado para operar sobre la app mobile.
- Un `Profesor`, `Administrador` o cualquier rol custom no `Alumno` puede acceder al panel de administracion segun sus permisos.

## Planes Y Pagos

- Solo los usuarios derivados como `ALUMNO` pueden tener plan asignado.
- Solo los usuarios derivados como `ALUMNO` pueden renovar cuota.
- Si un usuario pasa a un rol no `Alumno`, el sistema limpia el plan en ciertas operaciones de actualizacion/asignacion.
- Las reglas de renovacion, dashboard de cuotas y pagos siguen apoyadas en la clasificacion interna `ALUMNO`.

## Compatibilidad Con Logica Existente

- Muchas partes del backend siguen usando el concepto tecnico `userType` para reglas de negocio.
- Eso incluye, entre otras:
  - validaciones de alumnos/profesores
  - rutinas
  - clases
  - pagos
  - dashboard
  - mobile
- Por eso, aunque la UI de administracion trabaje con roles, internamente sigue existiendo esa clasificacion tecnica.

## Bootstrap De Base De Datos

- El esquema se crea y se actualiza desde un unico archivo:
  - `apps/fitexa/docker/mysql/bootstrap.sql`
- Ese script corre en cada arranque de MySQL mediante `--init-file`.
- El bootstrap es idempotente: intenta crear tablas, columnas, indices y datos base sin borrar informacion existente.
- El bootstrap ya no carga data mockeada.
- El bootstrap crea el usuario inicial si no existe:
  - email: `admin@fitexa.com`
  - password: `admin`
- Ese usuario queda con:
  - `user_type = ADMIN`
  - rol `Administrador`
  - `must_change_password = false`

## Persistencia Y Reset

- Como MySQL usa volumen Docker, los datos persisten entre reinicios.
- Si se quiere una base totalmente limpia, hay que borrar el volumen:

```bash
docker compose down -v
docker compose up -d --build
```

- Reiniciar contenedores sin borrar volumen no elimina datos previos.

## Tokens Y Sesiones

- Si existe un JWT viejo cuyo usuario ya no existe en base, el backend ahora lo trata como sesion invalida y no como error interno.
- El admin-panel limpia sesion cuando recibe `401`.
- Despues de resetear la base, puede ser necesario volver a iniciar sesion para descartar tokens viejos.

## Swagger

- Swagger/OpenAPI esta integrado en el backend.
- Se ajusto la version de `springdoc` para que sea compatible con la version actual de Spring Boot usada por Fitexa.

## Limites Actuales Del Modelo

- Hoy el nombre del rol tiene significado tecnico para tres casos especiales:
  - `Alumno`
  - `Administrador`
  - cualquier otro nombre
- Si en el futuro aparecen roles de negocio mas especificos como `Recepcion`, `Cobranza`, `Coordinador`, etc., actualmente todos esos caerian en el perfil tecnico `PROFESOR`.
- Esa decision fue tomada para unificar la UI sin romper reglas existentes.
- Si esos roles empiezan a tener logica propia de negocio, habra que separar formalmente:
  - acceso al panel
  - perfil operativo del sistema

## Recomendacion Para Evolucion Futura

- Mantener `roles` como control de acceso al panel.
- Separar en una etapa posterior el concepto de `perfil operativo` del concepto de `rol`.
- Implementar autorizacion backend por rol y endpoint, no solo por frontend.
- Evitar que la derivacion del perfil dependa unicamente del nombre del rol si el sistema sigue creciendo.
