# ProyectoRestaurantePrueba

Esqueleto de Spring Boot para una pagina web conectada a PostgreSQL.

Arranca una interfaz HTML servida por el backend (Thymeleaf) que lee y escribe
en PostgreSQL a traves de JPA/Hibernate, en arquitectura por capas.

## Stack

| Pieza | Version |
|---|---|
| Java | 17 (Temurin) |
| Spring Boot | 3.3.5 |
| Maven | 3.9.x |
| PostgreSQL | 16 (Docker en local) |
| Vistas | Thymeleaf |

Dependencias: `spring-boot-starter-web`, `-thymeleaf`, `-data-jpa`,
`-validation`, `-actuator`, driver `postgresql`. H2 solo en pruebas.

## Estructura

```
src/main/java/com/jcaa/restaurante/
├── RestauranteApplication.java     clase principal
├── config/                         configuracion transversal
├── controller/                     HomeController, ProductoController (HTTP)
├── dto/                            ProductoRequest, ProductoResponse
├── entity/                         Producto, Restaurante (JPA)
├── exception/                      GlobalExceptionHandler, ResourceNotFoundException
├── repository/                     ProductoRepository, RestauranteRepository
└── service/                        interfaz + impl (reglas de negocio)

src/main/resources/
├── application.properties          base comun
├── application-dev.properties      PostgreSQL local
├── application-prod.properties     variables de entorno, sin valores por defecto
├── schema.sql                      esquema documentado (no se ejecuta)
├── static/css/app.css
└── templates/
    ├── fragments/layout.html       head, cabecera y pie reutilizables
    ├── index.html                  portada
    ├── productos/lista.html        listado, busqueda y borrado
    ├── productos/formulario.html   alta y edicion
    └── error.html                  404 y errores de validacion

src/test/java/com/jcaa/restaurante/
├── service/impl/ProductoServiceImplTest.java   pruebas con Mockito
└── controller/PaginasControllerTest.java       pruebas de integracion con MockMvc + H2
```

La regla de dependencia va en una sola direccion: `controller` conoce `service`,
`service` conoce `repository` y `entity`. El `controller` nunca toca la entidad
de JPA: recibe y devuelve DTOs.

## Arranque en local

### 1. Base de datos

```powershell
docker compose up -d
```

Levanta PostgreSQL 16 en `127.0.0.1:5432` con base `restaurante` y usuario
`restaurante`. Esas credenciales son de desarrollo y estan en
`docker-compose.yml` a proposito; las de produccion no.

Comprobar que responde:

```powershell
docker compose exec postgres pg_isready -U restaurante -d restaurante
```

### 2. Aplicacion

```powershell
mvn spring-boot:run
```

Abrir <http://localhost:8080>.

El perfil `dev` esta activo por defecto. En desarrollo `ddl-auto=update` hace que
Hibernate cree las tablas `restaurante` y `producto` en el primer arranque, asi
que no hace falta correr `schema.sql`.

### 3. Pruebas

```powershell
mvn test
```

Los tests usan H2 en memoria, no necesitan el contenedor de PostgreSQL.

## Rutas

| Ruta | Metodo | Vista | Que hace |
|---|---|---|---|
| `/` | GET | `index` | Lista los restaurantes y el total |
| `/productos` | GET | `productos/lista` | Lista el menu, con filtro `?q=` |
| `/productos/nuevo` | GET | `productos/formulario` | Formulario de alta |
| `/productos` | POST | redirige | Crea un producto |
| `/productos/{id}/editar` | GET | `productos/formulario` | Formulario de edicion |
| `/productos/{id}/editar` | POST | redirige | Actualiza un producto |
| `/productos/{id}/eliminar` | POST | redirige | Elimina un producto |
| `/actuator/health` | GET | JSON | Estado de la app |

Un `GET /productos/99/editar` sobre un id inexistente devuelve **404**.

## Variables de entorno

El perfil `dev` tiene valores por defecto para que el proyecto clone y arranque
sin configurar nada. El perfil `prod` **no** los tiene:

| Variable | dev (por defecto) | prod (obligatoria) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `prod` |
| `SERVER_PORT` | `8080` | `8080` |
| `DB_HOST` | `localhost` | **obligatoria** |
| `DB_PORT` | `5432` | `5432` |
| `DB_NAME` | `restaurante` | `postgres` |
| `DB_USERNAME` | `restaurante` | **obligatoria** |
| `DB_PASSWORD` | `restaurante` | **obligatoria** |
| `DB_POOL_SIZE` | 5 | 10 |

Si falta una obligatoria, la aplicacion falla al arrancar. Es intencional:
arrancar con una credencial conocida seria peor que no arrancar.

`prod` connects con `?sslmode=require`, que es lo que exigen los proveedores
gestionados como Supabase, Neon o RDS.

Para produccion:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_HOST = "host-de-postgresql"
$env:DB_USERNAME = "usuario"
$env:DB_PASSWORD = "clave"
mvn spring-boot:run
```

## Esquema

Definido en `src/main/resources/schema.sql` y gestionado por Hibernate.

```sql
CREATE TABLE restaurante (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    nit       VARCHAR(32)  NOT NULL UNIQUE,
    direccion VARCHAR(200),
    telefono  VARCHAR(30)
);

CREATE TABLE producto (
    id          BIGSERIAL    PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    precio      NUMERIC(10, 2) NOT NULL CHECK (precio > 0),
    disponible  BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Dos diferencias con MySQL que hay que tener presentes: PostgreSQL no tiene
`NOW()` (usa `CURRENT_TIMESTAMP`) ni enums (se emula con `VARCHAR` mas `CHECK`).

En `prod` el `ddl-auto` es `validate`: la app no crea ni altera tablas, solo
verifica que el esquema exista. Los cambios de esquema se aplican con scripts
SQL antes del deploy.

## Notas

- El archivo no lleva datos de ejemplo. Sembrar credenciales en el repositorio
  las vuelve publicas. Si hacen falta datos de prueba, van en un archivo
  ignorado por git (`.gitignore` ya excluye `db/` y `*-seed.sql`).
- `schema.sql` no lo ejecuta la aplicacion (`spring.sql.init.mode=never`).
  Para correrlo a mano:

  ```powershell
  Get-Content src\main\resources\schema.sql -Raw |
      docker compose exec -T postgres psql -U restaurante -d restaurante
  ```

- `spring.jpa.open-in-view=false` mantiene la sesion de JPA dentro de la capa de
  servicio. Sin esto, Thymeleaf puede disparar consultas despues de que el
  servicio haya cerrado la transaccion.
- No hay autenticacion todavia. `POST /productos` esta abierto: antes de
  publicar hay que agregar Spring Security, porque un endpoint de escritura
  abierto cualquiera puede usar.