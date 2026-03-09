# Mercado Pago para `mobile-app`

La integracion mobile de pagos online usa:

- Checkout Pro de Mercado Pago
- backend `apps/fitexa` para crear la preferencia y consultar el estado del pago
- tabla `mercado_pago_configs` para persistir credenciales y URLs

La configuracion activa se toma asi:

1. primero desde la tabla `mercado_pago_configs`
2. si falta algun dato, hace fallback a variables de entorno

## Configurar credenciales en la base

La tabla usada es:

- `mercado_pago_configs`

Campos relevantes:

- `access_token`: token privado que usa el backend
- `public_key`: reservado para futuras integraciones frontend/nativas
- `public_base_url`: URL publica HTTPS del backend para webhook
- `mobile_base_url`: URL publica HTTPS de la mobile
- `sandbox_mode`: `1` para test, `0` para prod
- `active`: fila activa

Ver la configuracion actual:

```sql
SELECT
  id,
  name,
  access_token,
  public_key,
  public_base_url,
  mobile_base_url,
  sandbox_mode,
  active,
  created_at,
  updated_at
FROM mercado_pago_configs;
```

Si no existe la fila `default`, crearla:

```sql
INSERT INTO mercado_pago_configs (
  name,
  access_token,
  public_key,
  public_base_url,
  mobile_base_url,
  webhook_secret,
  sandbox_mode,
  active,
  created_at,
  updated_at
) VALUES (
  'default',
  NULL,
  NULL,
  'http://localhost:8080',
  'http://localhost:8100',
  NULL,
  1,
  1,
  NOW(),
  NOW()
);
```

## Paso a paso para obtener credenciales de test

1. Entrar a Mercado Pago Developers con la cuenta que va a actuar como `seller`.
2. Ir a `Your integrations`.
3. Si todavia no existe una app:
   - crear una nueva aplicacion
   - elegir una integracion propia
   - seleccionar `Checkout Pro`
4. Entrar a la aplicacion creada.
5. Abrir `Credentials > Test` o `Test credentials`.
6. Copiar el `Access Token` de test.
7. Si aparece `Public Key`, copiarla solo si queres guardarla para uso futuro. El flujo actual no la necesita.
8. Cargarla en la DB:

```sql
UPDATE mercado_pago_configs
SET
  access_token = 'TEST-xxxxxxxxxxxxxxxx',
  public_key = NULL,
  public_base_url = 'https://TU_BACKEND_PUBLICO',
  mobile_base_url = 'https://TU_MOBILE_PUBLICA',
  sandbox_mode = 1,
  active = 1,
  updated_at = NOW()
WHERE name = 'default';
```

9. Verificar:

```sql
SELECT name, access_token, public_base_url, mobile_base_url, sandbox_mode, active
FROM mercado_pago_configs
WHERE name = 'default';
```

10. Reiniciar backend:

```bash
docker compose up -d --build fitexa
```

## Paso a paso para obtener credenciales de produccion

1. Ir a `Your integrations`.
2. Abrir la aplicacion correspondiente.
3. Ir a `Credentials > Production` o `Production credentials`.
4. Completar los datos solicitados por Mercado Pago:
   - industria
   - sitio web
   - aceptacion de terminos
   - reCAPTCHA
5. Copiar el `Access Token` productivo.
6. Si queres, copiar tambien la `Public Key`.
7. Actualizar la DB:

```sql
UPDATE mercado_pago_configs
SET
  access_token = 'APP_USR-xxxxxxxxxxxxxxxx',
  public_key = 'APP_USR-xxxxxxxxxxxxxxxx',
  public_base_url = 'https://api.tu-dominio.com',
  mobile_base_url = 'https://app.tu-dominio.com',
  sandbox_mode = 0,
  active = 1,
  updated_at = NOW()
WHERE name = 'default';
```

8. Reiniciar backend:

```bash
docker compose up -d --build fitexa
```

## Como editar la configuracion en MySQL

Si usas el contenedor `fitexa-mysql`:

```powershell
docker exec -it fitexa-mysql mysql -uroot -proot -D fitexa
```

Despues:

```sql
SELECT * FROM mercado_pago_configs;
```

Y luego:

