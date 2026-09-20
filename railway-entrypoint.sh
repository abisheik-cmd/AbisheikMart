#!/usr/bin/env bash
set -euo pipefail

: "${PORT:=8080}"
: "${H2_PORT:=9123}"
: "${H2_DATA_DIR:=/data/h2}"
: "${DB_URL:=jdbc:h2:tcp://127.0.0.1:${H2_PORT}//${H2_DATA_DIR}/abisheikmartdb;DB_CLOSE_DELAY=-1}"
export PORT H2_PORT H2_DATA_DIR DB_URL

mkdir -p "${H2_DATA_DIR}"

# Railway exposes the HTTP service through PORT. Keep Tomcat 9 as the servlet runtime.
sed -i -E "s/port=\"8080\" protocol=\"HTTP\\/1.1\"/port=\"${PORT}\" protocol=\"HTTP\\/1.1\"/" "${CATALINA_HOME}/conf/server.xml"

# H2 is deliberately run in TCP Server Mode. The Railway volume keeps its database files persistent.
# H2 Server Mode does not create a missing remote database by default, so create the
# empty database file once in embedded mode before opening the TCP listener.
H2_JAR="/opt/h2/h2-2.2.224.jar"
if [[ ! -f "${H2_JAR}" ]]; then
  echo "H2 driver jar not found at ${H2_JAR}" >&2
  exit 1
fi

java -cp "${H2_JAR}" org.h2.tools.Shell \
  -url "jdbc:h2:${H2_DATA_DIR}/abisheikmartdb" \
  -user "${DB_USERNAME}" \
  -password "${DB_PASSWORD}" \
  -sql "SELECT 1" \
  > /tmp/h2-bootstrap.log 2>&1

java -cp "${H2_JAR}" org.h2.tools.Server \
  -tcp \
  -tcpPort "${H2_PORT}" \
  -tcpAllowOthers \
  -baseDir "${H2_DATA_DIR}" \
  > /tmp/h2-server.log 2>&1 &
H2_PID=$!

cleanup() {
  kill "${H2_PID}" 2>/dev/null || true
}
trap cleanup EXIT TERM INT

for attempt in {1..30}; do
  if (echo > "/dev/tcp/127.0.0.1/${H2_PORT}") >/dev/null 2>&1; then
    break
  fi
  if ! kill -0 "${H2_PID}" 2>/dev/null; then
    cat /tmp/h2-bootstrap.log /tmp/h2-server.log >&2 || true
    exit 1
  fi
  sleep 1
done

if ! (echo > "/dev/tcp/127.0.0.1/${H2_PORT}") >/dev/null 2>&1; then
  cat /tmp/h2-bootstrap.log /tmp/h2-server.log >&2 || true
  echo "H2 TCP server did not become ready" >&2
  exit 1
fi

echo "Starting AbisheikMart on Tomcat ${CATALINA_HOME##*/} with H2 TCP Server Mode"
exec "${CATALINA_HOME}/bin/catalina.sh" run
