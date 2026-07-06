# Rigel Collection

> Modular business management software for small businesses — inventory, sales, and operational control.

Rigel Collection consolidates and reimagines several previous iterations built during my learning process,
this time with a clear architecture and a long-term roadmap.

---

## Modules

| Module          | Tech                        | Status  |
|-----------------|-----------------------------|---------|
| `rigel`         | Spring Boot + Kotlin        | Active  |
| `rigel_dream`   | React + TypeScript          | Active  |
| `rigel_android` | Jetpack Compose (+ KMP TBD) | Planned |

---

## Architecture

```
┌─────────────────────────────────────────┐
│              rigel_dream                │
│           (React + TypeScript)          │
│         Web frontend · Port 5173        │
└────────────────────┬────────────────────┘
                     │ REST API
┌────────────────────▼────────────────────┐
│                  rigel                  │
│          (Spring Boot + Kotlin)         │
│            Backend · Port 8080          │
│                                         │
│  ┌──────────┐  ┌──────┐  ┌──────────┐   │
│  │Inventory │  │Sales │  │  Image   │   │
│  │ Module   │  │Module│  │ Service  │   │
│  └──────────┘  └──────┘  └────┬─────┘   │
└───────────────────────────────┼─────────┘
          ┌──────────┐          │
          │PostgreSQL│    ┌─────▼──────┐
          │    DB    │    │ Cloudinary │
          └──────────┘    └────────────┘
```

---

## Screenshots

> Coming soon — the project is under active development.

---

## Tech Stack

**Backend (`rigel`)**

- Kotlin + Spring Boot
- PostgreSQL
- Cloudinary (image storage)
- Docker

**Frontend (`rigel_dream`)**

- React + TypeScript
- Vite

**Planned**

- Jetpack Compose (Android)
- Kotlin Multiplatform (KMP)

---

## Getting Started

### Prerequisites

- Java 21+
- Node.js 22+
- PostgreSQL instance (local or remote)
- A Cloudinary account (optional — falls back to local storage)

### Backend (`rigel`)

```bash
cd rigel
./gradlew build bootRun
```

The server starts on `http://localhost:8080`.

### Frontend (`rigel_dream`)

```bash
cd rigel_dream
npm install
npm run dev
```

The app starts on `http://localhost:5173`.

### Docker (backend only)

```bash
docker build -t rigel:1.0 .
docker run -p 8080:8080 rigel:1.0
```

### JAR (standalone)

```bash
cd rigel
./gradlew clean build
java -jar build/libs/Rigel-*.jar
```

---

## Configuration

Create an `application.yml` (or set environment variables) for the backend:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rigel
    username: your_user
    password: your_password

cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
  folder: rigel/products      # optional, this is the default

image:
  storage:
    local-path: ./uploads     # fallback if Cloudinary is not configured
    public-path: /uploads
```

> If Cloudinary credentials are absent or blank, the service falls back to local disk storage automatically.

---

## Roadmap

### v0.1 · Foundation

- [x] Product inventory (CRUD)
- [x] Image upload with Cloudinary + local fallback
- [x] Supplier management
- [x] Sales module (basic)

### v0.2 · Operational control In progress

- [ ] Low stock alerts
- [ ] Sales history and reports
- [ ] Role-based access (admin / employee)

### v0.3 · Mobile In progress

- [ ] Android app (`rigel_android`) with Jetpack Compose
- [ ] Evaluate Kotlin Multiplatform for shared business logic

---

## Contributing

Contributions are welcome. Fork the repo, make your changes, and open a pull request.
For significant changes, opening an issue first to discuss the approach is appreciated.

---

## License

This project is licensed under the [MIT License](LICENSE).