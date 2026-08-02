param(
    [Parameter(Mandatory = $true)]
    [string] $ServiceName,

    [Parameter(Mandatory = $true)]
    [string] $BasePackage,

    [Parameter(Mandatory = $true)]
    [string] $ServicePackage,

    [Parameter(Mandatory = $true)]
    [string] $OutputPath
)

$ErrorActionPreference = "Stop"

$templateRoot = Split-Path -Parent $PSScriptRoot
$resolvedOutput = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputPath)
$normalizedTemplateRoot = [System.IO.Path]::GetFullPath($templateRoot).TrimEnd([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar)
$normalizedOutput = [System.IO.Path]::GetFullPath($resolvedOutput).TrimEnd([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar)
$templateRootWithSeparator = $normalizedTemplateRoot + [System.IO.Path]::DirectorySeparatorChar

if ($normalizedOutput.Equals($normalizedTemplateRoot, [System.StringComparison]::OrdinalIgnoreCase) -or
    $normalizedOutput.StartsWith($templateRootWithSeparator, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Output path must be outside the template directory: $resolvedOutput"
}

if (Test-Path -LiteralPath $resolvedOutput) {
    throw "Output path already exists: $resolvedOutput"
}

Copy-Item -LiteralPath $templateRoot -Destination $resolvedOutput -Recurse

@("target", ".flattened-pom", ".flattened-pom.xml") | ForEach-Object {
    $generatedPath = Join-Path $resolvedOutput $_
    if (Test-Path -LiteralPath $generatedPath) {
        Remove-Item -LiteralPath $generatedPath -Recurse -Force
    }
}

$javaRoot = Join-Path $resolvedOutput "src\main\java"
$placeholderPackageRoot = Join-Path (Join-Path $javaRoot "com.viettel.bccs") "price"
$basePackagePath = $BasePackage -replace '[\\/\.]', [System.IO.Path]::DirectorySeparatorChar
$servicePackagePath = $ServicePackage -replace '[\\/\.]', [System.IO.Path]::DirectorySeparatorChar
$packageName = (($BasePackage -replace '[\\/]', '.') + "." + ($ServicePackage -replace '[\\/]', '.'))
$renderedPackageRoot = Join-Path $javaRoot (Join-Path $basePackagePath $servicePackagePath)
$sampleMainRoot = Join-Path $javaRoot "com\viettel\bccs\template"
$testRoot = Join-Path $resolvedOutput "src\test\java"
$sampleTestRoot = Join-Path $testRoot "com\viettel\bccs\template"
$renderedTestRoot = Join-Path $testRoot (Join-Path $basePackagePath $servicePackagePath)
$applicationClassName = (($ServiceName -split '[^A-Za-z0-9]+' | Where-Object { $_ }) | ForEach-Object {
    if ($_.Length -eq 1) {
        $_.ToUpperInvariant()
    } else {
        $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1)
    }
}) -join ""
$applicationClassName = $applicationClassName + "Application"

if (Test-Path -LiteralPath $placeholderPackageRoot) {
    New-Item -ItemType Directory -Path (Split-Path -Parent $renderedPackageRoot) -Force | Out-Null
    Move-Item -LiteralPath $placeholderPackageRoot -Destination $renderedPackageRoot
    $placeholderBaseRoot = Join-Path $javaRoot "com.viettel.bccs"
    if (Test-Path -LiteralPath $placeholderBaseRoot) {
        Remove-Item -LiteralPath $placeholderBaseRoot -Recurse -Force
    }
}

if (Test-Path -LiteralPath $sampleMainRoot) {
    New-Item -ItemType Directory -Path $renderedPackageRoot -Force | Out-Null
    Get-ChildItem -LiteralPath $sampleMainRoot -Force | Copy-Item -Destination $renderedPackageRoot -Recurse -Force
    Remove-Item -LiteralPath $sampleMainRoot -Recurse -Force
}

if (Test-Path -LiteralPath $sampleTestRoot) {
    New-Item -ItemType Directory -Path $renderedTestRoot -Force | Out-Null
    Get-ChildItem -LiteralPath $sampleTestRoot -Force | Copy-Item -Destination $renderedTestRoot -Recurse -Force
    Remove-Item -LiteralPath $sampleTestRoot -Recurse -Force
}

$textExtensions = @(".java", ".md", ".xml", ".yml", ".yaml", ".properties", ".txt", ".ps1", ".sh", ".gitkeep")
$files = Get-ChildItem -LiteralPath $resolvedOutput -Recurse -File

foreach ($file in $files) {
    if ($textExtensions -contains $file.Extension) {
        $content = Get-Content -LiteralPath $file.FullName -Raw
        $content = $content.Replace("product-price-service", $ServiceName)
        $content = $content.Replace("com.viettel.bccs", $BasePackage)
        $content = $content.Replace("price", $ServicePackage)
        $content = $content.Replace("product-price-service", $ServiceName)
        $content = $content.Replace("com.viettel.bccs.price", $packageName)
        $content = $content.Replace("ProductPriceServiceApplication", $applicationClassName)
        $content = $content.Replace("ProductPriceServiceApplication", $applicationClassName)
        Set-Content -LiteralPath $file.FullName -Value $content -NoNewline
    }
}

$templateApplicationFiles = @(
    (Join-Path $renderedPackageRoot "ProductPriceServiceApplication.java"),
    (Join-Path $renderedPackageRoot "ProductPriceServiceApplication.java")
)
foreach ($templateApplicationFile in $templateApplicationFiles) {
    if (Test-Path -LiteralPath $templateApplicationFile) {
        Rename-Item -LiteralPath $templateApplicationFile -NewName ($applicationClassName + ".java")
        break
    }
}

Write-Host "Rendered BCCS service template to $resolvedOutput"