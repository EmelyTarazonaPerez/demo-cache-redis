# 🔐 Gestión de Sesiones con JWT + Redis

Este proyecto implementa un sistema de autenticación usando **JWT** junto con **Redis** para el control real de sesiones, permitiendo:

- Logout inmediato
- Manejo de múltiples dispositivos
- Expiración automática por TTL
- Control total de sesiones activas

---

## 🧠 Concepto Clave

- **JWT** → Identidad del usuario (stateless)
- **Redis** → Control de sesiones (stateful)

👉 El JWT dice *quién eres*  
👉 Redis decide *si sigues logueado*

---

## 🔑 Claims usados del JWT

| Claim | Uso |
|-----|----|
| `sub` | ID del usuario (`userId`) |
| `jti` | ID único del token (sesión) |
| `exp` | Fecha de expiración del token |

El **TTL en Redis** se calcula como:

