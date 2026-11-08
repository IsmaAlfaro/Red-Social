# UniMatch

Red social universitaria para conectar personas mediante musica, peliculas,
series, artistas y generos culturales.

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

## Enlaces

- Inicio: `http://localhost:8080/`
- Registro e inicio de sesion: `http://localhost:8080/access`
- Descubrir perfiles: `http://localhost:8080/discover`
- Mensajes: `http://localhost:8080/messages`
- Editar mi perfil: `http://localhost:8080/profile`

## Funcionalidades

- Registro e inicio de sesion con contrasenas cifradas mediante BCrypt.
- Perfil editable con nombre, carrera, bio, foto e intereses.
- Catalogo de peliculas, series, musica, artistas y generos.
- Creacion de gustos personales desde el perfil.
- Algoritmo de afinidad basado en intereses compartidos.
- Descubrir un perfil por vez, con acciones de corazon, pasar o arrastrar.
- Likes mutuos para formar matches.
- Mensajeria disponible solo entre matches mutuos.
- Foto o inicial de perfil en matches, mensajes y perfiles publicos.
- Diseno responsive para pantalla de escritorio y movil.

## Flujo De Uso

1. Crea una cuenta en `/access` y selecciona al menos un gusto.
2. Completa o modifica tu perfil desde la foto y nombre superiores en Descubrir.
3. Da corazon a perfiles compatibles o usa `Pasar` para ocultarlos.
4. Cuando dos usuarios se dan corazon, se genera un match.
5. Abre la burbuja de mensajes para conversar con tus matches.
6. Desde un chat seleccionado, usa `Ver perfil` para consultar el perfil de esa persona.

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
- `POST /api/interests`: crea un gusto personal autenticado.
- `PUT /api/auth/me`: actualiza el perfil autenticado.
- `POST /api/rejections/{profileId}`: oculta un perfil de futuras recomendaciones.
- `GET /api/likes/matches`: lista los matches mutuos de la cuenta.
- `GET /api/messages/{profileId}`: obtiene la conversacion con un match.
- `POST /api/messages/{profileId}`: envia un mensaje a un match.

## Persistencia

La aplicacion usa H2 en archivo local: `data/unimatch`. Las cuentas, perfiles,
likes, rechazos y mensajes permanecen despues de reiniciar la aplicacion. La
carpeta `data/` no se sube al repositorio.

La afinidad se calcula como los intereses compartidos entre el total de intereses
distintos de ambos perfiles. Por ejemplo, si dos perfiles comparten 2 intereses y
entre ambos tienen 4 intereses distintos, su afinidad es 50%.

Los endpoints de perfiles requieren una sesion iniciada. Las contrasenas se
guardan mediante hash BCrypt, nunca como texto plano.

El registro requiere elegir al menos un interes para calcular afinidades desde
el primer acceso.

## Prompts Del Desarrollo

Estas son las solicitudes que guiaron las funcionalidades principales:

- Crear una red social universitaria basada en musica, peliculas y compatibilidad.
- Implementar registro, inicio de sesion y perfiles universitarios.
- Mostrar perfiles compatibles con botones de interes y pasar.
- Agregar fotos de perfil, gustos propios y categorias culturales.
- Crear matches mutuos y mensajeria entre usuarios conectados.
- Separar la mensajeria en una pantalla propia y permitir ver el perfil desde un chat.
- Mejorar la interfaz responsive y aplicar paletas visuales definidas para el proyecto.
