import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

/**
 * 冒烟测试代理：验证「动态 attach 的 java agent 能否重转换已加载的类」。
 * 这是客户端从 hook.dll（JVMTI）迁移到 Instrumentation 后所依赖的唯一核心能力。
 * agentArgs 传入替换用的 .class 文件路径。
 */
public final class RetransformAgent {

    private RetransformAgent() {
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        System.out.println("[agent] isRetransformClassesSupported = " + inst.isRetransformClassesSupported());

        byte[] replacement = Files.readAllBytes(Paths.get(agentArgs));
        System.out.println("[agent] 替换字节码 " + replacement.length + " 字节：" + agentArgs);

        Class<?> target = null;
        for (Class<?> loaded : inst.getAllLoadedClasses()) {
            if ("RetransformProbe".equals(loaded.getName())) {
                target = loaded;
                break;
            }
        }
        if (target == null) {
            throw new IllegalStateException("目标进程中没有已加载的 RetransformProbe");
        }
        System.out.println("[agent] 找到目标类，isModifiableClass = " + inst.isModifiableClass(target));

        Class<?> captured = target;
        ClassFileTransformer transformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                // 与 a.a(...) 完全相同的分发方式：按 classBeingRedefined 的身份判断
                if (classBeingRedefined == captured) {
                    System.out.println("[agent] transform 命中：" + className);
                    return replacement;
                }
                return null; // 不修改，JVM 连重新解析都省了
            }
        };

        inst.addTransformer(transformer, true);
        try {
            inst.retransformClasses(captured);
            System.out.println("[agent] retransformClasses 成功");
        } finally {
            inst.removeTransformer(transformer);
        }
    }
}
