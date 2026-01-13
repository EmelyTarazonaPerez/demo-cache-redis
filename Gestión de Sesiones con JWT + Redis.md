# 🔐 Autenticación con JWT + Redis (Gestión de Sesiones)

Este proyecto implementa un sistema de autenticación usando **JWT** junto con **Redis** para manejar sesiones reales, permitiendo:

- Logout inmediato
- Manejo de múltiples dispositivos
- Control de sesiones activas
- Expiración automática sincronizada con JWT
- Seguridad de nivel producción

---

## 🧠 Concepto General

JWT por sí solo es **stateless** y **no se puede revocar**.  
Para solucionar esto, Redis se usa como **store de sesiones**.

| Componente | Responsabilidad |
|----------|----------------|
| JWT | Identidad + expiración |
| Redis | Control de sesiones |
| TTL | Limpieza automática |

👉 **JWT dice quién eres**  
👉 **Redis decide si sigues logueado**

---

## 🔑 Claims JWT Utilizados

Del JWT se extrae toda la información necesaria:

| Claim | Uso |
|-----|----|
| `sub` | ID del usuario (`userId`) |
| `jti` | Identificador único de la sesión |
| `exp` | Expiración real del token |

El TTL de Redis se calcula así:

```text
TTL = exp - currentTimeMillis
```
---

## 🧱 Estructuras en Redis
### 1️⃣ Sesión individual (por dispositivo)
```
Key: session:{jti}
Tipo: HASH
TTL: expiración del JWT
```

Contenido:
- userId
- device
- ip
- userAgent
- loginAt

👉 Representa UNA sesión / UN dispositivo
--- 
### 2️⃣ Índice de sesiones por usuario (multi-sesión)
```
Key: user:sessions:{userId}
Tipo: SET
Valores: jti
```

👉 Permite:

- Multi-dispositivo
- Logout global
- Listar dispositivos
- Cerrar sesiones específicas

🔐 Flujo de LOGIN

- Usuario se autentica
- Se genera JWT (sub, jti, exp)
- Se detecta información del dispositivo
- Se guarda la sesión en Redis
- Se asigna TTL igual al JWT

🧩 DeviceInfo (datos del dispositivo)
```
public record DeviceInfo(
        String detectDevice,
        String ip,
        String userAgent,
        String loginAt
) {}
```
Datos obtenidos desde HttpServletRequest.
```
🧠 Detección básica de dispositivo
private String detectDevice(String userAgent) {
    if (userAgent == null) return "UNKNOWN";
    if (userAgent.contains("Android")) return "Android";
    if (userAgent.contains("iPhone")) return "iPhone";
    if (userAgent.contains("Windows")) return "Windows";
    if (userAgent.contains("Mac")) return "Mac";
    return "Other";
}
```
🧩 SessionService (Redis)
```
Crear sesión (LOGIN)
public void createSession(
        String userId,
        String jti,
        long jwtExpirationMillis,
        DeviceInfo deviceInfo
) {

    long ttl = jwtExpirationMillis - System.currentTimeMillis();
    String key = "session:" + jti;

    Map<String, String> sessionData = new HashMap<>();
    sessionData.put("userId", userId);
    sessionData.put("device", deviceInfo.detectDevice());
    sessionData.put("ip", deviceInfo.ip());
    sessionData.put("userAgent", deviceInfo.userAgent());
    sessionData.put("loginAt", deviceInfo.loginAt());

    redisTemplate.opsForHash().putAll(key, sessionData);
    redisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);

    redisTemplate.opsForSet().add("user:sessions:" + userId, jti);
    redisTemplate.expire("user:sessions:" + userId, ttl, TimeUnit.MILLISECONDS);
}

Validar sesión (en cada request)
public boolean isSessionValid(String jti) {
    return Boolean.TRUE.equals(
            redisTemplate.hasKey("session:" + jti)
    );
}

Logout de una sola sesión
public void logout(String userId, String jti) {
    redisTemplate.delete("session:" + jti);
    redisTemplate.opsForSet().remove("user:sessions:" + userId, jti);
}

Logout global (todas las sesiones)
public void logoutAll(String userId) {

    String key = "user:sessions:" + userId;
    Set<String> sessions = redisTemplate.opsForSet().members(key);

    if (sessions != null) {
        for (String jti : sessions) {
            redisTemplate.delete("session:" + jti);
        }
    }

    redisTemplate.delete(key);
}
```
🔎 Validación en endpoints protegidos

- Se valida el JWT
- Se extrae el jti
- Se consulta Redis:
- EXISTS session:{jti}

✅ Existe → request permitido

❌ No existe → sesión inválida

🚪 Endpoints de Logout
Logout sesión actual
DELETE /logout
Authorization: Bearer <token>

Logout global
DELETE /logout-all
Authorization: Bearer <token>

📱 Listar dispositivos activos
```
public List<Map<Object, Object>> getActiveDevices(String userId) {

    Set<String> sessions =
        redisTemplate.opsForSet().members("user:sessions:" + userId);

    List<Map<Object, Object>> devices = new ArrayList<>();

    if (sessions != null) {
        for (String jti : sessions) {
            Map<Object, Object> data =
                redisTemplate.opsForHash().entries("session:" + jti);
            if (!data.isEmpty()) {
                devices.add(data);
            }
        }
    }

    return devices;
}
```
---
### ⏱️ TTL y limpieza automática

- Redis elimina sesiones automáticamente
- No existen sesiones zombis
- No se requieren cron jobs
- JWT y Redis expiran juntos
---
### 🧪 Comandos Redis útiles
- KEYS session:*
- HGETALL session:{jti}
- TTL session:{jti}
- SMEMBERS user:sessions:{userId}
- DEL session:{jti}
---
### ❌ Errores que este diseño evita

- Logout falso con JWT
- Tokens no revocables
- TTL fijo incorrecto
- No poder cerrar sesiones
- No saber desde dónde inició sesión el usuario
--- 
### 🏆 Nivel de la solución

- Esta arquitectura es de nivel producción, usada en:
- Aplicaciones bancarias
- Apps grandes
- Sistemas con alta seguridad
---
### 🚀 Mejoras futuras

- Limitar número de dispositivos
- Refresh Token
- Alertas de login sospechoso
- Panel de actividad de cuenta
- Integración completa con Spring Security
