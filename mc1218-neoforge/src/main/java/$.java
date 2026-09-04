import tech.hakuri.teto.mc1218.NeoEntry;

//这是专门给注入器留的入口点，与 1.20.1 侧的 $.java 一一对应。
//loader 探测到 1.21.x 后加载 client-1218.jar，然后 Class.forName("$") 实例化本类。
//这个类不要移动和改名。
public class $ {
    public $() {
        new NeoEntry();
    }
}
