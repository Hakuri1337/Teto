# Minecraft 1.20.1 Forge 客户端加载基础工程

> 本仓库是一个 Minecraft 1.20.1 / Forge 47.3.0 客户端基础工程，包含客户端 JAR、Java loader 和 JVMTI redefiner。请只在自己拥有或已经明确获得许可的 Minecraft 实例、测试环境和服务器中使用。

## 1. 这次整理的结果

本次变更完成了以下事项：

- 保留 `loader/src/$.java` 作为 loader：查找 Minecraft 的 `Render thread`，读取 `Jar` 指向的客户端 JAR，并把其中的 class 定义到 Minecraft 的目标 `ClassLoader`。
- `scripts/setup-env.ps1` 和 `scripts/setup-env.bat`：自动把三个构建产物复制部署到 `%LOCALAPPDATA%\Teto`，并将环境变量设置为部署后的路径。
- 新增 `injector/` 运行时注入器：通过 Attach API 附加到运行中的 Minecraft JVM，以 agent 形式把 `loader/$.class` 定义并实例化进游戏，实现运行时热注入。
- `client.ClientEntry`、loader 与注入器只从两个位置读取资源：相同目录（当前工作目录）和 `%LOCALAPPDATA%\Teto`（AppData\Local\Teto）；客户端统一命名为 `client.jar`。
- 明确 `hook.dll` 是 `redefiner` 的 x64 Release 构建产物；工程文件已将 x64 Release 目标名设为 `hook.dll`。
- 移除不再使用的 `loader_windows_x64.dll` 初始化类和未被引用的 HWID 工具类。
- 清理源码和验证副本中的硬编码账号访问令牌；当前源码不再包含账号登录、License、HWID、RSA 解密、在线过期时间或硬编码 `accessToken`。

## 2. 运行链路

```text
用户运行注入器 teto-injector.jar
        │
        ▼ Attach API（附加到运行中的 Minecraft JVM）
Agent.agentmain(...)
        │ 读取 $.class（相同目录 / AppData\Local\Teto）
        ▼
定义并实例化 loader/$.class
        │
        ▼
loader/$ 查找 Render thread 的 ContextClassLoader
        │ 读取 Jar
        ▼
随机尝试定义客户端 JAR 中的 class
        │
        ▼
执行客户端 JAR 中的顶层 $
        │
        ▼
client.ForgeEntry -> ClientEntry.init(null)
        │
        ├─ 注册模块、排序、启用 HUD
        ├─ System.load(HookDll)
        └─ 调用 a.a(...) 触发 JVMTI redefiner
```

注入器不修改游戏进程的启动方式：游戏照常由启动器运行，注入器在游戏已经启动后附加进去，把 loader 热加载到目标 JVM 内完成后续注入。所有资源只从两个位置读取：相同目录（当前工作目录）优先，`%LOCALAPPDATA%\Teto`（即 AppData\Local\Teto）其次。

## 3. 目录结构

```text
Teto/
├─ client0702/                         Forge 1.20.1 客户端工程
│  ├─ src/main/java/$\.java            客户端顶层入口
│  ├─ src/main/java/client/ForgeEntry.java
│  ├─ src/main/java/client/ClientEntry.java
│  ├─ build/libs/client0702.jar        构建后客户端 JAR
│  └─ verification/                    验证、差异和回滚记录
├─ loader/
│  └─ src/
│     ├─ $.java                        读取 JAR 并定义客户端 class 的 loader
│     └─ compile.bat                    编译 loader class
├─ injector/
│  ├─ src/
│  │  ├─ Injector.java                  用户注入器入口（Attach API）
│  │  ├─ Agent.java                     agentmain：定义并实例化 loader/$.class
│  │  ├─ MANIFEST.MF                    Main-Class / Agent-Class 清单
│  │  └─ compile.bat                    编译并打包 teto-injector.jar
│  ├─ run.bat                           用户双击入口
│  ├─ build/teto-injector.jar           注入器产物
│  └─ test/Target.java                  热注入冒烟测试目标
├─ redefiner/
│  ├─ dllmain.cpp                       JVMTI ClassFileLoadHook 实现
│  └─ redefiner.vcxproj                 Visual Studio 工程
├─ scripts/
│  ├─ setup-env.ps1                     环境变量部署脚本
│  └─ setup-env.bat                     PowerShell 包装脚本
│  ├─ install-forge-client.ps1          用户侧 Forge 安装加载器
│  └─ install-forge-client.bat          安装加载器批处理包装器
└─ README.md
```

