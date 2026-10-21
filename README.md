# Cloud Usuarios · BFF

API REST (Spring Boot 4 · Java 21) para administrar la tabla `usuarios`, protegida
con JWT de Amazon Cognito (grupo `administrador`).

## Instancia documentada

| Dato | Valor |
| --- | --- |
| Instancia EC2 | `i-034c79128b1fa7373` |
| IP pública | `77.113.69.25` |
| Servicio systemd | `cloud-usuarios-bff` |
| Jar en la instancia | `/opt/cloud-usuarios-bff/app.jar` |
| Variables de entorno | `/etc/cloud-usuarios-bff/env` (root, 600) |
| Puerto | `8080` |

## Desarrollo local

```bash
cp .env.example .env      # completa DB_PASSWORD
set -a && source .env && set +a
./gradlew bootRun
```

- API: `http://localhost:8080/api/usuarios` (401 sin token).
- Cognito: issuer `https://cognito-idp.us-east-2.amazonaws.com/us-east-2_KDduqjQdn`.
- `spring.jpa.hibernate.ddl-auto=update` crea/actualiza la tabla `usuarios`
  (solo desarrollo).

## Despliegue (GitHub Actions + git flow)

Flujo de ramas:

```
feature/*  --PR-->  develop  --PR-->  main  --push-->  deploy a EC2
                       |                  |
                    CI (build)        tag v*  -> también despliega
```

| Workflow | Se dispara con | Qué hace |
| --- | --- | --- |
| `.github/workflows/ci.yml` | PR a `develop`/`main`, push a `develop` | `./gradlew clean bootJar` |
| `.github/workflows/deploy.yml` | push a `main`, tag `v*`, manual | build + scp + restart + health check + rollback |

Instalación de git flow (opcional, el flujo también se puede seguir a mano):

```bash
brew install git-flow-avh
git flow init -d            # main = main, develop = develop
git flow feature start alta-usuarios
git flow feature finish alta-usuarios
git flow release start v0.1.0
git flow release finish v0.1.0   # mergea a main y crea el tag -> deploy
git push origin main develop --tags
```

### Preparar la instancia (una sola vez)

```bash
scp deploy/setup-ec2.sh ec2-user@77.113.69.25:/tmp/
ssh ec2-user@77.113.69.25 'sudo bash /tmp/setup-ec2.sh'
```

El script instala Java 21, crea el usuario `cloudusuarios`, los directorios y el
servicio systemd `cloud-usuarios-bff` (queda habilitado, arranca en el primer deploy).

### Secrets y Variables en GitHub

`Settings → Secrets and variables → Actions`

**Secrets**

| Nombre | Contenido |
| --- | --- |
| `EC2_SSH_PRIVATE_KEY` | clave privada (PEM/OpenSSH) del key pair de la instancia |
| `DB_PASSWORD` | password de PostgreSQL/Supabase |

**Variables**

| Nombre | Ejemplo |
| --- | --- |
| `EC2_HOST` | `77.113.69.25` |
| `EC2_USER` | `ec2-user` (`ubuntu` si la AMI es Ubuntu) |
| `DB_URL` | `jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:5432/postgres` |
| `DB_USERNAME` | `postgres.smdaoltztboxrpumbfrh` |
| `COGNITO_REGION` | `us-east-2` |
| `COGNITO_USER_POOL_ID` | `us-east-2_KDduqjQdn` |
| `COGNITO_CLIENT_ID` | `8vu37n7dpq595tfgsg8nrajc2` |

Notas:

- El pipeline escribe `/etc/cloud-usuarios-bff/env` en cada deploy: GitHub es la
  fuente de verdad de la configuración.
- La clave pública del key pair debe estar en `~/.ssh/authorized_keys` del usuario
  SSH de la instancia.
- GitHub-hosted runners tienen IP dinámica: el Security Group debe permitir SSH
  (`22`) desde `0.0.0.0/0` (o usar SSM/self-hosted runner).
- Abre el puerto `8080` en el Security Group para el origen del front.
- La IP pública cambia si detienes/inicias la instancia: asocia una **Elastic IP**
  y actualiza la variable `EC2_HOST`.

### Rollback

Cada deploy guarda la versión anterior en `/opt/cloud-usuarios-bff/app.jar.prev`.
Si el health check falla, el workflow restaura ese jar automáticamente. Manual:

```bash
sudo install -m 644 -o cloudusuarios -g cloudusuarios \
  /opt/cloud-usuarios-bff/app.jar.prev /opt/cloud-usuarios-bff/app.jar
sudo systemctl restart cloud-usuarios-bff
```

### Logs

```bash
ssh ec2-user@77.113.69.25 'sudo journalctl -u cloud-usuarios-bff -f'
```
