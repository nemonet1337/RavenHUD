package work.nemonet.ravenhud;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HUD_ENABLED = BUILDER
            .comment("Whether to display the HUD")
            .define("hudEnabled", true);

    public static final ModConfigSpec.EnumValue<HudMode> HUD_MODE = BUILDER
            .comment("HUD mode (RAVEN = Armored Core style, TYPE_B = Cockpit style, ACV = V style, AC6 = VI style)")
            .defineEnum("hudMode", HudMode.RAVEN);

    public static final ModConfigSpec.EnumValue<ColorPreset> COLOR_PRESET = BUILDER
            .comment("Color preset (CUSTOM = use custom hex values below, or select ORANGE, CYAN, GREEN, WHITE presets)")
            .defineEnum("colorPreset", ColorPreset.CUSTOM);

    public static final ModConfigSpec.ConfigValue<String> COLOR_NORMAL = BUILDER
            .comment("Normal Line Color (AARRGGBB, hex)")
            .define("colorNormal", "c7ffae00");

    public static final ModConfigSpec.ConfigValue<String> COLOR_WARNING = BUILDER
            .comment("Warning Color (AARRGGBB, hex)")
            .define("colorWarning", "e5ffff00");

    public static final ModConfigSpec.ConfigValue<String> COLOR_ALERT = BUILDER
            .comment("Alert Color (AARRGGBB, hex)")
            .define("colorAlert", "e5ff0000");

    public static final ModConfigSpec.ConfigValue<String> COLOR_NUMBER = BUILDER
            .comment("Number Color (AARRGGBB, hex)")
            .define("colorNumber", "ccd09000");

    public static final ModConfigSpec.ConfigValue<String> COLOR_IRON = BUILDER
            .comment("Iron Ore Alert Color (RRGGBB, hex)")
            .define("colorIron", "807050");

    public static final ModConfigSpec.ConfigValue<String> COLOR_GOLD = BUILDER
            .comment("Gold Ore Alert Color (RRGGBB, hex)")
            .define("colorGold", "999000");

    public static final ModConfigSpec.ConfigValue<String> COLOR_DIAMOND = BUILDER
            .comment("Diamond Ore Alert Color (RRGGBB, hex)")
            .define("colorDiamond", "009999");

    public static final ModConfigSpec.ConfigValue<String> COLOR_LAVA = BUILDER
            .comment("Lava Alert Color (RRGGBB, hex)")
            .define("colorLava", "ff3000");

    public static final ModConfigSpec.DoubleValue LINE_ALPHA = BUILDER
            .comment("HUD Line Alpha Value")
            .defineInRange("lineAlpha", 0.8D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue LINE_WIDTH = BUILDER
            .comment("HUD Line Width")
            .defineInRange("lineWidth", 1.0D, 0.5D, 10.0D);

    public static final ModConfigSpec.IntValue DEG_OFFSET = BUILDER
            .comment("Orientation Degree Offset")
            .defineInRange("degOffset", 18, 0, 36);

    static final ModConfigSpec SPEC = BUILDER.build();

    public enum HudMode {
        RAVEN,
        TYPE_B,
        ACV,
        AC6
    }

    public enum ColorPreset {
        CUSTOM,
        ORANGE,
        CYAN,
        GREEN,
        WHITE
    }
}

