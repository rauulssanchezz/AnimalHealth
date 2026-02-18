# 👤 Documentación de Endpoints de Usuario (Auth)

Este módulo gestiona el registro, la autenticación y el perfil de los usuarios (Veterinarios y Clientes) utilizando **Django REST Framework Token Authentication**.

## Base URL
`http://localhost:8000/api/` (o tu dominio en producción)

---

## 1. Registro de Usuario
Permite crear una nueva cuenta en el sistema.

* **URL:** `api/auth/register/`
* **Método:** `POST`
* **Permiso:** `AllowAny` (Público)
* **Cuerpo (JSON):**

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `username` | String | Nombre de usuario único. |
| `email` | String | Correo electrónico (Requerido). |
| `password` | String | Contraseña (Solo escritura). |
| `is_vet` | Boolean | `true` si es veterinario, `false` si es cliente. |
| `clinic_admin` | Boolean | Define si gestiona una clínica. |

**Respuesta Exitosa (201 Created):**
```json
{
    "message": "Usuario creado con éxito"
}s

## 2. Inicio de Sesión (Login)
Obtiene un token de acceso para realizar peticiones protegidas.

* **URL:** `/api/auth/login/`
* **Método:** `POST`
* **Permiso:** `AllowAny`
* **Cuerpo de la petición (JSON):**

```json
{
    "username": "tu_usuario",
    "password": "tu_password"
}

## 3. Mi Perfil (Detalle y Edición)
Punto de acceso para que el usuario autenticado consulte o gestione sus propios datos.

* **URL:** `/api/auth/me/`
* **Método:** `GET` | `PUT` | `PATCH`
* **Permiso:** `IsAuthenticated` (Requiere Token)
* **Header:** `Authorization: Token <tu_token>`

#### Opción A: Pestaña "Auth" (Recomendado)
1. Ve a la pestaña **Auth**.
2. En **Type**, selecciona **API Key**.
3. En **Key**, escribe: `Authorization`.
4. En **Value**, escribe: `Token <tu_token_aquí>` (ejemplo: `Token 9944b09199c62bcf9418ad846dd0e4bbdfc6ee4b`).
5. En **Add to**, selecciona **Header**.

#### Opción B: Pestaña "Headers" (Manual)
Añade una nueva fila con los siguientes valores:

| Key | Value |
| :--- | :--- |
| `Authorization` | `Token 9944b09199c62bcf9418ad846dd0e4bbdfc6ee4b` |

---

### importante poner Token antes del token ###

### A. Consultar Perfil (GET)
Retorna la información del usuario que realiza la petición.

**Respuesta Exitosa (200 OK):**
```json
{
    "username": "rauul",
    "email": "raul@ejemplo.com",
    "is_vet": true,
    "clinic": "68907bab-adb9-4ee3-b64a-144fde010fca",
    "clinic_admin": true
}

## 4. Cierre de Sesión (Logout)
Invalida y elimina el token de acceso actual en el servidor, obligando a un nuevo inicio de sesión para obtener uno nuevo.

* **URL:** `/api/auth/logout/`
* **Método:** `POST`
* **Permiso:** `IsAuthenticated` (Requiere Token)
* **Header:** `Authorization: Token <tu_token>`

**Respuesta Exitosa (200 OK):**
```json
{
    "message": "Sesión cerrada correctamente"
}

### 5. Borrar Usuario (Eliminar Cuenta)
Permite a un usuario autenticado eliminar permanentemente su cuenta y todos los datos asociados (perfil, clínicas si es administrador, etc.).

* **URL:** `/api/me/`
* **Método:** `DELETE`
* **Permiso:** `IsAuthenticated` (Solo el dueño de la cuenta)
* **Header:** `Authorization: Token <tu_token>`

#### Respuestas:

| Código | Descripción |
| :--- | :--- |
| **204 No Content** | El usuario y su cuenta han sido eliminados correctamente. |
| **401 Unauthorized** | No se ha proporcionado un token válido. |
| **403 Forbidden** | No tienes permisos para realizar esta acción sobre este perfil. |

> **Advertencia:** Esta acción es irreversible. Dependiendo de la configuración de `on_delete=models.CASCADE` en tus modelos, borrar el usuario también podría eliminar las clínicas asociadas o las reseñas que haya escrito.

# 🏥 Documentación de API: Clínicas y Reseñas

Este documento detalla los endpoints para la gestión de centros veterinarios y el sistema de feedback de usuarios.

---

## 🏗️ Módulo de Clínicas (Clinics)

