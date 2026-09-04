// shared：与 Minecraft 版本无关的客户端主体 —— 47 个功能模块、13 个事件、UI、工具。
//
// 这些文件对 1.20.1 与 1.21.8 是同一份源码，两个版本各编译一遍（不是共用同一份 class：
// 1.20.1 编完还要 reobf 成 srg 名，1.21.8 不用，产物不通用）。
//
// 本模块本身不注册为 Gradle 子项目 —— 它只是一个源码目录，由 client0702 与
// mc1218-neoforge 通过 srcDir 各自纳入编译。这样做的原因：
//   1. 里面的类直接引用 net.minecraft.*，编译需要具体版本的 MC classpath，
//      单独成模块就得自己再配一套 MC 依赖，没有意义；
//   2. 版本专属文件本来就不在这个目录里，所以两边都不需要 exclude ——
//      而 Gradle 的 exclude 作用于整个 SourceDirectorySet、不区分 srcDir 来源，
//      早先靠 exclude 区分的做法会把版本专属副本一起排掉。
//
// 需要版本专属实现的差异，统一收在各版本自己的 tech.hakuri.teto.compat 包下，
// 共享代码只调 compat 的静态方法。