## 4. 资源目录与读取位置

所有资源（客户端 `client.jar`、loader `$.class`、`hook.dll`、配置、注入器）只从两个位置读取：

| 位置 | 说明 |
|---|---|
| 相同目录 | 进程当前工作目录。游戏内组件为游戏进程的工作目录；把资源与游戏/注入器放在同一目录即可便携使用 |
| `%LOCALAPPDATA%\Teto` | 标准部署目录，即 `AppData\Local\Teto`；部署脚本默认写入这里 |

查找顺序：相同目录优先，`%LOCALAPPDATA%\Teto` 其次。不再读取环境变量，也不再回退到 `C:\幻影`。

资源文件统一命名：

| 资源 | 文件名 |
|---|---|
| 客户端 | `client.jar` |
| loader | `$.class` |
| JVMTI redefiner | `hook.dll` |
| 配置 | `config\` 目录下 |

配置保存始终写入 `%LOCALAPPDATA%\Teto\config\`；读取时先看相同目录的 `config\`，再看 `%LOCALAPPDATA%\Teto\config\`。所有文件路径都会在使用前规范化并检查是否为普通文件。

## 5. 前置环境

### 5.1 Java 和 Gradle

- Windows x64。
- JDK 17；本工程已使用 `java.toolchain` 指定 Java 17。
- Gradle 8.x。当前已验证 Gradle 8.10.2。
- ForgeGradle 6.0.35 与 Minecraft 1.20.1 / Forge 47.3.0。
- 不建议使用 Gradle 9.x 构建这个工程。

本工作区验证过的工具路径为：

```text
C:\Users\27881\Desktop\Teto\tools\jdk-17.0.20.1+1
C:\Users\27881\Desktop\Teto\tools\gradle-8.10.2\bin\gradle.bat
```

如果你的路径不同，把下面命令中的路径替换为自己的实际路径。

### 5.2 C++ 构建环境

构建 `redefiner` 需要：

- Visual Studio 2022。
- “使用 C++ 的桌面开发”工作负载。
- MSVC v143 工具集。
- Windows 10/11 SDK。
- 与 Minecraft JVM 位数相同的 x64 构建环境。

不要使用 `Win32` 构建去配合 64 位 Java；最终应使用 `Release|x64`。

## 6. 构建顺序

建议严格按以下顺序：

1. 构建 `client0702.jar`。
2. 编译 `loader/$.class`。
3. 构建 `redefiner` 的 `hook.dll`。
4. 编译并打包注入器 `teto-injector.jar`。
5. 运行环境变量部署脚本。
6. 游戏运行后，用注入器热加载 loader `$`。

### 6.1 构建 Forge 客户端

在 PowerShell 中执行：

```powershell
Set-Location 'C:\Users\27881\Desktop\Teto\client0702'
$env:JAVA_HOME='C:\Users\27881\Desktop\Teto\tools\jdk-17.0.20.1+1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\27881\Desktop\Teto\tools\gradle-8.10.2\bin\gradle.bat' clean build --no-daemon
```

成功标志：

```text
BUILD SUCCESSFUL
```

主要产物：

```text
C:\Users\27881\Desktop\Teto\client0702\build\libs\client0702.jar
```

如果只需要增量编译，可使用：

```powershell
& 'C:\Users\27881\Desktop\Teto\tools\gradle-8.10.2\bin\gradle.bat' build --no-daemon
```

### 6.2 编译 loader

推荐使用项目提供的脚本：

```powershell
Set-Location 'C:\Users\27881\Desktop\Teto\loader\src'
.\compile.bat
```

或手动使用 JDK 17：

```powershell
Set-Location 'C:\Users\27881\Desktop\Teto'
$env:JAVA_HOME='C:\Users\27881\Desktop\Teto\tools\jdk-17.0.20.1+1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
New-Item -ItemType Directory -Force '.\loader\build' | Out-Null
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d '.\loader\build' '.\loader\src\$.java'
```

成功后应至少存在：

```text
C:\Users\27881\Desktop\Teto\loader\build\$.class
```

`$.class` 是 loader 主类，负责在目标 Minecraft JVM 中查找目标 ClassLoader 并加载客户端 JAR。

### 6.3 构建 redefiner / hook.dll

从“Developer PowerShell for VS 2022”打开项目目录，执行：

```powershell
Set-Location 'C:\Users\27881\Desktop\Teto\redefiner'
msbuild .\redefiner.sln /m /p:Configuration=Release /p:Platform=x64
```

或者构建工程文件：

```powershell
msbuild .\redefiner.vcxproj /m /p:Configuration=Release /p:Platform=x64
```

工程已把 `Release|x64` 的目标名设置为 `hook`，因此预期产物为：

```text
C:\Users\27881\Desktop\Teto\redefiner\x64\Release\hook.dll
```

如果你的 Visual Studio 输出目录配置被覆盖，请把最终 DLL 复制或重命名为 `hook.dll`，并在部署脚本中通过 `-HookDll` 指定它的绝对路径。不要再准备或引用 `loader_windows_x64.dll`。

### 6.4 构建注入器

```powershell
Set-Location 'C:\Users\27881\Desktop\Teto'
$env:JAVA_HOME='C:\Users\27881\Desktop\Teto\tools\jdk-17.0.20.1+1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\injector\src\compile.bat
```

产物：

```text
C:\Users\27881\Desktop\Teto\injector\build\teto-injector.jar
```

该 JAR 同时是注入器主程序（`Main-Class: Injector`）和 agent（`Agent-Class: Agent`），附加时由 `loadAgent` 直接复用。

## 7. 自动部署

### 7.1 当前 PowerShell 进程生效

在三个产物都生成后，从项目根目录执行：

```powershell
.\scripts\setup-env.ps1
```

脚本会验证构建产物存在，创建 `%LOCALAPPDATA%\Teto`，并复制为：

```text
%LOCALAPPDATA%\Teto\client.jar
%LOCALAPPDATA%\Teto\$.class
%LOCALAPPDATA%\Teto\hook.dll
%LOCALAPPDATA%\Teto\teto-injector.jar
%LOCALAPPDATA%\Teto\run-injector.bat
```

如果 `injector\build\teto-injector.jar` 尚未构建，脚本会跳过注入器部署并给出提示。

然后在当前 PowerShell 进程设置：

```text
Jar=...
LoaderClass=...
HookDll=...
```

验证：

```powershell
Get-ChildItem Env:Jar,Env:LoaderClass,Env:HookDll
```

脚本修改的是当前脚本进程及其后续子进程；它不会改变已经打开的其他 PowerShell、启动器或 Minecraft 进程。

### 7.2 持久化到当前用户

```powershell
.\scripts\setup-env.ps1 -Persist
```

也可以显式指定路径：

```powershell
.\scripts\setup-env.ps1 `
  -ProjectRoot 'C:\Users\27881\Desktop\Teto' `
  -ClientJar 'C:\Users\27881\Desktop\Teto\client0702\build\libs\client0702.jar' `
  -LoaderClass 'C:\Users\27881\Desktop\Teto\loader\build\$.class' `
  -HookDll 'C:\Users\27881\Desktop\Teto\redefiner\x64\Release\hook.dll' `
  -DeployDirectory "$env:LOCALAPPDATA\Teto" `
  -Persist
