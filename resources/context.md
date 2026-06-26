# MÍTICO ANIMAL GYM — Contexto Técnico del Proyecto

> Documento centralizado de arquitectura, stacks y funcionalidades planeadas.
> Última actualización: Junio 2026

---

## 🏗️ Visión General

**Mítico Animal Gym** es un gimnasio ubicado en Paso del Rey, Buenos Aires. El proyecto digital busca ofrecer una experiencia premium para los miembros y visitantes, con una plataforma que evoluciona desde una landing page informativa hacia un ecosistema completo de gestión deportiva y e-commerce.

El objetivo es construir un producto escalable que permita al dueño gestionar contenidos, productos y operaciones de forma autónoma, sin depender de desarrollo constante.

---

## 🎯 Funcionalidades del Backend

### Fase 1: Autenticación + Gestión de Productos (Activa)

| Módulo | Descripción |
|--------|-------------|
| **Auth & JWT** | Login seguro con email + contraseña, generación y validación de tokens JWT, roles (ADMIN, STAFF) |
| **CRUD Productos** | Alta, baja, modificación y consulta de productos de la tienda (remeras, suplementos, accesorios) |
| **Categorías** | Gestión de categorías de productos (Remeras, Whey, Creatina, etc.) con estado "Próximamente" |
| **Upload de Imágenes** | Subida y servicio de imágenes de productos al filesystem del servidor |
| **Frontend Público** | API REST para que el sitio público consuma productos dinámicamente |
| **Panel Admin** | Endpoints protegidos con JWT para el panel de administración del frontend |

### Fase 2: Miembros y Membresías (Planificada)

| Módulo | Descripción |
|--------|-------------|
| **Registro de Usuarios** | Creación de cuentas para miembros del gimnasio con datos personales |
| **Perfiles de Miembro** | Gestión de datos: nombre, teléfono, fecha de nacimiento, plan activo |
| **Planes/Membresías** | CRUD de planes (mensual, trimestral, anual) con precios y beneficios |
| **Asignación de Planes** | Vincular miembros a planes activos con fecha de inicio y vencimiento |
| **Control de Acceso** | Verificación de membresía activa para ingreso al gimnasio |
| **Historial de Membresías** | Registro de planes anteriores y renovaciones |

### Fase 3: Pagos y Facturación (Planificada)

| Módulo | Descripción |
|--------|-------------|
| **Integración Mercado Pago** | Checkout para pagos de membresías y productos de la tienda |
| **Webhooks MP** | Recepción de notificaciones de pago exitoso, pendiente o rechazado |
| **Historial de Pagos** | Registro de transacciones con estado, monto, fecha y método |
| **Facturación Automática** | Generación de comprobantes fiscales (integración AFIP si aplica) |
| **Suscripciones Recurrentes** | Cobros automáticos mensuales/trimestrales para membresías |
| **Deudas y Vencimientos** | Alertas de membresías por vencer y gestión de morosos |
| **Reintegros** | Procesamiento de devoluciones y anulaciones de pagos |

### Fase 4: Gestión Operativa (Planificada)

| Módulo | Descripción |
|--------|-------------|
| **Clases y Horarios** | Gestión de clases grupales, horarios, cupos y profesores asignados |
| **Reservas** | Sistema de reserva de clases con límite de cupos y lista de espera |
| **Asistencia** | Registro de entrada/salida de miembros (QR, código o biométrico) |
| **Entrenadores** | Perfiles de entrenadores, especialidades, horarios y asignación de clases |
| **Comunicados** | Envío de notificaciones push o email a miembros activos |
| **Reportes** | Dashboard con métricas: ingresos, membresías activas, asistencia, ventas |

### Fase 5: E-commerce Avanzado (Planificada)

| Módulo | Descripción |
|--------|-------------|
| **Carrito de Compras** | Sesión de carrito persistente, agregar/quitar productos, cantidades |
| **Checkout Completo** | Flujo de compra con dirección de envío, método de pago y confirmación |
| **Seguimiento de Pedidos** | Estado de pedidos: pendiente, preparado, enviado, entregado |
| **Stock/Inventario** | Control de stock por producto, alertas de bajo stock |
| **Cupones y Descuentos** | Códigos promocionales con reglas de aplicación |
| **Favoritos/Wishlist** | Lista de productos favoritos por usuario |
| **Reseñas de Productos** | Sistema de calificación y comentarios de compradores |

### Fase 6: Integraciones Externas (Futuro)

| Módulo | Descripción |
|--------|-------------|
