[CmdletBinding()]
param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$ClientJar,
    [string]$ClientJar1218,
    [string]$LoaderClass,
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
    $ClientJar = Join-Path $ProjectRoot 'client0702\build\libs\client-1201.jar'
}
if ([string]::IsNullOrWhiteSpace($ClientJar1218)) {
    $ClientJar1218 = Join-Path $ProjectRoot 'mc1218-neoforge\build\libs\client-1218.jar'
}
if ([string]::IsNullOrWhiteSpace($LoaderClass)) {
    $LoaderClass = Join-Path $ProjectRoot 'loader\build\$.class'
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
}

[IO.Directory]::CreateDirectory($DeployDirectory) | Out-Null
$deployments = [ordered]@{
    Jar = Join-Path $DeployDirectory 'client-1201.jar'
    LoaderClass = Join-Path $DeployDirectory '$.class'
}

Copy-Item -LiteralPath $sources.ClientJar -Destination $deployments.Jar -Force
Copy-Item -LiteralPath $sources.LoaderClass -Destination $deployments.LoaderClass -Force

# 1.21.8 版本可选：还没构建时跳过，loader 会在 1.20.1 上照常工作
if ([IO.File]::Exists($ClientJar1218)) {
    $deployed1218 = Join-Path $DeployDirectory 'client-1218.jar'
    Copy-Item -LiteralPath $ClientJar1218 -Destination $deployed1218 -Force
    Write-Host ("1.21.8 客户端已部署：{0}" -f $deployed1218)
} else {
    Write-Host "未找到 client-1218.jar，跳过 1.21.8 部署（构建 :mc1218-neoforge:jar 后重跑本脚本）。"
}

if ([IO.File]::Exists($InjectorJar)) {
    $deployedInjector = Join-Path $DeployDirectory 'teto-injector.jar'
    # 旧版注入器把自己作为 agent jar 交给 loadAgent，目标 JVM 会一直内存映射它，
    # 于是被注入过的游戏还在跑时这里必然复制失败。新版已改用临时副本，但历史遗留的
    # 映射只能等 Windows 释放。这种情况下不要让整个部署失败 —— 写一个 .new.jar 并提示。
    $copied = $true
    try {
        Copy-Item -LiteralPath $InjectorJar -Destination $deployedInjector -Force -ErrorAction Stop
    } catch {
        $copied = $false
        $pending = Join-Path $DeployDirectory 'teto-injector.new.jar'
        Copy-Item -LiteralPath $InjectorJar -Destination $pending -Force
        Write-Host "注入器 JAR 正被占用（通常是之前注入过的游戏仍在运行，或映射尚未释放）。"
        Write-Host ("已改写到：{0}" -f $pending)
        Write-Host "关掉相关进程后，把它改名为 teto-injector.jar 即可；或直接重跑本脚本。"
    }
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
    if ($copied) {
        Write-Host ("注入器已部署：{0}" -f $deployedInjector)
    }
    Write-Host ("双击 {0} 即可对运行中的游戏热注入" -f $runner)
} else {
    Write-Host "未找到注入器 JAR（$InjectorJar），跳过注入器部署。"
}

$values = [ordered]@{
    Jar = $deployments.Jar
    LoaderClass = $deployments.LoaderClass
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
