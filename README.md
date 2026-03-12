# Messages Processing System

Sistema de mensajería asíncrona compuesto por dos microservicios independientes que se comunican a través de **RabbitMQ**. Construido con **Spring Boot 4**, **MySQL**, **MongoDB** y arquitectura limpia (hexagonal).

---

### Microservicios

| Servicio | Puerto | Base de datos | Rol |
|----------|--------|---------------|-----|
| `petitions` | `8080` | MySQL | Recibe peticiones, valida y publica en la cola |
| `processing` | `8081` | MongoDB | Consume la cola, procesa y expone los mensajes |

---

## Requisitos

- **Docker** y **Docker Compose** (recomendado)
- O bien: Java 21, Maven, MySQL 8, MongoDB 6, RabbitMQ 3.13

---

## Levantar con Docker Compose

```bash
# Clonar el repositorio
git clone <repo-url>
cd PruebaTecnica

# Levantar toda la infraestructura
docker compose up --build
```

Los servicios estarán disponibles en:
- **petitions:** `http://localhost:8080`
- **processing:** `http://localhost:8081`
- **RabbitMQ Management:** `http://localhost:15672` (guest/guest)

### Variables de entorno opcionales

Crea un archivo `.env` en la raíz del proyecto para sobreescribir los defaults:

```env
# API Security
API_KEY=mi-clave-secreta

# MySQL
MYSQL_ROOT_PASSWORD=password
MYSQL_DATABASE=petitions
MYSQL_PORT=3306

# MongoDB
MONGO_DATABASE=processing
MONGO_PORT=27017

# RabbitMQ
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672
RABBITMQ_EXCHANGE=petitions-exchange
RABBITMQ_QUEUE=petitions-queue
RABBITMQ_ROUTING_KEY=petition.created

# Puertos externos de los servicios
PETITIONS_PORT=8080
PROCESSING_PORT=8081
```

---

## API Reference

### `petitions` — `http://localhost:8080`

> ⚠️ Todos los endpoints requieren el header `X-API-KEY`.  
> Valor por defecto en desarrollo: `t3sT-k3y-123!`

#### `POST /api/v1/petitions` — Enviar una petición de mensaje

**Headers:**
```
X-API-KEY: t3sT-k3y-123!
Content-Type: application/json
```

**Body:**
```json
{
  "origin": "+573001234567",
  "destination": "+573007654321",
  "messageType": "TEXT",
  "content": "Hola, este es un mensaje de prueba"
}
```

Los tipos de mensaje válidos son: `TEXT`, `IMAGE`, `VIDEO`, `DOCUMENT`.  
Para los tipos multimedia, `content` debe ser una URL válida con esquema `http://` o `https://`.

**Respuesta exitosa (`201 Created`):**
```json
{
  "success": true,
  "status": 201,
  "message": "Petition processed successfully",
  "data": null
}
```

**Errores posibles:**

| Código | Causa |
|--------|-------|
| `401` | Header `X-API-KEY` ausente o inválido |
| `400` | Campos requeridos faltantes o `content` no es una URL válida (multimedia) |
| `404` | El número de origen no está registrado en el sistema |

---

### `processing` — `http://localhost:8081`

#### `GET /api/v1/messages/destination/{destination}` — Consultar mensajes por destino

**Parámetros:**

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `destination` | path | — | Número de teléfono destino |
| `page` | query | `0` | Página (0-indexed) |
| `size` | query | `20` | Elementos por página |
| `success` | query | `null` | Si es `true` retorna solo exitosos. Si es `false` retorna solo filtrados por rate-limit. Si no se envía retorna todos. |

**Ejemplo:**
```
GET http://localhost:8081/api/v1/messages/destination/+573007654321?success=true&page=0&size=10
```

**Respuesta exitosa (`200 OK`):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": "abc-123",
        "origin": "+573001234567",
        "destination": "+573007654321",
        "messageType": "TEXT",
        "content": "Hola",
        "createdDate": "2026-03-11T20:00:00Z",
        "processingTime": 42,
        "error": null
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## Estructura del proyecto

```
PruebaTecnica/
├── docker-compose.yml
├── petitions/                          # Microservicio de recepción
│   └── src/main/java/com/prueba/tecnica/
│       ├── application/
│       │   ├── dto/CreatePetitionRequestDto.java
│       │   └── usecase/PetitionUseCase.java
│       ├── domain/
│       │   ├── enums/MessageType.java
│       │   ├── exception/
│       │   ├── gateway/PetitionMessageGateway.java
│       │   ├── model/OriginLine.java
│       │   └── repository/OriginLineRepository.java
│       └── infrastructure/
│           ├── config/                 # RabbitMQ, ApiKey, WebConfig
│           ├── messaging/              # RabbitPetitionPublisher
│           ├── persistence/            # JPA + MySQL adapter
│           └── rest/                   # PetitionController, GlobalExceptionHandler
│
└── processing/                         # Microservicio de procesamiento
    └── src/main/java/com/prueba/tecnica/
        ├── application/
        │   ├── dto/
        │   └── usecase/
        │       ├── ProcessMessageUseCase.java
        │       └── GetMessagesByDestinationUseCase.java
        ├── domain/
        │   ├── exception/ProcessingException.java
        │   ├── model/ProcessedMessage.java
        │   └── repository/ProcessedMessageRepository.java
        └── infrastructure/
            ├── config/                 # RabbitMQ config
            ├── messaging/              # PetitionMessageListener
            ├── persistence/            # MongoDB adapter
            └── rest/                   # MessageController, GlobalExceptionHandler
```