Gestión de información, geolocalización automática vía Nominatim y galería de imágenes.

### 1. Listado y Búsqueda
* **URL:** `/api/clinics/` | `/api/clinics/nearby/?latitude=example&longitude=example&distance=example`
* **Método:** `GET`
* **Permiso:** `AllowAny`
* **Filtros:** * `?search=valor`: Busca coincidencias en los campos `name` y `address`.

**Respuesta Exitosa (200 OK):**
```json
[
  {
    "email": "vet@example.com",
    "name": "Veterinaria Central",
    "address": "Calle Mayor 1, Madrid",
    "latitude": "40.4167",
    "longitude": "-3.7038",
    "admin":"adminuuid"
    "rates": [...]
  }
]

### 2. Registrar una Clínica
Crea una nueva clínica asociada al usuario autenticado (Veterinario). Permite la subida de múltiples imágenes mediante archivos físicos.

* **URL:** `/api/clinics/`
* **Método:** `POST`
* **Autenticación:** Requerida (`Token <tu_token>`)
* **Formato de envío:** `multipart/form-data`

#### 📥 Parámetros (Body - form-data):

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `name` | `string` | Nombre de la clínica. |
| `address` | `string` | Dirección física de la clínica. |
| `uploaded_images` | `file` | Archivo de imagen (puedes enviar varios campos con la misma clave para subir varias fotos). |

> **Nota para Postman:** En la pestaña **Body**, selecciona **form-data**. Al escribir `uploaded_images` en la columna **Key**, cambia el tipo de "Text" a "File" en el menú desplegable que aparece a la derecha de la celda.

#### 📤 Respuesta Exitosa (201 Created):

```json
{
    "id": "uuid-clinica",
    "name": "Clínica Veterinaria Sol",
    "address": "Calle Falsa 123",
    "email": "contacto@clinicasol.com",
    "admin": "tu-uuid-usuario",
    "images": [
        {
            "id": 1,
            "image": "http://localhost:8000/media/clinics/foto1.jpg"
        },
        {
            "id": 2,
            "image": "http://localhost:8000/media/clinics/foto2.png"
        }
    ]
}

### 3. Gestión de Clínica (Detalle, Actualización y Borrado)
Permite consultar la información detallada de una clínica específica o modificar sus datos si se tienen los permisos de administrador.

* **URL:** `/api/clinics/<id_de_la_clinica>/`
* **Método:** `GET` | `PUT` | `PATCH` | `DELETE`
* **Permisos:** * `GET`: `AllowAny` (Público).
    * `PUT / PATCH / DELETE`: `IsClinicAdmin` (Solo el administrador de esa clínica específica).

#### A. Consultar Detalle (GET)
Retorna toda la información de la clínica, incluyendo sus coordenadas y las reseñas recibidas.

**Ejemplo de Respuesta (200 OK):**
```json
{
    "email": "contacto@clinica.com",
    "name": "Clínica Veterinaria Central",
    "address": "Calle Mayor 1, Madrid",
    "latitude": 40.4167,
    "longitude": -3.7038,
    "admin":"adminuuid"
}

### 4. Borrar Clínica
Permite al administrador de una clínica eliminar permanentemente el centro, sus datos y sus imágenes asociadas.

* **URL:** `/api/clinics/<id_de_la_clinica>/`
* **Método:** `DELETE`
* **Permiso:** `IsClinicAdmin` (Solo el usuario veterinario que gestiona esta clínica).
* **Header:** `Authorization: Token <tu_token>`

#### Respuestas:

| Código | Descripción |
| :--- | :--- |
| **204 No Content** | La clínica y sus imágenes han sido eliminadas con éxito. |
| **401 Unauthorized** | El token no es válido o está ausente. |
| **403 Forbidden** | No eres el administrador de esta clínica. |
| **404 Not Found** | El UUID de la clínica no existe. |

> **Efectos Secundarios:** > - Según tu modelo, al borrar la clínica se eliminarán también todas las imágenes asociadas en `ClinicImage` y las reseñas en `Rate` debido al comportamiento `on_delete=models.CASCADE`.
> - El usuario administrador dejará de estar vinculado a esta clínica.

### 5. Gestión de Imágenes de la Clínica
Las imágenes se gestionan principalmente a través del registro o actualización de la clínica, pero este es el detalle de cómo se procesan.

* **URL:** `/api/clinics/<id_clinica>/` (vía `uploaded_images`)
* **Método:** `POST` | `PATCH`
* **Permiso:** `IsClinicAdmin`
* **Tipo de contenido:** `multipart/form-data`

#### Parámetros para subir imágenes:
| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `uploaded_images` | file[] | No | Uno o varios archivos de imagen (JPG, PNG). |

#### Funcionamiento Interno:
1. **Almacenamiento:** Las imágenes se suben automáticamente a la carpeta `/media/clinics/`.
2. **Vinculación:** Cada imagen se registra en el modelo `ClinicImage` vinculada al ID de la clínica.
3. **Metadatos:** El sistema guarda automáticamente la fecha de creación (`created_at`).

#### Ejemplo de visualización en el detalle de Clínica (GET):
Cuando consultas una clínica, las imágenes se listan dentro de la estructura (si se incluye en el serializer):

```json
{
    "name": "Clínica Veterinaria",
    "images": [
        {
            "id": 1,
            "image": "/media/clinics/foto1.jpg",
            "created_at": "2024-05-20T12:00:00Z"
        }
    ]
}

