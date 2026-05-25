# ============================================================
# Pipeline SonarQube para proyectos Java/Maven
# Autor: Miguel Guevara / Investigación sycophancy-code-quality
# Entorno: Windows 10/11
# ============================================================

$RootPath = "C:\ws_ui\sycophancy-code-quality-evaluation"
$ScenariosPath = Join-Path $RootPath "scenarios"

$SonarHost = "http://localhost:9000"
$SonarScanner = "C:\sonar-scanner\bin\sonar-scanner.bat"

$ResultsPath = Join-Path $RootPath "results"
$CsvPath = Join-Path $ResultsPath "metrics.csv"
$LogPath = Join-Path $ResultsPath "pipeline.log"

$Metrics = @(
    "code_smells",
    "sqale_index",
    "cognitive_complexity",
    "complexity",
    "bugs",
    "vulnerabilities",
    "duplicated_lines_density",
    "maintainability_rating",
    "reliability_rating",
    "security_rating",
    "ncloc"
)

# ============================================================
# VALIDACIONES
# ============================================================

if (-not $env:SONAR_TOKEN) {
    Write-Error "No existe la variable SONAR_TOKEN"
    exit 1
}

if (-not (Test-Path $ScenariosPath)) {
    Write-Error "No existe la carpeta scenarios"
    exit 1
}

if (-not (Test-Path $SonarScanner)) {
    Write-Error "No existe SonarScanner en: $SonarScanner"
    exit 1
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "Maven no está disponible"
    exit 1
}

if (-not (Test-Path $ResultsPath)) {
    New-Item -ItemType Directory -Path $ResultsPath | Out-Null
}

"Inicio pipeline: $(Get-Date)" | Out-File $LogPath -Encoding UTF8

# ============================================================
# LOGGING
# ============================================================

function Write-Log {

    param([string]$Message)

    $line = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - $Message"

    Write-Host $line

    Add-Content `
        -Path $LogPath `
        -Value $line `
        -Encoding UTF8
}

# ============================================================
# RUN COMMAND
# ============================================================

function Run-Command {

    param(
        [string]$Command,
        [string]$WorkingDirectory
    )

    Write-Log "Ejecutando: $Command"

    Push-Location $WorkingDirectory

    $output = cmd.exe /c "$Command 2>&1"

    $exitCode = $LASTEXITCODE

    foreach ($line in $output) {

        Write-Host $line

        Add-Content `
            -Path $LogPath `
            -Value $line `
            -Encoding UTF8
    }

    Pop-Location

    Write-Log "Código de salida: $exitCode"

    return [int]$exitCode
}

# ============================================================
# OBTENER MÉTRICAS
# ============================================================

function Get-SonarMetrics {

    param([string]$ProjectKey)

    Start-Sleep -Seconds 5

    $metricKeys = ($Metrics -join ",")

    $uri = "$SonarHost/api/measures/component?component=$ProjectKey&metricKeys=$metricKeys"

    $headers = @{
        Authorization = "Bearer $env:SONAR_TOKEN"
    }

    $row = [ordered]@{
        project_key = $ProjectKey
    }

    foreach ($metric in $Metrics) {
        $row[$metric] = ""
    }

    try {

        $response = Invoke-RestMethod `
            -Uri $uri `
            -Method Get `
            -Headers $headers

        foreach ($measure in $response.component.measures) {

            $row[$measure.metric] = $measure.value
        }

        Write-Log "Métricas exportadas: ${ProjectKey}"
    }
    catch {

        Write-Log "ERROR exportando métricas ${ProjectKey}: $($_.Exception.Message)"
    }

    return New-Object PSObject -Property $row
}

# ============================================================
# DESCUBRIMIENTO DE PROYECTOS
# ============================================================

Write-Log "Buscando proyectos Maven en: $ScenariosPath"

$Projects = Get-ChildItem `
    -Path $ScenariosPath `
    -Recurse `
    -Filter "pom.xml" |

    Where-Object {
        Test-Path (Join-Path $_.Directory.FullName "src\main\java")
    } |

    ForEach-Object {
        $_.Directory.FullName
    } |

    Sort-Object -Unique

Write-Log "Proyectos detectados: $($Projects.Count)"

$Results = @()

# ============================================================
# PROCESAMIENTO PRINCIPAL
# ============================================================

foreach ($ProjectPath in $Projects) {

    $ProjectKey = Split-Path $ProjectPath -Leaf

    $StartTime = Get-Date

    Write-Log "---------------------------------------------"
    Write-Log "Procesando proyecto: ${ProjectKey}"
    Write-Log "Ruta: $ProjectPath"

    try {

        # ====================================================
        # COMPILACIÓN
        # ====================================================

        $compileExit = Run-Command `
            -Command "mvn clean compile -DskipTests" `
            -WorkingDirectory $ProjectPath

        if ($compileExit -eq 0) {

            Write-Log "Compilación exitosa para ${ProjectKey}"
        }
        else {

            Write-Log "WARNING: compilación falló para ${ProjectKey}"
        }

        # ====================================================
        # VALIDAR TARGET/CLASSES
        # ====================================================

        $binariesPath = Join-Path $ProjectPath "target\classes"

        if (-not (Test-Path $binariesPath)) {

            Write-Log "No existe target/classes para ${ProjectKey}"
            Write-Log "Se omite análisis SonarQube"

            continue
        }

        # ====================================================
        # CONFIGURAR TESTS
        # ====================================================

        $testsPath = Join-Path $ProjectPath "src\test\java"

        $testsArgument = ""

        if (Test-Path $testsPath) {

            $testsArgument = '-D"sonar.tests=src/test/java"'

            Write-Log "Se detectó src/test/java para ${ProjectKey}"
        }

        # ====================================================
        # SONAR SCANNER
        # ====================================================

        $scanCommand = @"
"$SonarScanner" ^
-D"sonar.projectKey=$ProjectKey" ^
-D"sonar.projectName=$ProjectKey" ^
-D"sonar.projectVersion=1.0" ^
-D"sonar.host.url=$SonarHost" ^
-D"sonar.token=$env:SONAR_TOKEN" ^
-D"sonar.sources=src/main/java" ^
$testsArgument ^
-D"sonar.java.binaries=target/classes" ^
-D"sonar.sourceEncoding=UTF-8"
"@

        $scanExit = Run-Command `
            -Command $scanCommand `
            -WorkingDirectory $ProjectPath

        if ($scanExit -ne 0) {

            Write-Log "ERROR ejecutando SonarScanner para ${ProjectKey}"

            continue
        }

        Write-Log "SonarScanner finalizó correctamente para ${ProjectKey}"

        # ====================================================
        # EXPORTAR MÉTRICAS
        # ====================================================

        $metricsRow = Get-SonarMetrics -ProjectKey $ProjectKey

        $Results += $metricsRow

        $Elapsed = New-TimeSpan `
            -Start $StartTime `
            -End (Get-Date)

        Write-Log "Proyecto finalizado ${ProjectKey} en $($Elapsed.TotalSeconds) segundos"
    }
    catch {

        Write-Log "ERROR general en proyecto ${ProjectKey}: $($_.Exception.Message)"
    }
}

# ============================================================
# EXPORTACIÓN CSV
# ============================================================

if ($Results.Count -gt 0) {

    $Results | Export-Csv `
        -Path $CsvPath `
        -NoTypeInformation `
        -Encoding UTF8

    Write-Log "CSV generado: $CsvPath"
}
else {

    Write-Log "No se generaron métricas"
}

Write-Log "Fin pipeline: $(Get-Date)"