```

批处理包装器用法：

```bat
C:\Users\27881\Desktop\Teto\scripts\setup-env.bat -Persist
```

使用 `-Persist` 后，需要重新启动 Minecraft 或启动器，让新进程继承更新后的用户环境变量。

### 7.3 手动设置

如果不使用脚本，可以在同一个启动 PowerShell 中执行：

```powershell
$env:Jar = "$env:LOCALAPPDATA\Teto\client.jar"
$env:LoaderClass = "$env:LOCALAPPDATA\Teto\$.class"
$env:HookDll = "$env:LOCALAPPDATA\Teto\hook.dll"
```

不要用 `setx` 代替当前进程设置：`setx` 通常只影响之后新开的进程，当前窗口和已经运行的 Minecraft 不会自动获得新值。

## 8. 使用方式

### 8.1 运行时热注入（注入器）

这是项目的主要用法：游戏照常由启动器启动，进入主菜单后运行注入器，它会把 `loader/$.class` 热加载进运行中的 Minecraft JVM，无需重启游戏。

先构建并部署（见第 6、7 节），然后启动 Minecraft 1.20.1 / Forge 47.3.0，进入主菜单后执行：

```bat
C:\Users\27881\Desktop\Teto\injector\run.bat
```

部署目录中也有一份可直接双击的入口：

```text
%LOCALAPPDATA%\Teto\run-injector.bat
```

注入器会自动检测运行中的 Minecraft JVM：只有一个候选时直接注入，多个候选时列出编号让用户选择。也可以显式指定进程：

```bat
run.bat --pid 12345
run.bat --list
```

`--list` 只列出当前所有 Java 进程（第一列是进程 ID），用于确认目标。

热注入前提：

- 游戏必须已经启动并完成初始化（主菜单已出现），此时 `Render thread` 才存在。
- 资源已部署到 `%LOCALAPPDATA%\Teto`（`client.jar`、`$.class`、`hook.dll`），或放在游戏进程的当前工作目录（相同目录）。
- 注入器与游戏由同一个 Windows 用户运行。

### 8.2 备用方案：安装为 Forge mod

如果不使用热注入，也可以把客户端 JAR 安装到目标 Forge 实例的 `mods` 目录。安装加载器会先部署 `client.jar`、`$.class`、`hook.dll`，随后把已部署的 `client.jar` 复制为目标实例中的 `mods\Teto-1.20.1.jar`。

默认游戏目录：

```powershell
.\scripts\install-forge-client.ps1 -Persist
```

自定义实例目录：

```powershell
.\scripts\install-forge-client.ps1 `
  -MinecraftDirectory 'D:\Minecraft\Instances\Forge-1.20.1' `
  -Persist
