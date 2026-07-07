#!/usr/bin/env bash
#
# dev.sh — start/stop the Secondhand backend with one command.
#
#   ./dev.sh            start databases + all backend services (background)
#   ./dev.sh up         same as above
#   ./dev.sh up user    start only specific services (user note message gateway promotion)
#   ./dev.sh stop       stop the backend services (databases keep running)
#   ./dev.sh down       stop services AND databases
#   ./dev.sh logs       tail the combined service logs
#   ./dev.sh status     show which services are up
#   ./dev.sh restart    stop then start
#
set -euo pipefail
cd "$(dirname "$0")"

# Services started by default. Promotion is excluded (not routed through the gateway).
DEFAULT_SERVICES=(service-user service-note service-message gateway)

# port for each module
port_for() {
  case "$1" in
    gateway)           echo 7100 ;;
    service-user)      echo 7101 ;;
    service-note)      echo 7102 ;;
    service-message)   echo 7103 ;;
    service-promotion) echo 7104 ;;
    *) echo "" ;;
  esac
}
ALL_SERVICES=(gateway service-user service-note service-message service-promotion)

LOG_DIR="logs"

# Accept short aliases: "user" -> "service-user", "gateway" stays "gateway".
normalize() {
  case "$1" in
    gateway) echo gateway ;;
    service-*) echo "$1" ;;
    *) echo "service-$1" ;;
  esac
}

load_env() {
  if [[ -f .env ]]; then
    echo "Loading .env"
    set -a; source .env; set +a
  else
    echo "No .env found — relying on already-exported environment variables."
  fi
}

wait_for_postgres() {
  echo -n "Waiting for PostgreSQL"
  for _ in $(seq 1 30); do
    if docker compose exec -T postgres pg_isready -U postgres -d secondhand >/dev/null 2>&1; then
      echo " — ready."
      return 0
    fi
    echo -n "."
    sleep 1
  done
  echo
  echo "PostgreSQL did not become ready in time." >&2
  return 1
}

is_up() { [[ -n "$(lsof -ti "tcp:$1" 2>/dev/null || true)" ]]; }

cmd_up() {
  load_env
  mkdir -p "$LOG_DIR"

  echo "Starting databases (docker compose)..."
  docker compose up -d
  wait_for_postgres

  echo "Installing shared library (service-common)..."
  mvn -q -pl service-common install -DskipTests

  local services=("$@")
  [[ ${#services[@]} -eq 0 ]] && services=("${DEFAULT_SERVICES[@]}")

  for raw in "${services[@]}"; do
    local svc port
    svc="$(normalize "$raw")"
    port="$(port_for "$svc")"
    if [[ -z "$port" ]]; then
      echo "  unknown service '$raw' — skipping" >&2
      continue
    fi
    if is_up "$port"; then
      echo "  $svc already running on :$port — skipping"
      continue
    fi
    echo "  starting $svc on :$port  (logs: $LOG_DIR/$svc.log)"
    nohup mvn -pl "$svc" spring-boot:run >"$LOG_DIR/$svc.log" 2>&1 &
  done

  echo
  echo "Services are starting (first boot may take ~20-40s while they compile)."
  echo "  tail logs:  ./dev.sh logs"
  echo "  status:     ./dev.sh status"
  echo "  gateway:    http://localhost:7100"
}

cmd_stop() {
  echo "Stopping backend services..."
  for svc in "${ALL_SERVICES[@]}"; do
    local port pids
    port="$(port_for "$svc")"
    pids="$(lsof -ti "tcp:$port" 2>/dev/null || true)"
    if [[ -n "$pids" ]]; then
      echo "  stopping $svc (:$port)"
      # shellcheck disable=SC2086
      kill $pids 2>/dev/null || true
    fi
  done
  echo "Done. Databases are still running (use './dev.sh down' to stop them too)."
}

cmd_down() {
  cmd_stop
  echo "Stopping databases..."
  docker compose down
}

cmd_logs() {
  mkdir -p "$LOG_DIR"
  if compgen -G "$LOG_DIR/*.log" >/dev/null; then
    tail -n 40 -F "$LOG_DIR"/*.log
  else
    echo "No logs yet — start the backend with ./dev.sh up"
  fi
}

cmd_status() {
  for svc in "${ALL_SERVICES[@]}"; do
    local port
    port="$(port_for "$svc")"
    if is_up "$port"; then
      printf "  %-18s :%s  UP\n"   "$svc" "$port"
    else
      printf "  %-18s :%s  down\n" "$svc" "$port"
    fi
  done
}

sub="${1:-up}"; shift || true
case "$sub" in
  up)      cmd_up "$@" ;;
  stop)    cmd_stop ;;
  down)    cmd_down ;;
  logs)    cmd_logs ;;
  status)  cmd_status ;;
  restart) cmd_stop; cmd_up "$@" ;;
  -h|--help|help)
    grep '^#' "$0" | sed 's/^# \{0,1\}//' | head -n 20 ;;
  *)
    echo "Usage: ./dev.sh [up|stop|down|logs|status|restart] [services...]" >&2
    exit 1 ;;
esac