```sql
UPDATE mercado_pago_configs
SET
  access_token = 'TEST-xxxxxxxxxxxxxxxx',
  public_base_url = 'https://TU_BACKEND_PUBLICO',
  mobile_base_url = 'https://TU_MOBILE_PUBLICA',
  updated_at = NOW()
WHERE name = 'default';
```

Salir:

```sql
exit
```

## Configurar ngrok para backend y mobile

Para probar Mercado Pago desde un entorno local, este proyecto necesita dos tuneles HTTPS:

- uno para `fitexa` en `localhost:8080`
- otro para `mobile-app` en `localhost:8100`

### Paso 1. Configurar `ngrok.yml`

En Windows, si instalaste ngrok desde Microsoft Store, el archivo suele estar en:

- `C:\Users\TU_USUARIO\AppData\Local\Packages\ngrok.ngrok_1g87z0zv29zzc\LocalCache\Local\ngrok\ngrok.yml`

Ejemplo:

```yaml
version: 3
agent:
  authtoken: TU_NGROK_AUTHTOKEN

tunnels:
  fitexa-api:
    proto: http
    addr: 8080
  fitexa-mobile:
    proto: http
    addr: 8100
```

### Paso 2. Levantar los dos tuneles

En PowerShell:

```powershell
ngrok start fitexa-api fitexa-mobile
```

Si queres levantar cada uno por separado:

```powershell
ngrok http 8080
```

```powershell
ngrok http 8100
```

### Paso 3. Copiar las URLs HTTPS

Ngrok te va a devolver dos URLs distintas, por ejemplo:

- backend: `https://api-fitexa-demo.ngrok-free.app`
- mobile: `https://mobile-fitexa-demo.ngrok-free.app`

Esas URLs no deben ser la misma.

### Paso 4. Guardar las URLs de ngrok en DB

```sql
UPDATE mercado_pago_configs
SET
  public_base_url = 'https://API_NGROK_URL',
  mobile_base_url = 'https://MOBILE_NGROK_URL',
  updated_at = NOW()
WHERE name = 'default';
```

Ejemplo:

```sql
UPDATE mercado_pago_configs
SET
  public_base_url = 'https://api-fitexa-demo.ngrok-free.app',
  mobile_base_url = 'https://mobile-fitexa-demo.ngrok-free.app',
  updated_at = NOW()
WHERE name = 'default';
```

### Paso 5. Abrir la app desde la URL publica del tunel mobile

Para que `back_urls.success` funcione correctamente:

- abrir la app desde `https://MOBILE_NGROK_URL`
- no desde `http://localhost:8100`

### Comando PowerShell recomendado

```powershell
docker compose -f docker-compose.yml up -d --build
ngrok start fitexa-api fitexa-mobile
```

Luego:

1. copiar las 2 URLs HTTPS
2. actualizar `mercado_pago_configs`
3. abrir la mobile desde la URL HTTPS del tunel

### Nota sobre errores de ngrok

Si aparece `ERR_NGROK_334`, significa que intentaste reutilizar el mismo endpoint/dominio para dos servicios distintos.

En ese caso:

- cerrar el tunel actual con `Ctrl + C`
- o levantar ambos con `ngrok start fitexa-api fitexa-mobile`

## Probar el flujo actual en este proyecto

1. Configurar `access_token`, `public_base_url` y `mobile_base_url` en `mercado_pago_configs`.
2. Levantar backend y mobile.
3. Abrir la app desde la URL HTTPS del tunel mobile.
4. Loguearte con un alumno.
5. Ir a `Historial de pagos`.
6. Tocar `Pagar cuota online`.
7. Completar el checkout en Mercado Pago.
8. Al volver a la app, el flujo hace `sync` y, si el pago fue aprobado:
   - actualiza la cuota del alumno
   - registra el movimiento en `user_payment_movements`

## Referencias oficiales

- Credenciales: https://www.mercadopago.com.ar/developers/en/docs/credentials
- Crear aplicacion Checkout Pro: https://www.mercadopago.com.ar/developers/en/docs/checkout-pro/create-application
- Credenciales de Checkout Pro: https://www.mercadopago.com.ar/developers/en/docs/checkout-pro/additional-content/your-integrations/credentials
- Test accounts: https://www.mercadopago.com.ar/developers/en/docs/your-integrations/test/accounts
- Integration test Checkout Pro: https://www.mercadopago.com.ar/developers/en/docs/checkout-pro/integration-test