```

也可直接双击/执行批处理包装器：

```bat
C:\Users\27881\Desktop\Teto\scripts\install-forge-client.bat -Persist
```

之后在启动器中选择 Minecraft 1.20.1 / Forge 47.3.0，并使用该实例目录启动。Forge 会发现 `Teto-1.20.1.jar` 中的 `ForgeEntry`，并完成客户端初始化。

### 8.3 先确认配置

在目标 Minecraft JVM 使用的同一进程环境中确认：

```powershell
Test-Path $env:Jar
Test-Path $env:LoaderClass
Test-Path $env:HookDll
```

三个命令都应输出 `True`。

### 8.4 注入后的实际顺序

1. 注入器通过 Attach API 附加到目标 Minecraft JVM，`loadAgent` 载入 `teto-injector.jar`。
2. agent 按顺序解析 `$.class`（注入器显式传入路径 / 相同目录 / `AppData\Local\Teto`）。
3. agent 用隔离 ClassLoader 定义并实例化 loader `$`。
4. loader 查找名为 `Render thread` 的 Minecraft 线程并取得 ContextClassLoader。
5. loader 解析并校验客户端 JAR。
6. loader 读取 `Jar` 中全部非 `module-info.class` 的 `.class` 文件，在一秒随机尝试窗口内定义 class。
7. loader 调用客户端 JAR 中的顶层 `$`，进入 `ForgeEntry` 和 `ClientEntry`。
8. `ClientEntry` 注册模块、启用 HUD、加载 `HookDll`，并调用 JVMTI 重转换入口。

### 8.5 日志和失败定位

注入器失败时向控制台输出 `[injector]` 前缀；agent 和 loader 运行在游戏进程内，其输出会进入游戏日志（启动器日志/`latest.log`），分别带 `[agent]` 和 `[loader]` 前缀。

常见错误：

- `[injector] 未找到 loader/$.class`：相同目录与 `%LOCALAPPDATA%\Teto` 均不存在有效的 `$.class`。
- `[injector] 无法定位注入器 JAR 路径`：注入器没有以 JAR 形式运行，应使用 `run.bat` 或 `java -jar`。
- `[injector] 未检测到运行中的 Java 进程`：没有找到游戏进程；先启动游戏进入主菜单，或用 `--pid` 指定进程 ID。
- `[injector] 不能附加到当前 JVM（注入器自身）`：`--pid` 指向了注入器自己，请改为游戏进程 ID。
- `AttachNotSupportedException`：目标不是可附加的 JVM，或与游戏进程用户不一致（管理员权限问题）。
- `[agent] 无法加载 loader/$.class`：agent 已载入但 loader 定义失败，检查 `$.class` 是否完整。
- `[loader] 未找到 Minecraft 的 Render thread`：注入太早，游戏尚未创建渲染线程，等主菜单出现后重试。
- `[loader] 未找到 Jar 资源`：相同目录与 `%LOCALAPPDATA%\Teto` 均不存在有效的 `client.jar`。
- `Jar 中没有可加载的 .class`：给到的文件不是有效客户端 JAR。
- `有 N 个 class 未在超时窗口内定义成功`：目标 JAR 或运行时映射与当前游戏不匹配，也可能是依赖定义顺序不适合当前环境。
- `[loader] 未找到资源 hook.dll`：相同目录与 `%LOCALAPPDATA%\Teto` 均不存在有效的 `hook.dll`。
- `Can't load IA 32-bit .dll on a AMD 64-bit platform`：DLL 位数错误，应重新构建 `Release|x64`。
- `UnsatisfiedLinkError`：JDK、DLL 位数、导出符号或目标 JVM 不匹配。

