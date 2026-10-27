# Billing SaaS de tenants

Este modulo cobra la suscripcion de cada tenant/gimnasio a la cuenta de Mercado Pago de Fitexa.

## Tablas principales

- `billing_plans`: planes SaaS de Fitexa.
- `client_subscriptions`: estado de suscripcion por `client_id`.
- `client_subscription_payments`: pagos recurrentes aprobados y periodo de acceso que habilitan.
- `mercado_pago_accounts`: credenciales y URLs de la cuenta cobradora.
- `billing_events`: auditoria e idempotencia de webhooks.

## Configurar Mercado Pago

La fila activa se toma de `mercado_pago_accounts`.

```sql
UPDATE mercado_pago_accounts
SET
  encrypted_access_token = 'TEST-xxxxxxxxxxxxxxxx',
  encrypted_webhook_secret = 'tu-secreto-compartido',
  public_base_url = 'https://api.tu-dominio.com',
  app_base_url = 'https://admin.tu-dominio.com',
  sandbox_mode = 1,
  active = 1,
  updated_at = NOW()
WHERE name = 'default';
```

Los valores con prefijo `enc:` se descifran con `FITEXA_SECRETS_ENCRYPTION_KEY`; los valores sin prefijo se aceptan para configuracion manual/local.

Las credenciales de login de los usuarios test de Mercado Pago no se guardan en Fitexa. Para operar por API se usa el `access_token` del vendedor. El comprador se configura como `payer_email` en `client_subscriptions` o se toma del email del admin logueado.

Ejemplo para pruebas:

```sql
UPDATE mercado_pago_accounts
SET
  encrypted_access_token = 'TEST-ACCESS-TOKEN-DEL-VENDEDOR',
  sandbox_mode = 1,
  active = 1,
  updated_at = NOW()
WHERE name = 'default';

UPDATE client_subscriptions cs
JOIN clients c ON c.id = cs.client_id
SET
  cs.payer_email = 'EMAIL_TEST_DEL_COMPRADOR',
  cs.updated_at = NOW()
WHERE c.slug = 'fitexa-central';
```

Si solo tenes el usuario tipo `TESTUSER...`, entra a Mercado Pago Developers con el vendedor de prueba y copia el `Access Token`; para el comprador, usa el email real del usuario test si Mercado Pago lo muestra. La password solo sirve para iniciar sesion en Mercado Pago durante el checkout.

## Endpoints

- `GET /api/v1/billing/plans`
- `GET /api/v1/billing/subscription`
- `POST /api/v1/billing/subscription/checkout`
- `POST /api/v1/billing/webhooks/mercado-pago`

Cuando un tenant no esta activo, el backend devuelve `402` con `TENANT_SUBSCRIPTION_REQUIRED` para todos los endpoints protegidos excepto login, cambio de password, billing, webhooks y recursos publicos.

El acceso se considera activo si existe un `current_period_end` vigente. Si el tenant cancela la suscripcion despues de pagar, puede usar Fitexa hasta el final del periodo ya pagado.
