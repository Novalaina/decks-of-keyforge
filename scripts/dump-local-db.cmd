set PGPASSWORD=postgres
pg_dump --username postgres --no-owner --no-privileges --create --clean --format c --compress=7 --no-unlogged-table-data --file .\db-backups\dok-local-db.backup keyswap
:: pg_dump --username postgres --no-owner --format d --jobs 4 --data-only --no-unlogged-table-data --file .\local-db-dump keyswap
