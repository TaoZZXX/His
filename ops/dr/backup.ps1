$ErrorActionPreference = "Stop"

$ts = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = ".\backup_$ts"
New-Item -ItemType Directory -Path $backupDir | Out-Null

Write-Host "Exporting MySQL his schema..."
mysqldump -uroot -p123456 his > "$backupDir\his.sql"

Write-Host "Exporting nacos config snapshot..."
nacos-config-export -o "$backupDir\nacos-config.json"

Write-Host "Backup completed: $backupDir"
