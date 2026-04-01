#!/bin/sh

set -eu

BACKUP_DIR="${BACKUP_DIR:-/backups/mysql}"
RETENTION_COUNT="${RETENTION_COUNT:-4}"
MYSQL_HOST="${MYSQL_HOST:-mysql}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-fitexa}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
TZ="${TZ:-America/Buenos_Aires}"

export TZ
export MYSQL_PWD="$MYSQL_PASSWORD"

mkdir -p "$BACKUP_DIR"

log() {
  printf '[mysql-backup] %s\n' "$1"
}

seconds_until_next_run() {
  now_epoch="$(date +%s)"
  today_run="$(date -d "$(date +%F) 02:00:00" +%s)"

  if [ "$now_epoch" -lt "$today_run" ]; then
    echo $((today_run - now_epoch))
    return
  fi

  tomorrow_run="$(date -d "tomorrow 02:00:00" +%s)"
  echo $((tomorrow_run - now_epoch))
}

wait_for_mysql() {
  until mysqladmin ping -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" --silent; do
    log "Esperando a MySQL en $MYSQL_HOST:$MYSQL_PORT..."
    sleep 5
  done
}

perform_backup() {
  timestamp="$(date +%Y-%m-%d_%H-%M-%S)"
  output_file="$BACKUP_DIR/fitexa_$timestamp.sql.gz"
  tmp_file="$output_file.tmp"

  log "Generando backup en $output_file"
  mysqldump \
    -h "$MYSQL_HOST" \
    -P "$MYSQL_PORT" \
    -u "$MYSQL_USER" \
    --single-transaction \
    --quick \
    --routines \
    --triggers \
    "$MYSQL_DATABASE" | gzip -9 > "$tmp_file"

  mv "$tmp_file" "$output_file"

  old_backups="$(find "$BACKUP_DIR" -maxdepth 1 -type f -name 'fitexa_*.sql.gz' | sort -r | awk "NR>$RETENTION_COUNT")"
  if [ -n "$old_backups" ]; then
    echo "$old_backups" | while IFS= read -r file; do
      [ -n "$file" ] && rm -f "$file"
    done
  fi

  log "Backup completado"
}

log "Servicio de backup iniciado. Zona horaria: $TZ. Destino: $BACKUP_DIR"
wait_for_mysql

while true; do
  sleep_seconds="$(seconds_until_next_run)"
  log "Proximo backup en $sleep_seconds segundos"
  sleep "$sleep_seconds"
  wait_for_mysql
  perform_backup
done