没有游戏环境时，可以用 `injector/test/Target.java` 冒烟验证热注入链路：编译并运行 `Target`（普通 JVM），再对它的 PID 执行注入器。日志中应出现 `[agent] 已实例化 loader/$.class`，随后 `[loader]` 因测试 JVM 没有 `Render thread` 报错，这属于预期结果。

## 9. 版本和兼容性要求

- 客户端工程按 Minecraft 1.20.1、Forge 47.3.0 和官方 mappings 构建。
- loader 使用 Java 17 编译；不要用高于目标 JVM 的 class 文件版本。
- `hook.dll` 必须使用与目标 Java/Minecraft 进程相同的 x64 架构。
- loader 依赖目标进程中存在名称为 `Render thread` 的线程，并依赖其 ContextClassLoader。
- 客户端 JAR 必须是当前源码和当前 Forge/Minecraft 映射生成的版本，不能随意混用其他版本产物。

## 10. 清理、重新构建和回滚

### 10.1 清理构建产物

```powershell
Set-Location 'C:\Users\27881\Desktop\Teto\client0702'
$env:JAVA_HOME='C:\Users\27881\Desktop\Teto\tools\jdk-17.0.20.1+1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\27881\Desktop\Teto\tools\gradle-8.10.2\bin\gradle.bat' clean

Set-Location 'C:\Users\27881\Desktop\Teto'
[IO.Directory]::Delete('.\loader\build', $true) # 仅删除 loader 构建目录
```

