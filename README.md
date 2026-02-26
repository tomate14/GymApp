# GymApp

Aplicacion de gimnasio con dos modulos independientes:

- `apps/admin-panel`: panel de administracion en Angular
- `apps/mobile-app`: app de usuarios en Ionic + Angular

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
