<#
.SYNOPSIS
  Exporta métricas de múltiples proyectos SonarQube a un CSV consolidado.

.REQUISITOS
  - PowerShell 5.1+
  - Variable de entorno SONAR_TOKEN configurada
  - Acceso a SonarQube Server
  - Proyectos ya analizados en SonarQube

.EJEMPLO
  .\export-sonarqube-metrics.ps1 `
    -SonarUrl "http://localhost:9000" `
    -OutputCsv ".\sonarqube_metrics_consolidado.csv"
#>

param(
    [string]$SonarUrl = "http://localhost:9000",
    [string]$OutputCsv = ".\sonarqube_metrics_consolidado.csv",
    [int]$PageSize = 100
)

$ErrorActionPreference = "Stop"

if (-not $env:SONAR_TOKEN) {
    throw "No existe la variable de entorno SONAR_TOKEN. Configúrala antes de ejecutar el script."
}

$MetricKeys = @(
    "ncloc",
    "code_smells",
    "complexity",
    "cognitive_complexity",
    "duplicated_lines_density",
    "duplicated_blocks",
    "sqale_index",
    "sqale_rating"
)

$Headers = @{
    Authorization = "Bearer $env:SONAR_TOKEN"
}

function Invoke-SonarApi {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Endpoint
    )

    $uri = "$SonarUrl$Endpoint"

    try {
        return Invoke-RestMethod -Uri $uri -Headers $Headers -Method Get
    }
    catch {
        Write-Warning "Error consultando: $uri"
        Write-Warning $_.Exception.Message
        return $null
    }
}

function Get-ExperimentalVariables {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectKey
    )

    # Convención esperada:
    # esc1-ind-claude-r1
    # esc1-neu-gpt-r2
    # esc2-ind-gemini-r5

    $parts = $ProjectKey -split "-"

    $scenario = $null
    $condition = $null
    $model = $null
    $repetition = $null

    if ($parts.Count -ge 4) {
        $scenario = $parts[0]
        $condition = $parts[1]
        $model = $parts[2]
        $repetition = $parts[3]
    }

    $condition_label = switch ($condition) {
        "ind" { "induced" }
        "neu" { "neutral" }
        default { $condition }
    }

    return [ordered]@{
        scenario        = $scenario
        condition_code  = $condition
        condition_label = $condition_label
        model           = $model
        repetition      = $repetition
    }
}

function Get-AllSonarProjects {

    $projects = @()
    $page = 1

    do {
        $endpoint = "/api/projects/search?p=$page&ps=$PageSize"
        $response = Invoke-SonarApi -Endpoint $endpoint

        if ($null -eq $response) {
            throw "No se pudo obtener la lista de proyectos desde SonarQube."
        }

        $projects += $response.components

        $total = [int]$response.paging.total
        $fetched = $page * $PageSize
        $page++

    } while ($fetched -lt $total)

    return $projects
}

# Descomenta el siguiente metodo para identificar metricas que no funcionan
# function Get-ProjectMeasures {
# 
#     param(
#         [Parameter(Mandatory = $true)]
#         [string]$ProjectKey
#     )
# 
#     $measureMap = @{}
# 
#     foreach ($metric in $MetricKeys) {
# 
#         try {
# 
#             $endpoint = "/api/measures/component?component=$ProjectKey&metricKeys=$metric"
# 
#             $response = Invoke-SonarApi -Endpoint $endpoint
# 
#             if (
#                 $null -ne $response `
#                 -and $null -ne $response.component `
#                 -and $null -ne $response.component.measures `
#                 -and $response.component.measures.Count -gt 0
#             ) {
# 
#                 $measureMap[$metric] = $response.component.measures[0].value
#             }
#             else {
# 
#                 $measureMap[$metric] = $null
#             }
#         }
#         catch {
# 
#             Write-Warning "No se pudo obtener métrica [$metric] para proyecto [$ProjectKey]"
# 
#             $measureMap[$metric] = $null
#         }
#     }
# 
#     return $measureMap
# }

function Get-ProjectMeasures {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectKey
    )

    $metrics = ($MetricKeys -join ",")
    $encodedProjectKey = [uri]::EscapeDataString($ProjectKey)
    $endpoint = "/api/measures/component?component=$encodedProjectKey&metricKeys=$([uri]::EscapeDataString($metrics))"

    $response = Invoke-SonarApi -Endpoint $endpoint

    $measureMap = @{}

    foreach ($metric in $MetricKeys) {
        $measureMap[$metric] = $null
    }

    if ($null -eq $response -or $null -eq $response.component.measures) {
        return $measureMap
    }

    foreach ($measure in $response.component.measures) {
        $measureMap[$measure.metric] = $measure.value
    }

    return $measureMap
}

Write-Host "Obteniendo proyectos desde SonarQube..." -ForegroundColor Cyan
$projects = Get-AllSonarProjects

Write-Host "Proyectos encontrados: $($projects.Count)" -ForegroundColor Green

# Descomentar para recorrer los proyectos y hacer debug del Key y name project
# ===== DEBUG TEMPORAL =====
# foreach ($project in $projects) {
# 
#     Write-Host "KEY : $($project.key)"
#     Write-Host "NAME: $($project.name)"
#     Write-Host "--------------------------"
# }
# ==========================

$results = @()

$results = @()

foreach ($project in $projects) {

    $projectKey = $project.key
    $projectName = $project.name

    Write-Host "Procesando: $projectKey" -ForegroundColor Yellow

    $experimentalVars = Get-ExperimentalVariables -ProjectKey $projectKey
    $measures = Get-ProjectMeasures -ProjectKey $projectKey

    $row = [ordered]@{
        project_key     = $projectKey
        project_name    = $projectName
        scenario        = $experimentalVars.scenario
        condition_code  = $experimentalVars.condition_code
        condition_label = $experimentalVars.condition_label
        model           = $experimentalVars.model
        repetition      = $experimentalVars.repetition
    }

    foreach ($metric in $MetricKeys) {
        $value = $measures[$metric]

        if ($null -ne $value -and $value -match "^\d+(\.\d+)?$") {
            $row[$metric] = [double]$value
        }
        else {
            $row[$metric] = $value
        }
    }

    $results += New-Object PSObject -Property $row
}

$results |
    Sort-Object scenario, condition_label, model, repetition, project_key |
    Export-Csv -Path $OutputCsv -NoTypeInformation -Encoding UTF8

Write-Host "CSV generado correctamente en: $OutputCsv" -ForegroundColor Green