## ⭐ Módulo de Reseñas (Rates)

Sistema de calificación para que los usuarios valoren las clínicas. Las clínicas pueden ver sus notas, pero **no pueden editarlas**.

### 1. Listado y Filtros
* **URL:** `/api/rates/`
* **Método:** `GET`
* **Permiso:** `AllowAny` (Público)
* **Filtros (Query Params):**
    * `?clinic=<uuid>`: Ver todas las reseñas de una clínica específica.
    * `?user=<uuid>`: Ver todas las reseñas creadas por un usuario.

**Respuesta Exitosa (200 OK):**
```json
[
  {
    "id": "uuid-reseña",
    "clinic": "uuid-clinica",
    "rate": 5,
    "comment": "Excelente atención profesional.",
    "user_name": "raul_petlover",
    "user_username": "raul_petlover"
  }
]

### 2. Crear Reseña (Rate)
Permite a un usuario autenticado calificar una clínica con una puntuación y un comentario opcional.

* **URL:** `/api/rates/`
* **Método:** `POST`
* **Permiso:** `IsAuthenticated` (Requiere Token)
* **Header:** `Authorization: Token <tu_token>`

#### Cuerpo de la petición (JSON):

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `clinic` | UUID | Sí | ID de la clínica a la que se desea calificar. |
| `rate` | Integer | Sí | Puntuación numérica entre **0 y 5**. |
| `comment` | String | No | Comentario detallado sobre la experiencia (opcional). |

#### Ejemplo de Cuerpo:
```json
{
    "clinic": "68907bab-adb9-4ee3-b64a-144fde010fca",
    "rate": 5,
    "comment": "Un trato excelente con mi mascota, muy recomendados."
}

### 3. Gestionar Reseña Propia (Detalle, Edición y Borrado)
Permite al autor de una reseña verla en detalle, modificar su puntuación/comentario o eliminarla por completo.

* **URL:** `/api/rates/<id_de_la_reseña>/`
* **Método:** `GET` | `PUT` | `PATCH` | `DELETE`
* **Permisos:** * `GET`: `AllowAny` (Público).
    * `PUT / PATCH / DELETE`: `IsOwnerOrReadOnly` (Solo el autor que creó la reseña).

#### A. Consultar Detalle (GET)
Retorna la información completa de una calificación específica.

**Ejemplo de Respuesta (200 OK):**
```json
{
    "id": "77218ace-6672-466d-9788-0f04771c379a",
    "clinic": "68907bab-adb9-4ee3-b64a-144fde010fca",
    "rate": 4,
    "comment": "Buen servicio, aunque tardaron un poco.",
    "user_name": "rauul",
    "user_username": "rauul"
}

### 4. Borrar Reseña (Rate)
Permite al autor de una calificación eliminarla permanentemente del sistema.

* **URL:** `/api/rates/<id_de_la_reseña>/`
* **Método:** `DELETE`
* **Permiso:** `IsOwnerOrReadOnly` (Solo el autor de la reseña puede realizar esta acción).
* **Header:** `Authorization: Token <tu_token>`

#### Respuestas:

| Código | Descripción |
| :--- | :--- |
| **204 No Content** | La reseña ha sido eliminada con éxito. |
| **401 Unauthorized** | El token no es válido o no se ha proporcionado. |
| **403 Forbidden** | Intentas borrar una reseña que no te pertenece (ej. si eres el administrador de la clínica). |
| **404 Not Found** | El ID de la reseña no existe. |

> **Nota:** Al eliminar una reseña, esta desaparece automáticamente del cálculo de valoraciones y del listado de la clínica asociada.