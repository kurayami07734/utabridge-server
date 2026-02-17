#!/bin/sh

BACKUP_DIR="/backups"
RETENTION_DAYS=7

echo "Starting Backup Service..."
echo "Target: $PG_HOST:$PGPORT/$PGDB"

while true; do
    DATE=$(date +%Y%m%d_%H%M%S)
    FILE="$BACKUP_DIR/db_backup_$DATE.sql.gz"

    echo "[$DATE] Creating backup..."

    # pg_dump consumes PGPASSWORD env var automatically
    pg_dump -h "$PGHOST" -U "$PGUSER" -d "$PGDB" | gzip > "$FILE"

    if [ $? -eq 0 ]; then
        echo "[$DATE] Backup success: $FILE"
    else
        echo "[$DATE] Backup FAILED"
    fi

    echo "Cleaning up backups older than $RETENTION_DAYS days..."
    find $BACKUP_DIR -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete

    echo "Sleeping for 12 hours..."
    sleep 43200
done