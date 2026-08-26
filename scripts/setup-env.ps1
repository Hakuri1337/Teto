[CmdletBinding()]
param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$ClientJar,
    [string]$LoaderClass,
    [string]$HookDll,
    [string]$InjectorJar,
    [string]$DeployDirectory,
    [switch]$Persist
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Resolve-File([string]$Value, [string]$Name) {
    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "$Name 路径为空。"
    }
    $full = [IO.Path]::GetFullPath($Value)
    if (-not [IO.File]::Exists($full)) {
        throw "$Name 文件不存在：$full"
    }
    return $full
}

function Get-DefaultDeployDirectory {
    if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        return Join-Path $env:LOCALAPPDATA 'Teto'
    }
    if (-not [string]::IsNullOrWhiteSpace($env:USERPROFILE)) {
        return Join-Path $env:USERPROFILE 'AppData\Local\Teto'
    }
    throw '无法确定 LOCALAPPDATA 或 USERPROFILE，不能确定部署目录。'
}

$ProjectRoot = [IO.Path]::GetFullPath($ProjectRoot)
if (-not [IO.Directory]::Exists($ProjectRoot)) {
    throw "项目根目录不存在：$ProjectRoot"
}

if ([string]::IsNullOrWhiteSpace($ClientJar)) {
    $ClientJar = Join-Path $ProjectRoot 'client0702\build\libs\client0702.jar'
}
if ([string]::IsNullOrWhiteSpace($LoaderClass)) {
    $LoaderClass = Join-Path $ProjectRoot 'loader\build\$.class'
}
if ([string]::IsNullOrWhiteSpace($HookDll)) {
    $HookDll = Join-Path $ProjectRoot 'redefiner\x64\Release\hook.dll'
}
if ([string]::IsNullOrWhiteSpace($InjectorJar)) {
    $InjectorJar = Join-Path $ProjectRoot 'injector\build\teto-injector.jar'
}

if ([string]::IsNullOrWhiteSpace($DeployDirectory)) {
    $DeployDirectory = Get-DefaultDeployDirectory
}
$DeployDirectory = [IO.Path]::GetFullPath($DeployDirectory)

$sources = [ordered]@{
    ClientJar = Resolve-File $ClientJar 'ClientJar'
    LoaderClass = Resolve-File $LoaderClass 'LoaderClass'
    HookDll = Resolve-File $HookDll 'HookDll'
}

[IO.Directory]::CreateDirectory($DeployDirectory) | Out-Null
$deployments = [ordered]@{
    Jar = Join-Path $DeployDirectory 'client.jar'
    LoaderClass = Join-Path $DeployDirectory '$.class'
    HookDll = Join-Path $DeployDirectory 'hook.dll'
}

Copy-Item -LiteralPath $sources.ClientJar -Destination $deployments.Jar -Force
Copy-Item -LiteralPath $sources.LoaderClass -Destination $deployments.LoaderClass -Force
Copy-Item -LiteralPath $sources.HookDll -Destination $deployments.HookDll -Force

if ([IO.File]::Exists($InjectorJar)) {
    $deployedInjector = Join-Path $DeployDirectory 'teto-injector.jar'
    Copy-Item -LiteralPath $InjectorJar -Destination $deployedInjector -Force
    $runner = Join-Path $DeployDirectory 'run-injector.bat'
    @'
@echo off
setlocal
chcp 65001 >nul
set "JAVA_CMD=java"
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVA_CMD%" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 --add-modules jdk.attach -jar "%~dp0teto-injector.jar" %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
'@ | Set-Content -LiteralPath $runner -Encoding ASCII
    Write-Host ("注入器已部署：{0}" -f $deployedInjector)
    Write-Host ("双击 {0} 即可对运行中的游戏热注入" -f $runner)
} else {
    Write-Host "未找到注入器 JAR（$InjectorJar），跳过注入器部署。"
}

$values = [ordered]@{
    Jar = $deployments.Jar
    LoaderClass = $deployments.LoaderClass
    HookDll = $deployments.HookDll
}

foreach ($entry in $values.GetEnumerator()) {
    [Environment]::SetEnvironmentVariable($entry.Key, $entry.Value, [EnvironmentVariableTarget]::Process)
    if ($Persist) {
        [Environment]::SetEnvironmentVariable($entry.Key, $entry.Value, [EnvironmentVariableTarget]::User)
    }
}

Write-Host ("资源已部署到：{0}" -f $DeployDirectory)
Write-Host '环境变量已设置为部署后的资源：'
foreach ($entry in $values.GetEnumerator()) {
    Write-Host ("{0}={1}" -f $entry.Key, $entry.Value)
}
if ($Persist) {
    Write-Host '持久化范围：当前用户；当前 PowerShell 进程也已立即生效。'
} else {
    Write-Host '生效范围：当前 PowerShell 进程。需要持久化时追加 -Persist。'
}
