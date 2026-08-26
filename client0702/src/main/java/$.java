import client.ForgeEntry;

//这是专门给注入器留的入口点
//不用特地掩盖此类和ForgeEntry，主要掩盖ClientEntry就行
//这个类不要移动和改名，也不要混类名
public class $ {
    public $() {
        new ForgeEntry();
    }
}
