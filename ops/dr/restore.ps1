$ErrorActionPreference = "Stop"

param(
  [Parameter(Mandatory = $true)]
  [string]$BackupDir
)

if (!(Test-Path $BackupDir)) {
  throw "Backup directory not found: $BackupDir"
}

Write-Host "Restoring MySQL his schema..."
mysql -uroot -p123456 his < "$BackupDir\his.sql"

if (Test-Path "$BackupDir\nacos-config.json") {
  Write-Host "Restoring nacos config snapshot..."
  nacos-config-import -i "$BackupDir\nacos-config.json"
}

Write-Host "Restore completed."
