# 🧠 REDIS — RESUMEN COMPLETO (JWT & SESIONES)

## 1️⃣ ¿Qué es Redis en tu proyecto?

Redis lo estás usando como:

- 🧠 almacenamiento temporal
- 🔐 control de sesiones
- 🚫 invalidar JWT (logout real)
- 👉 Aunque JWT es stateless, Redi le da estado.
---

## 2️⃣ Tipos de datos de Redis (los importantes)
### 🔹 STRING

- Guarda un solo valor
- Se sobrescribe completo

Spring
```
redisTemplate.opsForValue().set(key, value);
```
Redis CLI
```
SET key value
GET key
TYPE key   # string
```

📌 Útil para flags, tokens simples, contadores pequeños.
---

### 🔹 HASH ⭐ (el más importante para sesiones)
- Similar a un objeto / mapa
- Ideal para guardar información estructurada

Spring
```
redisTemplate.opsForHash().put(key, field, value);
redisTemplate.opsForHash().putAll(key, map);
```
Redis CLI
```
HSET key field value
HGET key field
HGETALL key
TYPE key   # hash
```

### 📌 Ideal para:
- sesiones
- dispositivos
- metadata
---

### 🔹 SET (multi-sesión)

- No admite duplicados
- Perfecto para listas de JTIs

Spring
```
redisTemplate.opsForSet().add(key, value);
```
Redis CLI
```
SADD key value
SMEMBERS key
TYPE key   # set
```

📌 Ideal para:
- múltiples sesiones por usuario
- dispositivos conectados
---

3️⃣ Estructura CORRECTA de sesiones (la clave de todo)
✔️ Una sesión = un JWT = un JTI = un HASH
session:<jti>   (HASH)
 ├─ userId
 ├─ device
 ├─ ip
 ├─ userAgent
 └─ loginAt

✔️ Un usuario puede tener varias sesiones
user:sessions:<userId>   (SET)
 ├─ jti1
 ├─ jti2
 └─ jti3

4️⃣ 🚫 Error común (que tú tenías)

❌ Usar la misma key como HASH y STRING

opsForHash().putAll("session:123", data);
opsForValue().set("session:123", userId); // ❌ borra el hash


📌 Redis NO permite dos tipos en una misma key

5️⃣ Método correcto para crear sesión
public void createSession(String userId, String jti, long exp, DeviceInfo deviceInfo) {

    long ttl = exp - System.currentTimeMillis();

    String sessionKey = "session:" + jti;
    String userSessionsKey = "user:sessions:" + userId;

    Map<String, String> sessionData = new HashMap<>();
    sessionData.put("userId", userId);
    sessionData.put("device", deviceInfo.device());
    sessionData.put("ip", deviceInfo.ip());
    sessionData.put("userAgent", deviceInfo.userAgent());
    sessionData.put("loginAt", deviceInfo.loginAt());

    redisTemplate.opsForHash().putAll(sessionKey, sessionData);
    redisTemplate.expire(sessionKey, ttl, TimeUnit.MILLISECONDS);

    redisTemplate.opsForSet().add(userSessionsKey, jti);
    redisTemplate.expire(userSessionsKey, ttl, TimeUnit.MILLISECONDS);
}

6️⃣ TTL (expiración)

Redis borra automáticamente la key cuando expira.

redisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);


📌 El TTL debe ser igual al exp del JWT.

7️⃣ Cómo VER el contenido en Redis (CLI)
🔍 Ver sesiones
KEYS session:*

🔍 Ver una sesión
TYPE session:<jti>
HGETALL session:<jti>

🔍 Ver sesiones de un usuario
SMEMBERS user:sessions:<userId>

8️⃣ Logout (la razón principal de Redis)
🔹 Logout de UNA sesión

👉 Usa el token de esa sesión

redisTemplate.delete("session:" + jti);
redisTemplate.opsForSet()
    .remove("user:sessions:" + userId, jti);

🔹 Logout de TODAS las sesiones
Set<String> jtis = redisTemplate.opsForSet()
        .members("user:sessions:" + userId);

for (String jti : jtis) {
    redisTemplate.delete("session:" + jti);
}

redisTemplate.delete("user:sessions:" + userId);

9️⃣ JWT + Redis (flujo mental)
🔐 Login

Usuario se autentica

Generas JWT con jti

Guardas sesión en Redis

🔎 Request protegida

Extraes JWT

Sacas jti

Verificas:

redisTemplate.hasKey("session:" + jti)


❌ Si no existe → sesión inválida

🚪 Logout

Borras la sesión en Redis

El JWT queda inválido aunque no haya expirado

🔟 Regla de oro 🏆

JWT dice quién eres
Redis dice si sigues logueado
