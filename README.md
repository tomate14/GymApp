# GymApp

Aplicacion de gimnasio con dos modulos independientes:

- `apps/admin-panel`: panel de administracion en Angular
- `apps/mobile-app`: app de usuarios en Ionic + Angular
- `apps/fitexa`: backend Java (Spring Boot + Hibernate + JPA + MySQL)

## Diseno

Ambos modulos usan una estetica oscura, moderna y llamativa con variables CSS para mantener consistencia visual.
El panel de administracion usa Bootstrap para responsive y esta organizado en:

- `app/components`: vistas por modulo (usuarios, actividades, clases, rutinas, ejercicios, maquinas)
- `app/services`: capa de datos CRUD en memoria
- `app/class`: types/modelos
- `app/popup`: vistas de popup reutilizables
- `app/utils`: utilidades compartidas

## Ejecutar el panel admin

```bash
cd apps/admin-panel
npm install
npm start
```

## Ejecutar la app mobile

```bash
cd apps/mobile-app
npm install
npm start
```

## Compilar app mobile para dispositivos

```bash
cd apps/mobile-app
npm run build
npm run cap:android
npm run cap:ios
npm run cap:sync
```

Luego se abre cada proyecto nativo desde Android Studio o Xcode.

## Backend Fitexa (Spring Boot + MySQL)

### Estructura principal

- `controler`: endpoints REST
- `services` + `services/impl`: contratos y logica de negocio
- `repository`: acceso a datos por entidad
- `entities`: modelo JPA
- `dtos`: contratos de request/response
- `utils`: utilidades y configuraciones
- `exeptions`: excepciones custom

### Scripts SQL

- `apps/fitexa/src/main/resources/schema.sql`: creacion de todas las tablas
- `apps/fitexa/src/main/resources/data.sql`: datos iniciales mock

### Levantar MySQL (opcional con Docker)

```bash
cd apps/fitexa
docker compose up -d
```

Nota: `schema.sql` y `data.sql` se ejecutan automaticamente desde `/docker-entrypoint-initdb.d` cuando MySQL inicializa una base nueva (volumen vacio).

## Docker (todas las aplicaciones)

Se agrego un `docker-compose.yml` en la raiz para levantar:

- MySQL (`localhost:3306`)
- Backend Fitexa (`localhost:8080`)
- Admin Panel Angular (`localhost:4200`)
- Mobile App Ionic/Angular (`localhost:8100`)

Comandos:

```bash
docker compose up -d --build
```

Para detener:

```bash
docker compose down
```

Para detener y borrar volumen de MySQL:

```bash
docker compose down -v
```

Si queres forzar que MySQL vuelva a ejecutar `schema.sql`/`data.sql`, elimina el volumen y volve a levantar:

```bash
docker compose down -v
docker compose up -d --build
```

### Ejecutar backend

```bash
cd apps/fitexa
mvn spring-boot:run
```

Si queres sobreescribir la conexion:

```bash
set MYSQL_URL=jdbc:mysql://localhost:3306/fitexa?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=root
mvn spring-boot:run
```

## Documentacion adicional

- Mercado Pago y ngrok para `mobile-app`: [docs/mercado-pago/README.md](docs/mercado-pago/README.md)
