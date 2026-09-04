import com.sun.tools.attach.VirtualMachine;

/**
 * 冒烟测试用的最小附加器：把 retransform-agent.jar 附加到指定 pid。
 * 用法：java --add-modules jdk.attach -cp . RetransformAttach <pid> <agent.jar> <替换用的.class>
 */
public final class RetransformAttach {

    private RetransformAttach() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("用法：RetransformAttach <pid> <agent.jar> <替换用的.class>");
            System.exit(2);
        }
        VirtualMachine vm = VirtualMachine.attach(args[0]);
        try {
            vm.loadAgent(args[1], args[2]);
            System.out.println("[attach] 已向 JVM " + args[0] + " 载入 agent");
        } finally {
            vm.detach();
        }
    }
}
