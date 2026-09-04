package tech.hakuri.teto.feature;


import tech.hakuri.teto.event.impl.*;
import tech.hakuri.teto.i18n.I18n;
import net.minecraft.client.Minecraft;

import java.util.LinkedList;
import java.util.List;

public class Module {
    public Minecraft mc = Minecraft.getInstance();
    /**
     * 模块的<b>身份</b>：永远是构造器里写的中文字面值（也是配置里存的名字）。
     * 切换语言<b>不会</b>改变它 —— 显示用 {@link #displayName()}。
     */
    public String name;
    public String category;
    public List<Value> values = new LinkedList<>();
    public boolean enable;
    public int keyCode;

    /** 当前语言下的显示名（HUD / ClickGUI 用）。身份仍然是 {@link #name}。 */
    public String displayName() {
        return I18n.module(name);
    }

    /** 当前语言下的分类显示名。 */
    public String displayCategory() {
        return I18n.category(category);
    }

    public void addValues(Value... vls) {
        values.addAll(List.of(vls));
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onAfterAttack(EventAfterAttack event) {
    }

    public void onCamera(EventCamera event) {
    }

    public void onChamsPre(EventChamsPre event) {
    }

    public void onChamsAfter(EventChamsAfter event) {
    }

    public void onInvMove(EventInvMove event) {
    }

    public void onKey(EventKey event) {
    }

    public void onModuleToggle(EventModuleToggle event) {
    }

    public void onMoveInput(EventMoveInput event) {
    }

    public void onPacket(EventPacket event) {
    }

    public void onRender2D(EventRender2D event) {
    }

    public void onRender3D(EventRender3D event) {
    }

    public void onTick(EventTick event) {
    }

    public void onKick(EventKick event) {
    }

    public void enable() {
        if (enable) return;
        enable = true;
        onEnable();

        new EventModuleToggle(this);
    }

    public void disable() {
        if (!enable) return;
        enable = false;
        onDisable();

        new EventModuleToggle(this);
    }

    public void toggle() {
        if (enable) {
            disable();
        } else {
            enable();
        }
    }

}
