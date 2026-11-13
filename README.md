# UniMatch

Red social universitaria para conectar personas mediante musica, peliculas,
series, artistas y generos culturales.

## Tecnologia

- Java 8
- Spring Boot 2.7
- Maven

## Estructura Del Codigo

```text
src/main/java/com/unimatch/
  audit/       Registro tecnico interno para el programador.
  config/       Datos iniciales de prueba.
  domain/       Modelos: perfiles, intereses, likes, rechazos y mensajes.
  repository/   Persistencia CSV sincronizada.
  security/     Inicio de sesion, sesion y proteccion de rutas.
  web/          API REST y navegacion de la interfaz.
src/main/resources/
  templates/    Pantallas: inicio, acceso, descubrir, perfil y mensajes.
  static/css/   Estilos visuales.
  static/js/    Comportamiento de cada pantalla.
```

Para defender el proyecto: `domain` representa los datos, `repository` los
consulta, `security` protege el acceso y `web` conecta backend e interfaz.

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

## Persistencia CSV

La aplicacion no usa base de datos, JPA ni H2. Cada repositorio usa archivos CSV
UTF-8 en `data/`; los archivos se crean al guardar el primer dato. Las escrituras
estan sincronizadas dentro de la aplicacion y se realizan mediante un archivo
temporal para evitar archivos parcialmente escritos. Las comillas, comas, barras
invertidas y saltos de linea se escapan al serializar los campos.

- `data/accounts.csv`: correo, hash BCrypt y perfil de cada cuenta.
- `data/profiles.csv`: datos de perfil e identificadores de intereses.
- `data/interests.csv`: catalogo y gustos personalizados.
- `data/likes.csv`: likes entre perfiles.
- `data/rejections.csv`: perfiles descartados.
- `data/match_notifications.csv`: avisos de match y su estado de lectura.
- `data/hidden_contacts.csv`: contactos ocultos.
- `data/messages.csv`: mensajes y fecha de envio.
- `data/audit.csv`: acciones tecnicas sin contrasenas.
- `uploads/`: fotos de perfil; `profiles.csv` guarda solo la ruta de cada foto.

`CsvStore` centraliza el formato y el bloqueo de lectura/escritura. Los
repositorios reconstruyen las relaciones por identificador, por lo que los
controladores y rutas existentes no dependen de una base de datos.

Las fotos nuevas se guardan como `uploads/profile-{id}.jpg` o `.png`. Si un
perfil antiguo contiene texto Base64 en `profiles.csv`, cambia su foto y guarda
el perfil una vez para convertirlo a la nueva ruta legible.

## Auditoria Tecnica

Las acciones de registro, login, logout y actualizacion de perfil se guardan en
`data/audit.csv`. Nunca registra contrasenas y no se muestra a los usuarios.

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
