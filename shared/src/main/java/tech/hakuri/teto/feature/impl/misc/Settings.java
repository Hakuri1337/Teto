package tech.hakuri.teto.feature.impl.misc;

import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.i18n.I18n;
import tech.hakuri.teto.i18n.Language;

/**
 * 通用设置：界面语言、主题、辉光。
 * <p>
 * 语言切换只改 {@link I18n} 的全局语言，<b>不改任何模块的 {@code name}</b> ——
 * 身份（配置键、Web 请求键）因此跨语言稳定。
 */
public class Settings extends Module {

    public static Value language = new Value("语言", "中文", java.util.List.of("中文", "English"));
    public static Value theme = new Value("主题", "深色", java.util.List.of("深色", "浅色"));
    public static Value bloom = new Value("辉光", true);

    public Settings() {
        name = "设置";
        category = Category.misc;
        addValues(language, theme, bloom);
    }

    @Override
    public void onEnable() {
        apply();
    }

    @Override
    public void onTick(tech.hakuri.teto.event.impl.EventTick event) {
        apply();
    }

    /** 把 Value 的当前选择同步到 I18n。放在 tick 里是为了让「在 ClickGUI 里改语言」即时生效。 */
    private static void apply() {
        I18n.setLanguage("English".equals(language.currentMode) ? Language.EN : Language.ZH);
    }

    public static boolean isLight() {
        return "浅色".equals(theme.currentMode);
    }

    public static boolean bloomEnabled() {
        return bloom.enable;
    }
}