### 10.2 验证回滚脚本

上一阶段和本次验证 artifacts 位于：

```text
C:\Users\27881\Desktop\Teto\client0702\verification
```

其中：

- `MODIFIED_FILE.java`：修改后的 `ClientEntry.java` 快照。
- `DIFF_FILE.patch`：变更差异。
- `VERIFICATION.txt`：基线、修改版和回滚测试记录。
- `ROLLBACK.sh`：将测试副本恢复到记录的基线。

回滚脚本只应对验证副本或明确指定的测试目录使用；执行后再把修改版恢复到实际项目，确保交付状态仍是修改态。

## 11. 账号令牌和登录验证清理说明

当前 `client0702/src/main/java` 已移除：

- `登录正版账号()` 调用和实现。
- RSA 公钥、模数和 `License` 环境变量读取。
- HWID 读取、剪贴板写入、`setx HWID` 和在线时间请求。
- 机器码比对、过期时间判断和失败退出线程。
- 硬编码账号访问令牌。
- `loader_windows_x64.dll` 和 `C:\幻影\data.dat` 的旧初始化逻辑。

运行时认证行为由 Minecraft/Forge 本身以及启动器提供；本工程不再修改 `User.accessToken`，也不提供新的账号登录实现。

## 12. 产物命名的最终约定

| 产物 | 来源 | 当前状态 |
|---|---|---|
| `client0702.jar` | `client0702` Gradle `build` | 构建源产物；部署时重命名为 `client.jar` |
| `$.class` | `loader/src/$.java` | 使用中 |
| `teto-injector.jar` | `injector/src/compile.bat` | 使用中；运行时热注入入口 |
| `hook.dll` | `redefiner` `Release|x64` | 使用中 |
| `loader_windows_x64.dll` | 旧混淆/加密 loader | 已废弃，不再需要 |

## 13. 兼容性报告

### 保留

- 原有 `ForgeEntry -> ClientEntry.init(null)` 初始化入口。
- 系统属性设置、初始化延迟、字体初始化、模块注册、模块排序和 HUD 启用。
- `a.a(...)` 的 JVMTI 重转换调用顺序。
- loader 的“随机尝试定义 class，再执行客户端 `$`”核心加载顺序。
- 客户端 JAR 内 class 的注入目标仍为 Minecraft `Render thread` 的 ContextClassLoader。
- `ForgeEntry -> ClientEntry.init(null)` 初始化入口在 mods 安装方案中继续可用。

### 更改

- loader 增加了路径校验、资源自动关闭和可诊断的错误输出。
- 所有运行时资源只从相同目录（当前工作目录）与 `%LOCALAPPDATA%\Teto`（AppData\Local\Teto）两处读取；配置保存统一落在 `%LOCALAPPDATA%\Teto\config`。
- redefiner x64 Release 目标名设为 `hook.dll`。
- 新增自动环境变量部署脚本和完整构建说明。
- 新增 `injector/` 运行时注入器：Attach API 附加运行中的游戏进程，agent 定义并实例化 `loader/$.class`，取代原先需要在 JVM 内预先存在的 Java 执行入口。

### 移除

- 账号登录验证链及其所有授权判断。
- 硬编码账号访问令牌。
- 未被引用的 HWID 工具类。
- 旧的 `loader_windows_x64.dll`/`data.dat` 初始化类。
- `loader/src/Payload.java`：其职责已由注入器 `Agent.agentmain` 承接。

### 未实现

- 注入器使用 JDK 自带的 Attach API（`jdk.attach` 模块），不包含原生进程注入或提权逻辑；游戏必须由同一 Windows 用户启动。
- 未改变客户端各功能模块本身的游戏逻辑。

