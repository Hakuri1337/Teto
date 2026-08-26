package tech.hakuri.teto.feature.impl.render;


import tech.hakuri.teto.event.impl.EventChamsAfter;
import tech.hakuri.teto.event.impl.EventChamsPre;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import com.mojang.blaze3d.systems.RenderSystem;

//thanks liquidbounce
//布吉岛的光影mod和entity culling mod把这个chams干废了，后者已解决，前者的覆盖点我还在找
public class Chams extends Module {
    public static boolean state;//我不知道这个是拿来干嘛的，但是水影也干了

    public Chams() {
        name = "贴图透视";
        category = Category.render;
    }

    @Override
    public void onChamsPre(EventChamsPre event) {
        if (!state) {
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(1f, -1000000);
            state = true;
        }
    }

    @Override
    public void onChamsAfter(EventChamsAfter event) {
        if (state) {
            RenderSystem.polygonOffset(1f, 1000000);//这个1000000是试出来的值，尽量不要动，不然背对着你的贴图会画在最上面
            RenderSystem.disablePolygonOffset();
            state = false;
        }
    }
}