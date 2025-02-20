
set PGPASSWORD=postgres
pg_restore -h localhost -U postgres --clean --create --if-exists --no-tablespaces --no-privileges --no-owner -d keyswap .\db-backups\dok-prod-db.backup
