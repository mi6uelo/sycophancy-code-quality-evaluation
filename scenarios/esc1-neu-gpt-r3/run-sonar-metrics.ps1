# ============================================================
# Pipeline SonarQube + Maven + Exportacion CSV
# Proyecto: dinamico segun carpeta actual
# ============================================================

$ErrorActionPreference = "Stop"

$ProjectPath = Get-Location
$ProjectKey = Split-Path $ProjectPath -Leaf
$ProjectName = $ProjectKey

$SonarHost = "http://localhost:9000"

Write-Host "Proyecto detectado: $ProjectKey"
Write-Host "SonarQube host: $SonarHost"

$Metrics = @(
    "ncloc",
    "code_smells",
    "complexity",
    "cognitive_complexity",
    "duplicated_lines_density",
    "duplicated_blocks",
    "sqale_index",
    "sqale_rating"
)

if (-not $env:SONAR_TOKEN) {
    Write-Error "No existe la variable de entorno SONAR_TOKEN."
    exit 1
}

$SonarDir = Join-Path (Get-Location) "sonar"

if (-not (Test-Path $SonarDir)) {
    New-Item -ItemType Directory -Path $SonarDir | Out-Null
}

$CsvPath = Join-Path $SonarDir "metrics.csv"
$JsonPath = Join-Path $SonarDir "metrics.json"

Write-Host "Compilando proyecto..."
mvn clean compile -DskipTests

Write-Host "Ejecutando analisis SonarQube..."
mvn sonar:sonar `
    "-Dsonar.projectKey=$ProjectKey" `
    "-Dsonar.projectName=$ProjectName" `
    "-Dsonar.host.url=$SonarHost" `
    "-Dsonar.token=$env:SONAR_TOKEN"

Write-Host "Esperando procesamiento del analisis..."
Start-Sleep -Seconds 8

$headers = @{
    Authorization = "Bearer $env:SONAR_TOKEN"
}

$metricKeys = ($Metrics -join ",")

$uri = "$SonarHost/api/measures/component?component=$ProjectKey&metricKeys=$metricKeys"

Write-Host "Consultando metricas desde API SonarQube..."
$response = Invoke-RestMethod `
    -Uri $uri `
    -Headers $headers `
    -Method Get

$response | ConvertTo-Json -Depth 10 | Out-File $JsonPath -Encoding UTF8

$row = [ordered]@{
    artifact_id = $response.component.key
}

foreach ($metric in $Metrics) {
    $row[$metric] = ""
}

foreach ($m in $response.component.measures) {
    $row[$m.metric] = $m.value
}

$ncloc = 0

if ($row["ncloc"] -ne "") {
    $ncloc = [double]$row["ncloc"]
}

if ($ncloc -gt 0) {
    $row["smells_per_100_loc"] = [math]::Round(([double]$row["code_smells"] / $ncloc) * 100, 4)
    $row["complexity_per_100_loc"] = [math]::Round(([double]$row["complexity"] / $ncloc) * 100, 4)
    $row["cognitive_complexity_per_100_loc"] = [math]::Round(([double]$row["cognitive_complexity"] / $ncloc) * 100, 4)
    $row["debt_minutes_per_loc"] = [math]::Round(([double]$row["sqale_index"] / $ncloc), 4)
}
else {
    $row["smells_per_100_loc"] = ""
    $row["complexity_per_100_loc"] = ""
    $row["cognitive_complexity_per_100_loc"] = ""
    $row["debt_minutes_per_loc"] = ""
}

[PSCustomObject]$row |
    Export-Csv $CsvPath -NoTypeInformation -Encoding UTF8

Write-Host "CSV generado correctamente en: $CsvPath"
Write-Host "JSON respaldo generado en: $JsonPath"