# UniMatch

Red social universitaria para conectar personas mediante sus gustos de musica,
peliculas y series.

## Tecnologia

- Java 8
- Spring Boot 2.7
- Maven

## Abrir en IntelliJ IDEA

1. Abre IntelliJ IDEA Community.
2. Selecciona **Open** y elige la carpeta `Red-Social`.
3. IntelliJ detectara el archivo `pom.xml`; acepta cargar el proyecto Maven.
4. Configura un JDK 8 en **File > Project Structure > Project SDK**.
5. Ejecuta `UniMatchApplication`.

La aplicacion estara disponible en `http://localhost:8080`.
El estado del backend se consulta en `http://localhost:8080/api/health`.

Abre `http://localhost:8080/access` para crear una cuenta o iniciar sesion.

## Endpoints iniciales

- `GET /api/health`: confirma que el backend esta activo.
- `GET /api/profiles`: devuelve los perfiles de ejemplo y sus intereses.
- `GET /api/profiles/{id}`: devuelve un perfil por identificador.
- `GET /api/profiles/{id}/matches`: devuelve otros perfiles ordenados por afinidad.
- `GET /api/profiles/matches`: devuelve afinidades para la cuenta autenticada.
- `POST /api/likes/{profileId}`: registra un like y comunica si es mutuo.
- `POST /api/auth/register`: crea una cuenta y su perfil.
- `POST /api/auth/login`: inicia sesion y conserva una sesion segura en el navegador.
- `POST /api/auth/logout`: cierra la sesion actual.
- `GET /api/interests`: lista los gustos disponibles para el registro.

La aplicacion usa una base de datos H2 en memoria para desarrollo. Sus datos se
reinician al detener la aplicacion.

La afinidad se calcula como los intereses compartidos entre el total de intereses
distintos de ambos perfiles. Por ejemplo, si dos perfiles comparten 2 intereses y
entre ambos tienen 4 intereses distintos, su afinidad es 50%.

Los endpoints de perfiles requieren una sesion iniciada. Las contrasenas se
guardan mediante hash BCrypt, nunca como texto plano.

El registro requiere elegir al menos un interes para calcular afinidades desde
el primer acceso.
