param(
    [string[]]$ProjectNames,
    [switch]$WhatIf
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = 'C:\ws_ui\sycophancy-code-quality-evaluation'
$experimentalRoot = Join-Path $repoRoot 'experimental_artifacts'
$scenariosRoot = Join-Path $repoRoot 'scenarios'

function New-Utf8NoBomEncoding {
    return [System.Text.UTF8Encoding]::new($false)
}

function Normalize-RelativePath {
    param([string]$Path)
    if ([string]::IsNullOrWhiteSpace($Path)) { return $null }
    $p = $Path.Trim()
    $p = $p -replace '^`+|`+$', ''
    $p = $p -replace '^["'']+|["'']+$', ''
    $p = $p.Trim()
    $p = $p -replace '\\','/'
    $p = $p -replace '^[.]/',''
    $p = $p.Trim('/')
    if ([string]::IsNullOrWhiteSpace($p)) { return $null }
    return $p
}

function Append-FileContent {
    param(
        [hashtable]$Buffers,
        [string]$Path,
        [string]$Content
    )
    if ([string]::IsNullOrWhiteSpace($Path)) { return }
    if (-not $Buffers.ContainsKey($Path)) {
        $Buffers[$Path] = [System.Collections.Generic.List[string]]::new()
    }
    $Buffers[$Path].Add($Content)
}

function Get-JavaPathFromContent {
    param([string]$Content)
    $package = [regex]::Match($Content, '(?m)^\s*package\s+([A-Za-z0-9_.]+)\s*;')
    $type = [regex]::Match($Content, '(?m)^\s*public\s+(?:class|interface|enum|record)\s+([A-Za-z0-9_]+)')
    if (-not $type.Success) {
        $type = [regex]::Match($Content, '(?m)^\s*(?:class|interface|enum|record)\s+([A-Za-z0-9_]+)')
    }
    if ($package.Success -and $type.Success) {
        return 'src/main/java/' + ($package.Groups[1].Value -replace '\.','/') + '/' + $type.Groups[1].Value + '.java'
    }
    return $null
}

function Parse-StructureBlock {
    param([string]$Content)
    $results = [System.Collections.Generic.List[string]]::new()
    $stack = @{}
    $rootPrefix = ''
    $lines = $Content -split "`r?`n"
    foreach ($line in $lines) {
        $raw = $line.TrimEnd()
        if ([string]::IsNullOrWhiteSpace($raw)) { continue }
        if ($raw -match '[├└]──') {
            $branchIndex = $raw.IndexOf('├──')
            if ($branchIndex -lt 0) { $branchIndex = $raw.IndexOf('└──') }
            if ($branchIndex -lt 0) { continue }
            $depth = [Math]::Floor($branchIndex / 4)
            $name = $raw.Substring($branchIndex + 3).Trim()
            $name = $name.Trim()
            if ([string]::IsNullOrWhiteSpace($name)) { continue }
            $parent = ''
            if ($depth -eq 0) {
                $parent = $rootPrefix
            } elseif ($stack.ContainsKey($depth - 1)) {
                $parent = $stack[$depth - 1]
            } else {
                $parent = $rootPrefix
            }
            $full = if ([string]::IsNullOrWhiteSpace($parent)) { $name } else { "$parent/$name" }
            $full = Normalize-RelativePath $full
            $isLikelyFile = ($name -match '\.[A-Za-z0-9]+$') -or ($name -eq 'Dockerfile')
            if ($name.EndsWith('/') -or -not $isLikelyFile) {
                $stack[$depth] = $full.TrimEnd('/')
            } else {
                $results.Add($full)
                $directoryName = [System.IO.Path]::GetDirectoryName(($full -replace '/','\'))
                if ([string]::IsNullOrWhiteSpace($directoryName)) {
                    $stack[$depth] = ''
                } else {
                    $stack[$depth] = $directoryName -replace '\\','/'
                }
            }
            continue
        }

        $trimmed = $raw.Trim()
        if ($trimmed -match '^[A-Za-z0-9_./-]+/?$') {
            $normalized = Normalize-RelativePath $trimmed
            if ($normalized -like 'src/*') {
                $rootPrefix = $normalized.TrimEnd('/')
                $stack[0] = $rootPrefix
            }
        }
    }
    return $results
}

function Resolve-TargetPath {
    param(
        [string]$Hint,
        [string]$Language,
        [string]$BlockContent,
        [string[]]$StructurePaths
    )

    $normalizedHint = Normalize-RelativePath $Hint
    if ($normalizedHint) {
        if ($normalizedHint -match '^(src/|pom\.xml$|Dockerfile$|README\.md$)') {
            return $normalizedHint
        }
        if ($normalizedHint -match '\.(java|xml|properties|ya?ml|sql)$') {
            if ($Language -eq 'java') {
                $javaPath = Get-JavaPathFromContent $BlockContent
                if ($javaPath) { return $javaPath }
            }
            if ($normalizedHint -eq 'application.properties') {
                $propMatches = @($StructurePaths | Where-Object { $_ -like '*/application.properties' -or $_ -eq 'application.properties' })
                if ($propMatches.Count -ge 1) { return $propMatches[0] }
                return 'src/main/resources/application.properties'
            }
            if ($normalizedHint -eq 'application.yml') { return 'src/main/resources/application.yml' }
            if ($normalizedHint -eq 'application.yaml') { return 'src/main/resources/application.yaml' }
            $nameOnly = Split-Path $normalizedHint -Leaf
            $matches = @($StructurePaths | Where-Object { (Split-Path $_ -Leaf) -eq $nameOnly })
            if ($matches.Count -eq 1) { return $matches[0] }
            if ($normalizedHint -eq 'pom.xml') { return 'pom.xml' }
            return $normalizedHint
        }
    }

    if ($Language -eq 'java') {
        $javaPath = Get-JavaPathFromContent $BlockContent
        if ($javaPath) { return $javaPath }
    }

    if ($Language -eq 'xml' -and $BlockContent -match '<project\b') {
        return 'pom.xml'
    }

    if ($Language -in @('properties','yaml','yml')) {
        $propMatches = @($StructurePaths | Where-Object { $_ -like '*/application.properties' -or $_ -like '*/application.yml' -or $_ -like '*/application.yaml' })
        if ($propMatches.Count -ge 1) { return $propMatches[0] }
        if ($Language -eq 'properties') { return 'src/main/resources/application.properties' }
        if ($Language -eq 'yml') { return 'src/main/resources/application.yml' }
        if ($Language -eq 'yaml') { return 'src/main/resources/application.yaml' }
    }

    return $null
}

function LooksLikeStructureTree {
    param([string]$Content)
    return ($Content -match '[├└]──')
}

function LooksLikeFileHintBlock {
    param([string]$Content)
    $trimmed = $Content.Trim()
    if ($trimmed -match "`r|`n") { return $false }
    return ($trimmed -match '^(src/)?[A-Za-z0-9_./-]+(\.(java|xml|properties|ya?ml|sql)|/)?$' -or $trimmed -eq 'Dockerfile')
}

function Parse-ReadmeToFiles {
    param([string]$ReadmePath)

    $text = [System.IO.File]::ReadAllText($ReadmePath)
    $lines = $text -split "`r?`n"
    $buffers = @{}
    $structurePaths = [System.Collections.Generic.List[string]]::new()
    $activeTarget = $null
    $pendingHint = $null
    $pendingJavaLines = [System.Collections.Generic.List[string]]::new()

    for ($i = 0; $i -lt $lines.Length; $i++) {
        $line = $lines[$i]

        if ($line -match '^#{1,6}\s*(.+?)\s*$') {
            $headingText = $matches[1].Trim()
            $nameMarker = [regex]::Match($headingText, 'name=([^\s]+)')
            if ($nameMarker.Success) {
                $activeTarget = Normalize-RelativePath $nameMarker.Groups[1].Value
                $pendingHint = $activeTarget
                continue
            }

            if ($activeTarget -and $headingText -match '^[)\]}]+$') {
                Append-FileContent -Buffers $buffers -Path $activeTarget -Content $headingText
                continue
            }

            if (-not $activeTarget -and $pendingJavaLines.Count -gt 0 -and $headingText -match '^[)\]}].*') {
                $pendingJavaLines.Add($headingText)
                $pendingJavaContent = [string]::Join("`n", $pendingJavaLines)
                $resolvedJavaTarget = Resolve-TargetPath -Hint $pendingHint -Language 'java' -BlockContent $pendingJavaContent -StructurePaths $structurePaths
                if (-not $resolvedJavaTarget) {
                    $resolvedJavaTarget = Resolve-TargetPath -Hint $null -Language 'java' -BlockContent $pendingJavaContent -StructurePaths $structurePaths
                }
                if ($resolvedJavaTarget) {
                    Append-FileContent -Buffers $buffers -Path $resolvedJavaTarget -Content $pendingJavaContent
                    $activeTarget = $resolvedJavaTarget
                    $pendingJavaLines.Clear()
                }
                continue
            }

            $fileLike = [regex]::Match($headingText, '((src/)?[A-Za-z0-9_./-]+\.(java|xml|properties|ya?ml|sql)|pom\.xml|Dockerfile)')
            if ($fileLike.Success) {
                $pendingHint = $fileLike.Groups[1].Value
                $activeTarget = Normalize-RelativePath $pendingHint
                if ($activeTarget -notmatch '^(src/|pom\.xml$|Dockerfile$)') {
                    $activeTarget = $null
                }
                continue
            }

            $activeTarget = $null
            $pendingHint = $null
            continue
        }

        if ($line -match '^`{3,}([A-Za-z0-9_-]+)?\s*$') {
            $language = ''
            if ($null -ne $matches[1] -and $matches[1] -is [string] -and $matches[1] -ne '') {
                $language = $matches[1].ToLowerInvariant()
            }
            $blockLines = [System.Collections.Generic.List[string]]::new()
            $i++
            while ($i -lt $lines.Length -and $lines[$i] -notmatch '^`{3,}\s*$') {
                $blockLines.Add($lines[$i])
                $i++
            }
            $blockContent = [string]::Join("`n", $blockLines)
            $trimmedBlock = $blockContent.Trim()

            if ($language -eq 'text' -and (LooksLikeStructureTree $blockContent)) {
                foreach ($path in (Parse-StructureBlock $blockContent)) {
                    if (-not [string]::IsNullOrWhiteSpace($path)) { $structurePaths.Add($path) }
                }
                continue
            }

            if ($language -eq 'text' -and (LooksLikeFileHintBlock $blockContent)) {
                $pendingHint = $trimmedBlock
                $resolvedFromHint = Resolve-TargetPath -Hint $pendingHint -Language $language -BlockContent $blockContent -StructurePaths $structurePaths
                if ($resolvedFromHint) { $activeTarget = $resolvedFromHint }
                continue
            }

            if ([string]::IsNullOrWhiteSpace($trimmedBlock)) { continue }

            $effectiveBlockContent = $blockContent
            if ($language -eq 'java' -and $pendingJavaLines.Count -gt 0) {
                $effectiveBlockContent = ([string]::Join("`n", $pendingJavaLines) + "`n" + $blockContent).Trim("`n")
            }

            $target = $null
            if ($pendingHint) {
                $target = Resolve-TargetPath -Hint $pendingHint -Language $language -BlockContent $effectiveBlockContent -StructurePaths $structurePaths
            }
            if (-not $target -and $activeTarget) {
                $target = $activeTarget
            }
            if (-not $target) {
                $target = Resolve-TargetPath -Hint $null -Language $language -BlockContent $effectiveBlockContent -StructurePaths $structurePaths
            }

            if ($target) {
                Append-FileContent -Buffers $buffers -Path $target -Content $effectiveBlockContent
                $activeTarget = $target
                $pendingJavaLines.Clear()
            } elseif ($language -eq 'java' -and $blockContent -match '(?m)^\s*package\s+[A-Za-z0-9_.]+\s*;') {
                $pendingJavaLines.Clear()
                foreach ($pendingLine in ($blockContent -split "`n")) {
                    $pendingJavaLines.Add($pendingLine)
                }
            } elseif (-not $activeTarget -and $pendingJavaLines.Count -gt 0) {
                foreach ($pendingLine in ($blockContent -split "`n")) {
                    $pendingJavaLines.Add($pendingLine)
                }
                $pendingJavaContent = [string]::Join("`n", $pendingJavaLines)
                $resolvedJavaTarget = Resolve-TargetPath -Hint $pendingHint -Language 'java' -BlockContent $pendingJavaContent -StructurePaths $structurePaths
                if (-not $resolvedJavaTarget) {
                    $resolvedJavaTarget = Resolve-TargetPath -Hint $null -Language 'java' -BlockContent $pendingJavaContent -StructurePaths $structurePaths
                }
                if ($resolvedJavaTarget) {
                    Append-FileContent -Buffers $buffers -Path $resolvedJavaTarget -Content $pendingJavaContent
                    $activeTarget = $resolvedJavaTarget
                    $pendingJavaLines.Clear()
                }
            }
            continue
        }

        if ($activeTarget) {
            Append-FileContent -Buffers $buffers -Path $activeTarget -Content $line
            continue
        }

        if (-not $activeTarget -and $pendingJavaLines.Count -gt 0) {
            $pendingJavaLines.Add($line)
            $pendingJavaContent = [string]::Join("`n", $pendingJavaLines)
            $resolvedJavaTarget = Resolve-TargetPath -Hint $pendingHint -Language 'java' -BlockContent $pendingJavaContent -StructurePaths $structurePaths
            if (-not $resolvedJavaTarget) {
                $resolvedJavaTarget = Resolve-TargetPath -Hint $null -Language 'java' -BlockContent $pendingJavaContent -StructurePaths $structurePaths
            }
            if ($resolvedJavaTarget) {
                Append-FileContent -Buffers $buffers -Path $resolvedJavaTarget -Content $pendingJavaContent
                $activeTarget = $resolvedJavaTarget
                $pendingJavaLines.Clear()
            }
            continue
        }
    }

    $final = @{}
    foreach ($key in $buffers.Keys) {
        $final[$key] = [string]::Join("`n", $buffers[$key])
    }
    return $final
}

function Get-RelevantPackageFileCount {
    param([string]$ScenarioPath)
    $files = Get-ChildItem -LiteralPath $ScenarioPath -Recurse -File -ErrorAction SilentlyContinue | Where-Object {
        $_.FullName -match '\\src\\main\\java\\.*\\controller\\' -or
        $_.FullName -match '\\src\\main\\java\\.*\\entity\\' -or
        $_.FullName -match '\\src\\main\\java\\.*\\model\\entity\\' -or
        $_.FullName -match '\\src\\main\\java\\.*\\repository\\' -or
        $_.FullName -match '\\src\\main\\java\\.*\\service\\impl\\'
    }
    return @($files).Count
}

function Remove-ScenarioContents {
    param([string]$ScenarioPath)
    Get-ChildItem -LiteralPath $ScenarioPath -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force
}

$projects = Get-ChildItem -LiteralPath $experimentalRoot -Directory | Sort-Object Name
if ($ProjectNames -and $ProjectNames.Count -gt 0) {
    $expandedNames = @()
    foreach ($projectName in $ProjectNames) {
        $expandedNames += @($projectName -split ',')
    }
    $lookup = @{}
    foreach ($name in $expandedNames) {
        $trimmedName = $name.Trim()
        if ($trimmedName) { $lookup[$trimmedName] = $true }
    }
    $projects = @($projects | Where-Object { $lookup.ContainsKey($_.Name) })
}

$processed = [System.Collections.Generic.List[string]]::new()
$skipped = [System.Collections.Generic.List[string]]::new()
$failed = [System.Collections.Generic.List[string]]::new()

foreach ($project in $projects) {
    $name = $project.Name
    $readmePath = Join-Path $project.FullName 'README.md'
    $scenarioPath = Join-Path $scenariosRoot $name

    if (-not (Test-Path -LiteralPath $scenarioPath) -or -not (Test-Path -LiteralPath $readmePath)) {
        $failed.Add($name + ' (missing path)')
        continue
    }

    $relevantCount = Get-RelevantPackageFileCount -ScenarioPath $scenarioPath
    if ($relevantCount -gt 0) {
        Write-Host "SKIP $name (relevant files: $relevantCount)"
        $skipped.Add($name)
        continue
    }

    try {
        $files = Parse-ReadmeToFiles -ReadmePath $readmePath
        if ($files.Count -eq 0) {
            throw "No files extracted from README"
        }

        Write-Host "PROCESS $name -> $($files.Count) files"
        if (-not $WhatIf) {
            Remove-ScenarioContents -ScenarioPath $scenarioPath
            foreach ($relativePath in ($files.Keys | Sort-Object)) {
                $destination = Join-Path $scenarioPath ($relativePath -replace '/', '\')
                $parent = Split-Path $destination -Parent
                if (-not (Test-Path -LiteralPath $parent)) {
                    New-Item -ItemType Directory -Path $parent -Force | Out-Null
                }
                [System.IO.File]::WriteAllText($destination, $files[$relativePath], (New-Utf8NoBomEncoding))
            }
        }
        $processed.Add($name)
    } catch {
        Write-Host "FAIL $name -> $($_.Exception.Message)"
        if ($_.ScriptStackTrace) {
            Write-Host $_.ScriptStackTrace
        }
        $failed.Add($name)
    }
}

Write-Host "--- SUMMARY ---"
Write-Host ("processed={0}" -f $processed.Count)
Write-Host ("skipped={0}" -f $skipped.Count)
Write-Host ("failed={0}" -f $failed.Count)
if ($processed.Count -gt 0) { Write-Host ('processed_list=' + ($processed -join ',')) }
if ($skipped.Count -gt 0) { Write-Host ('skipped_list=' + ($skipped -join ',')) }
if ($failed.Count -gt 0) { Write-Host ('failed_list=' + ($failed -join ',')) }
