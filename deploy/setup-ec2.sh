#!/usr/bin/env bash
#
# Bootstrap de la instancia EC2 para correr el BFF como servicio systemd.
# Idempotente: se puede correr más de una vez.
#
# Uso:
#   scp deploy/setup-ec2.sh ec2-user@77.113.69.25:/tmp/
#   ssh ec2-user@77.113.69.25 'sudo bash /tmp/setup-ec2.sh'
#
set -euo pipefail

APP_NAME="cloud-usuarios-bff"
APP_USER="cloudusuarios"
APP_DIR="/opt/${APP_NAME}"
CONF_DIR="/etc/${APP_NAME}"

echo "==> Instalando Java 21"
if command -v dnf >/dev/null 2>&1; then
	# Ojo: en Amazon Linux 2023 "curl" choca con "curl-minimal" (ya instalado);
	# no lo agregamos como paquete.
	sudo dnf install -y java-21-amazon-corretto-headless
elif command -v apt-get >/dev/null 2>&1; then
	sudo apt-get update -y
	sudo apt-get install -y openjdk-21-jre-headless curl
else
	echo "ERROR: no reconozco el gestor de paquetes (dnf/apt-get)" >&2
	exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
	echo "ERROR: falta curl en la instancia (lo usa el health check)" >&2
	exit 1
fi

if ! command -v java >/dev/null 2>&1; then
	echo "ERROR: Java no quedó disponible en PATH" >&2
	exit 1
fi
JAVA_BIN="$(command -v java)"
java -version

echo "==> Usuario de servicio (${APP_USER})"
if ! id -u "${APP_USER}" >/dev/null 2>&1; then
	sudo useradd --system --home-dir "${APP_DIR}" --shell /sbin/nologin "${APP_USER}"
fi

echo "==> Directorios"
sudo install -d -m 755 "${APP_DIR}"
sudo install -d -m 750 "${CONF_DIR}"
# El pipeline reemplaza este archivo en cada deploy; queda vacío hasta el primero.
if [ ! -f "${CONF_DIR}/env" ]; then
	sudo install -m 600 -o root -g root /dev/null "${CONF_DIR}/env"
fi

echo "==> Servicio systemd"
sudo tee "/etc/systemd/system/${APP_NAME}.service" >/dev/null <<UNIT
[Unit]
Description=Cloud Usuarios BFF (Spring Boot)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=${APP_USER}
Group=${APP_USER}
WorkingDirectory=${APP_DIR}
EnvironmentFile=${CONF_DIR}/env
ExecStart=${JAVA_BIN} -XX:MaxRAMPercentage=70 -jar ${APP_DIR}/app.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
UNIT

sudo systemctl daemon-reload
sudo systemctl enable "${APP_NAME}"

echo
echo "Servicio habilitado (aún sin jar). El primer push a main lo despliega."
echo "Alternativa manual:"
echo "  sudo install -m 644 -o ${APP_USER} -g ${APP_USER} app.jar ${APP_DIR}/app.jar"
echo "  sudo systemctl restart ${APP_NAME}"
echo "  sudo journalctl -u ${APP_NAME} -f"
