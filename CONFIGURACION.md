# Configuración de desarrollo

La aplicación requiere estas variables de entorno antes de iniciar:

- `DB_URL`: URL JDBC de PostgreSQL.
- `DB_USER`: usuario de PostgreSQL.
- `DB_PASSWORD`: contraseña de PostgreSQL.

Los ajustes opcionales del pool están documentados en `.env.example`.
El archivo `.env.example` es únicamente una plantilla: Java no lo carga
automáticamente. Configura las variables en Eclipse, Tomcat, Docker o Render.

## Base de datos

Antes de habilitar el login, ejecuta en la base de datos de desarrollo:

1. Conectado a `postgres`: `db/local/00_crear_base.sql`.
2. Conectado a `gps_control_dev`: `db/migrations/V000__esquema_base.sql`.
3. En la misma base: `db/migrations/V001__crear_usuarios.sql`.
4. `db/migrations/V002__crear_auditoria.sql`.
5. `db/migrations/V003__planes_y_notificaciones.sql`.
6. `db/migrations/V004__suscripciones_push.sql`.

En producción, crea primero un respaldo y ejecuta solamente V001 a V004, en
ese orden. V000 documenta el esquema base y no es necesaria cuando las tablas
`clientes` y `vehiculos_gps` ya existen.

## Primer administrador

Cuando la tabla `usuarios` esté vacía, configura temporalmente:

- `ADMIN_INITIAL_NAME`
- `ADMIN_INITIAL_USER`
- `ADMIN_INITIAL_PASSWORD` (entre 12 y 72 caracteres)

Al iniciar, la aplicación creará el primer usuario con rol `ADMIN` y almacenará
únicamente su hash bcrypt. Después del primer inicio exitoso, elimina
`ADMIN_INITIAL_PASSWORD` del entorno.

La sesión expira tras 30 minutos de inactividad. En producción,
`SESSION_COOKIE_SECURE` debe ser `true`. Para Tomcat local mediante HTTP puede
usarse temporalmente `false`.

## Eclipse

En la configuración del servidor Tomcat, agrega las variables al entorno del
proceso. No escribas credenciales dentro de `Conexion.java` ni las confirmes en
Git.

## Render

Configura los secretos en **Environment**. El Dockerfile no contiene ni copia
credenciales dentro de la imagen. Además de las variables de base de datos,
configura:

- `SESSION_COOKIE_SECURE=true`
- `ADMIN_INITIAL_NAME`, `ADMIN_INITIAL_USER` y temporalmente
  `ADMIN_INITIAL_PASSWORD` para el primer despliegue.
- `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY` y
  `VAPID_SUBJECT=https://gps-control.onrender.com`.

Elimina `ADMIN_INITIAL_PASSWORD` del entorno después de confirmar el primer
inicio de sesión. No publiques las claves VAPID ni contraseñas en Git.
