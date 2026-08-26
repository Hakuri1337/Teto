[CmdletBinding()]
param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$MinecraftDirectory,
    [string]$ModsDirectory,
    [string]$DeployDirectory,
    [switch]$Persist
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-DefaultMinecraftDirectory {
    if ([string]::IsNullOrWhiteSpace($env:APPDATA)) {
        throw '无法确定 APPDATA；请通过 -MinecraftDirectory 指定游戏目录。'
    }
    return Join-Path $env:APPDATA '.minecraft'
}

function Resolve-Directory([string]$Value, [string]$Name) {
    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "$Name 路径为空。"
    }
    return [IO.Path]::GetFullPath($Value)
}

$ProjectRoot = Resolve-Directory $ProjectRoot 'ProjectRoot'
if (-not [IO.Directory]::Exists($ProjectRoot)) {
    throw "项目根目录不存在：$ProjectRoot"
}

if ([string]::IsNullOrWhiteSpace($MinecraftDirectory)) {
    $MinecraftDirectory = Get-DefaultMinecraftDirectory
}
$MinecraftDirectory = Resolve-Directory $MinecraftDirectory 'MinecraftDirectory'

if ([string]::IsNullOrWhiteSpace($ModsDirectory)) {
    $ModsDirectory = Join-Path $MinecraftDirectory 'mods'
}
$ModsDirectory = Resolve-Directory $ModsDirectory 'ModsDirectory'

$setupScript = Join-Path $PSScriptRoot 'setup-env.ps1'
$setupArguments = @{
    ProjectRoot = $ProjectRoot
}
if (-not [string]::IsNullOrWhiteSpace($DeployDirectory)) {
    $setupArguments.DeployDirectory = $DeployDirectory
}
if ($Persist) {
    $setupArguments.Persist = $true
}

& $setupScript @setupArguments

$installedClient = Join-Path $ModsDirectory 'Teto-1.20.1.jar'
[IO.Directory]::CreateDirectory($ModsDirectory) | Out-Null
Copy-Item -LiteralPath $env:Jar -Destination $installedClient -Force

Write-Host ("Forge 客户端已安装：{0}" -f $installedClient)
Write-Host ("资源部署目录：{0}" -f (Split-Path -Parent $env:Jar))
Write-Host '请在启动器中选择 Minecraft 1.20.1 / Forge 47.3.0，并使用此游戏目录启动。'
