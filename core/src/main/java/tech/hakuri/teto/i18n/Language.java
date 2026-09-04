package tech.hakuri.teto.i18n;

/**
 * 界面语言。存储与匹配用的身份是「键」（模块类名、中文字面值），
 * 这里只是决定把键渲染成哪种语言，所以放进 core 与两版共用。
 */
public enum Language {
    ZH("中文", "zh"),
    EN("English", "en");

    public final String displayName;
    public final String code;

    Language(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public static Language byCode(String code) {
        for (Language language : values()) {
            if (language.code.equalsIgnoreCase(code)) {
                return language;
            }
        }
        return ZH;
    }
}
