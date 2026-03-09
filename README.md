# ❄️ Servi A/C Pro - Backend API

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)

Una API RESTful robusta desarrollada en **Java y Spring Boot** para administrar de manera integral un negocio de venta, instalación y mantenimiento de aires acondicionados.

Este sistema está diseñado para manejar tanto la parte operativa (agenda de técnicos, equipos del cliente, ubicaciones) como la parte comercial (ventas, catálogo de productos y clientes).

## 🚀 Características Principales

* **Gestión de Usuarios y Roles:** Control de acceso interno estricto para perfiles de Administradores y Técnicos.
* **Directorio de Clientes:** Registro detallado con validación de formato para documentos (DUI salvadoreño), coordenadas de geolocalización para visitas y borrado lógico para mantener la integridad referencial.
* **Calculadora Inteligente y Catálogo:** Jerarquía recursiva de categorías (Residencial/Industrial) y gestión de productos enlazados a capacidades en BTU para el cálculo de áreas a climatizar.
* **Agenda y Prevención de Choques:** Sistema central de citas para servicios técnicos con `Triggers` a nivel de base de datos que impiden empalmes de horarios entre técnicos.
* **Historial de Equipos (Trazabilidad):** Registro de las máquinas físicas exactas instaladas en las propiedades de los clientes para facilitar el seguimiento de garantías y mantenimientos futuros.

## 🛠️ Stack Tecnológico y Arquitectura

* **Lenguaje:** Java 17+
* **Framework:** Spring Boot
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** MySQL
* **Validaciones:** Spring Boot Starter Validation (`@Valid`, Regex)
* **Arquitectura:** Multicapa estandarizada (Controllers, Services, Repositories, Entities) implementando el Patrón DTO (Data Transfer Object) para la protección y transporte de datos.

## 📁 Estructura del Proyecto

```text
src/main/java/com/climatizacion/
 ├── config/       # Configuraciones globales (Seguridad, CORS)
 ├── controller/   # Endpoints REST (API)
 ├── dto/          # Data Transfer Objects (Request/Response)
 ├── entity/       # Modelos mapeados a la base de datos
 ├── enums/        # Tipos de datos estandarizados (Roles, Géneros, Estados)
 ├── repository/   # Interfaces de acceso a datos (Spring Data JPA)
 └── service/      # Reglas de negocio e interfaces
      └── impl/    # Implementaciones concretas de los servicios
