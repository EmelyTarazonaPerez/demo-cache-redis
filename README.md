# 🚀 Optimización de Desempeño con Caché – Redis & Spring Boot

## 📌 Descripción
Este proyecto demuestra cómo optimizar el desempeño de una API REST utilizando **Redis** como sistema de caché en memoria, integrado con **Spring Boot**.  
El objetivo principal es reducir la latencia y la carga sobre la base de datos mediante el uso de cacheo eficiente.

---

## 🧠 ¿Por qué Redis?
Redis es ampliamente utilizado en la industria porque:

- Funciona en memoria (RAM) → alta velocidad
- Permite TTL (Time To Live)
- Soporta estructuras de datos avanzadas
- Tiene integración nativa con Spring Boot
- Es más flexible y escalable que Memcached

Redis se utiliza como **optimización**, no como reemplazo de la base de datos.

---

## 🏗️ Arquitectura del Proyecto

- El **controller** no maneja cache
- El **service** gestiona la lógica de cache
- Redis almacena los datos temporalmente en memoria

---

## ⚙️ Tecnologías Utilizadas

- Java 17+
- Spring Boot
- Spring Cache
- Spring Data Redis
- Redis (Docker / Local)
- Jackson (serialización JSON)

---

## 🔧 Configuración Básica

### Dependencias principales
```xml
spring-boot-starter-data-redis
spring-boot-starter-cache
spring-boot-starter-web

## Habilitador de Cache
@EnableCaching

## Configuracion Redis yml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```

🔑 Diseño de Keys (Buenas Prácticas)

- Redis no entiende usuarios ni objetos de negocio.
- La aplicación define la relación mediante el diseño de la key.

Formato recomendado:
```xml
<dominio>:<entidad>:<id>:<contexto>
user:42
user:42:profile
user:42:orders
```
El diseño de keys se define en la capa de servicio, preferiblemente centralizado en una clase de constantes.
---
## 🔁 Funcionamiento del Caché
### Primera llamada (Cache Miss)
- Spring intercepta el método
- Se genera la key
- Redis no encuentra el dato
- Se consulta la base de datos
- El resultado se serializa
- Se guarda en Redis con TTL

### Segunda llamada (Cache Hit)
- Spring consulta Redis
- Redis devuelve el dato
- El método no se ejecuta
- La respuesta se entrega en milisegundos
---
### Serialización de Objetos

- Redis almacena bytes, no objetos Java
- Spring serializa el objeto (JSON)
- Redis guarda el valor en RAM
- Spring deserializa al recuperar
- El diseño del objeto vive únicamente en el código Java.

### 🧹 TTL e Invalidación
## TTL (obligatorio)

- Evita datos obsoletos y consumo excesivo de memoria.
- .entryTtl(Duration.ofMinutes(5))
- Invalidación de cache
- Cuando el dato cambia:
- @CacheEvict(value = "users", key = "#id")
---
### ⚠️ Buenas Prácticas

- Redis es una optimización, no una dependencia crítica
- Siempre usar TTL
- Cachear objetos pequeños (KB, no MB)
- Diseñar bien las keys para evitar colisiones
- Manejar fallos de Redis sin afectar la aplicación
---
### 🚫 Qué NO hacer

- Cachear en el controller
- Guardar archivos grandes
- No usar TTL
- Usar Redis como base de datos principal

###🎯 Conclusión

- Reds permite mejorar significativamente el desempeño de aplicaciones Spring Boot.
- Una implementación correcta requiere no solo agregar dependencias, sino también diseñar claves, definir TTL, manejar serialización e invalidar correctamente el cache.
