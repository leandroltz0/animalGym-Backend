# MÍTICO ANIMAL GYM — Backend Context & Architecture

> **Last Updated:** Junio 2026
> **Purpose:** Single source of truth for AI agents and developers implementing the backend.

---

## 🏗️ Overview
**Mítico Animal Gym** is a premium gym in Paso del Rey, Buenos Aires. We are building a scalable web platform.
**Current Scope:** Backend API for managing Products (Store) and Admin Authentication.
**Future Scope:** Memberships, Payments, Classes, Users (Not in this phase).

---

## 🛠️ Tech Stack
-   **Language:** Java 17
-   **Framework:** Spring Boot 3.3.x
-   **Database:** PostgreSQL (Hosted on Railway)
-   **Security:** Spring Security + JWT (Stateless, 24h expiration)
-   **Image Storage:** Cloudinary (API-based upload) — **NO local file storage**.
-   **Build Tool:** Maven
-   **Testing:** JUnit 5 + Mockito

---

## 🚀 Hosting Strategy
-   **Backend:** Render / Railway (Stateless, ephemeral filesystem).
-   **Database:** Railway PostgreSQL (Persistent).
-   **Images:** Cloudinary (External storage, URL-based).
-   **Frontend:** Vercel / Netlify (React SPA).

---

## 📊 Database Schema (Current Scope)

### 1. Table: `products`
Stores store items. No complex relations yet.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Primary Key |
| `name` | `VARCHAR(255)` | `NOT NULL` | Product name |
| `price` | `DECIMAL(10,2)` | `NOT NULL` | Current price |
| `stock` | `INT` | `DEFAULT 0` | Available quantity |
| `description` | `TEXT` | | Detailed description |
| `image_url` | `VARCHAR(500)` | `NOT NULL` | Full Cloudinary URL |
| `active` | `BOOLEAN` | `DEFAULT TRUE` | Soft delete toggle |

### 2. Table: `users` (Auth)
Stores admin credentials.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Primary Key |
| `email` | `VARCHAR(255)` | `UNIQUE`, `NOT NULL` | Login email |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | BCrypt hash |
| `role` | `VARCHAR(50)` | `DEFAULT 'ADMIN'` | Role enum (ADMIN, STAFF) |

---

## 🔌 API Specification

### 1. Public Endpoints (No Auth Required)
Used by the public gym website to display products.

-   **`GET /api/products`**
    -   **Response:** `List<ProductResponseDTO>` (Only active products).
    -   **Format:** JSON Array of objects.

### 2. Authentication
-   **`POST /api/auth/login`**
    -   **Request:** `{ "email": "string", "password": "string" }`
    -   **Response:** `{ "token": "eyJ...", "role": "ADMIN", "email": "..." }`
    -   **Logic:** Verify email, check BCrypt password, generate JWT.

### 3. Admin Endpoints (JWT Protected)
Used by the `/admin` dashboard. Requires `Authorization: Bearer <token>` header.

-   **`POST /api/admin/products`**
    -   **Format:** `multipart/form-data`
    -   **Parts:** `file` (Image), `data` (JSON string of ProductRequestDTO).
    -   **Logic:** Upload image to Cloudinary -> Get URL -> Save Product to DB with URL.
    -   **Response:** `ProductResponseDTO` (Created).

-   **`PATCH /api/admin/products/{id}`**
    -   **Format:** `application/json` or `multipart/form-data` (optional image update).
    -   **Logic:** Update fields. If new image provided, upload and replace URL.
    -   **Response:** `ProductResponseDTO` (Updated).

-   **`DELETE /api/admin/products/{id}`**
    -   **Logic:** Soft delete (set `active = false`).
    -   **Response:** `204 No Content`.

---

## 📐 Implementation Guidelines

### Architecture Pattern
Use a standard layered architecture:
1.  **Controller:** Handles HTTP requests, validation, returns DTOs.
2.  **Service:** Business logic, Cloudinary integration, transaction management.
3.  **Repository:** Spring Data JPA interfaces.
4.  **Entity:** JPA mapped classes (`@Entity`, `@Table`).
5.  **DTO:** Separate objects for Request (`ProductRequestDTO`) and Response (`ProductResponseDTO`). Never expose Entities directly.

### Security Best Practices
-   **CORS:** Configure `WebSecurityConfig` to allow requests from Frontend origins (`http://localhost:5173`, production URL).
-   **Validation:** Use `@Valid`, `@NotBlank`, `@Positive` on DTOs.
-   **Error Handling:** Use `@RestControllerAdvice` to return clean JSON errors, not stack traces.

### Image Handling (Cloudinary)
-   **Do NOT save files locally.** The backend is stateless.
-   Use `cloudinary` Maven dependency.
-   Service receives `MultipartFile`, uploads to Cloudinary via API, and receives a secure URL.
-   Save **only the URL string** in the PostgreSQL database.

### Testing
-   **Unit Tests:** Use **JUnit 5** and **Mockito**. Test Service logic and Security rules.
-   **Integration Tests:** Use `@SpringBootTest` and `MockMvc` for Controller endpoints.
-   **Coverage:** Focus on Auth flow (Login success/fail) and Product CRUD (Create with image).

---

## 🎨 Frontend Integration Notes
-   **Framework:** React + Vite + TypeScript + TailwindCSS.
-   **State Management:** Zustand (Auth), TanStack Query (Data Fetching).
-   **Auth:** JWT stored in `localStorage`, sent in headers.
-   **Product Cards:** Frontend iterates over the JSON array from `GET /api/products`.
-   **Admin Dashboard:** Single file HTML/React component hosted at `/admin`.
-   **Image Upload:** Frontend uses `FormData` to send `multipart/form-data` requests.

---

## ⏳ Future Scope (Not yet implemented)
-   Memberships (Plans, Member profiles, Due dates).
-   Payments (MercadoPago integration, Invoicing).
-   Classes & Schedules (Reservations, Trainers).
-   User Registration (Customer accounts).