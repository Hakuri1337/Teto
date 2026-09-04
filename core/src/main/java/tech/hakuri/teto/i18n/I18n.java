package tech.hakuri.teto.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * 界面文案的双语查询。默认中文，可在运行时用 {@link #setLanguage} 切换。
 * <p>
 * <b>身份与显示分离</b>：模块与数值的「身份」永远是模块类名（如 {@code KillAura}）
 * 或配置里的中文字面值（如 {@code "杀戮光环"}），<b>不是</b>当前显示的文字。
 * 这样切换语言只影响 HUD / ClickGUI 的显示，不影响：
 * <ul>
 *   <li>配置存取（{@code Save}/{@code Load} 按键匹配，跨语言不变）</li>
 *   <li>Web 界面的 toggle/update 请求（键不随语言变）</li>
 *   <li>模块间互相引用（{@code PingPongKB.toggle(...)} 等）</li>
 * </ul>
 * 本类只负责「键 → 当前语言的文字」这一个查询。
 */
public final class I18n {

    private static volatile Language language = Language.ZH;

    private I18n() {
    }

    public static Language language() {
        return language;
    }

    public static void setLanguage(Language lang) {
        language = lang == null ? Language.ZH : lang;
    }

    public static void toggle() {
        language = language == Language.ZH ? Language.EN : Language.ZH;
    }

    /**
     * 取当前语言下的文字。{@code key} 是匹配身份（中文字面值），
     * {@code en} 是英文显示；没有英文译名时两边都给 {@code key}。
     */
    public static String tr(String key, String en) {
        return language == Language.EN && en != null && !en.isEmpty() ? en : key;
    }

    // ===== 模块 / 数值 / 分类 的注册表 =====
    // key 是构造器里原本写的中文字面值（也是配置里存的名字），value 是英文显示。
    // 英文译名以模块类名为准（用户要求），个别名字不直译类名时在注释里说明。

    private static final Map<String, String> MODULE_EN = new HashMap<>();
    private static final Map<String, String> CATEGORY_EN = new HashMap<>();
    private static final Map<String, String> VALUE_EN = new HashMap<>();

    static {
        CATEGORY_EN.put("战斗", "Combat");
        CATEGORY_EN.put("移动", "Movement");
        CATEGORY_EN.put("方块", "Block");
        CATEGORY_EN.put("背包", "Inventory");
        CATEGORY_EN.put("视觉", "Render");
        CATEGORY_EN.put("杂项", "Misc");

        // 模块：英文译名以类名为准（按用户要求）
        MODULE_EN.put("放置无冷却", "FastPlace");
        MODULE_EN.put("假方块", "GhostBlock");
        MODULE_EN.put("破坏无冷却", "NoBreakDelay");
        MODULE_EN.put("自动搭路", "Scaffold");
        MODULE_EN.put("自瞄", "Aim");
        MODULE_EN.put("连点", "AutoClicker");
        MODULE_EN.put("自动金头", "AutoHead");
        MODULE_EN.put("自动汤", "AutoSoup");
        MODULE_EN.put("方块全反", "BlockNoKB");
        MODULE_EN.put("刀爆", "Criticals");
        MODULE_EN.put("自动控距", "KeepRange");
        MODULE_EN.put("杀戮光环", "KillAura");
        MODULE_EN.put("发包光环", "MultiAura");
        MODULE_EN.put("点击无延迟", "NoClickDelay");
        MODULE_EN.put("反击退", "PingPongKB");
        MODULE_EN.put("回溯", "StopEvading");
        MODULE_EN.put("超级击退", "SuperKB");
        MODULE_EN.put("百米大刀", "TPAura");
        MODULE_EN.put("瞄到就打", "TriggerBot");
        MODULE_EN.put("自动工具", "AutoTool");
        MODULE_EN.put("自动武器", "AutoWeapon");
        MODULE_EN.put("拿箱", "ChestStealer");
        MODULE_EN.put("容器速点", "FastInvClick");
        MODULE_EN.put("自动钓鱼", "AutoFishing");
        MODULE_EN.put("点击管理器", "ClickManager");
        MODULE_EN.put("按键管理器", "KeyListener");
        MODULE_EN.put("被封提示", "KickReason");
        MODULE_EN.put("玩家探测", "PlayerDetector");
        MODULE_EN.put("转头管理器", "RotationManager");
        MODULE_EN.put("目标管理器", "TargetManager");
        MODULE_EN.put("边缘蹲下", "Eagle");
        MODULE_EN.put("飞行", "Fly");
        MODULE_EN.put("背包行走", "InvMove");
        MODULE_EN.put("跳跃重置", "JumpReset");
        MODULE_EN.put("击中不减速", "KeepSprint");
        MODULE_EN.put("无摔落伤害", "NoFall");
        MODULE_EN.put("无跳跃冷却", "NoJumpDelay");
        MODULE_EN.put("时间管理", "PulseTimer");
        MODULE_EN.put("疾跑", "Sprint");
        MODULE_EN.put("贴图透视", "Chams");
        MODULE_EN.put("碰撞箱透视", "ESP");
        MODULE_EN.put("外置菜单", "ExternalClickGui");
        MODULE_EN.put("外置功能列表", "ExternalHUD");
        MODULE_EN.put("功能列表", "HUD");
        MODULE_EN.put("内置菜单", "InGameClickGUI");
        MODULE_EN.put("夜视", "NightVision");
        MODULE_EN.put("网页外置菜单", "WebGUI");
        // 通用设置（本系统引入）
        MODULE_EN.put("设置", "Settings");

        // 数值（value）：按中文字面值直译
        VALUE_EN.put("中立", "Neutral");
        VALUE_EN.put("假人", "Bots");
        VALUE_EN.put("其他实体", "Other Entities");
        VALUE_EN.put("刷新间隔", "Refresh Interval");
        VALUE_EN.put("包括斜向移动", "Include Diagonal");
        VALUE_EN.put("友善", "Passive");
        VALUE_EN.put("反向击退", "Reverse KB");
        VALUE_EN.put("回原位", "Return");
        VALUE_EN.put("尽量绕过阻挡", "Avoid Obstacles");
        VALUE_EN.put("左键时开始", "On Left Click");
        VALUE_EN.put("快几倍", "Faster ×");
        VALUE_EN.put("快多久", "Faster Duration");
        VALUE_EN.put("慢几倍", "Slower ×");
        VALUE_EN.put("慢多久", "Slower Duration");
        VALUE_EN.put("拿着武器时开始", "While Holding Weapon");
        VALUE_EN.put("挖掘时停止", "Stop While Mining");
        VALUE_EN.put("排序", "Sort");
        VALUE_EN.put("摔落距离", "Fall Distance");
        VALUE_EN.put("攻击范围", "Attack Range");
        VALUE_EN.put("敌对", "Hostile");
        VALUE_EN.put("最大尝试", "Max Attempts");
        VALUE_EN.put("最大每秒点击", "Max CPS");
        VALUE_EN.put("最大距离", "Max Distance");
        VALUE_EN.put("最小每秒点击", "Min CPS");
        VALUE_EN.put("最小距离", "Min Distance");
        VALUE_EN.put("村民", "Villagers");
        VALUE_EN.put("模式", "Mode");
        VALUE_EN.put("步长", "Step");
        VALUE_EN.put("水晶", "Crystals");
        VALUE_EN.put("火球", "Fireballs");
        VALUE_EN.put("点击冷却", "Click Delay");
        VALUE_EN.put("玩家", "Players");
        VALUE_EN.put("瞄准时停止", "Stop While Aiming");
        VALUE_EN.put("自动放置", "Auto Place");
        VALUE_EN.put("视野", "FOV");
        VALUE_EN.put("超时", "Timeout");
        VALUE_EN.put("距离", "Distance");
        VALUE_EN.put("路径范围", "Path Range");
        VALUE_EN.put("速度", "Speed");
        VALUE_EN.put("间隔延迟", "Interval Delay");
        VALUE_EN.put("队友", "Teammates");
        VALUE_EN.put("隐身", "Invisible");
        VALUE_EN.put("风格", "Style");
        // 通用设置的数值
        VALUE_EN.put("语言", "Language");
        VALUE_EN.put("主题", "Theme");
        VALUE_EN.put("辉光", "Bloom");
        VALUE_EN.put("浅色", "Light");
        VALUE_EN.put("深色", "Dark");
    }

    /** 模块显示名（中文键 → 当前语言）。 */
    public static String module(String zhName) {
        return tr(zhName, MODULE_EN.get(zhName));
    }

    /** 分类显示名。 */
    public static String category(String zhName) {
        return tr(zhName, CATEGORY_EN.get(zhName));
    }

    /** 数值显示名。 */
    public static String value(String zhName) {
        return tr(zhName, VALUE_EN.get(zhName));
    }

    /** 纯文案（HUD 标题、提示等）。 */
    public static String text(String zh, String en) {
        return tr(zh, en);
    }
}
