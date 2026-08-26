package client;

import net.minecraftforge.fml.common.Mod;

//为了在idea内正常调试，该类一定要使用@Mod注解，发布时去掉也能正常工作但是麻烦，不过该类没有核心加载逻辑，不用特地掩盖
//如果保留这个注解，用户可以将客户端当一个普通mod放入mods加载
@Mod("client")
//这里继承一下Thread，使得其运行的任何代码都不会阻塞渲染线程（是的，mc渲染跟工作是一个线程）
public class ForgeEntry extends Thread {//好吧，我就是要多线程加载

    public ForgeEntry() {//forge会自动调用@Mod里的类的构造方法
        start();
    }

    @Override
    public void run() {//好吧，我就是要多线程加载
        ClientEntry.init(null);//这里注意一个小细节，virbox似乎是不会混构造方法的，所以我们只能弄成一个普通方法
    }
}