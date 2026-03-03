package io.ula.drng.config;

import com.google.gson.JsonObject;

public class Configs {
    private static JsonObject mainConfigs = new JsonObject();
    public static ConfigFile PMS_CODES = new ConfigFile("permission_codes.json","permission",null);
    public static ConfigFile DRNG_PERMISSIONS = new ConfigFile("permissions.json","permission",null);
    public static ConfigFile PLAYER_TITLES = new ConfigFile("player_titles.json","player",null);
    public static ConfigFile DRNG_TIPS = new ConfigFile("tips.json","talkbar",null);
    public static ConfigFile DRNG_NOTICES = new ConfigFile("notices.json","notice",null);
    public static ConfigFile COMMAND_EXECUTE = new ConfigFile("cmd_exe.json","log",null);
    public static ConfigFile COMMANDS_TO_LOG = new ConfigFile("log_cmd.json","log",null);
    public static ConfigFile PLAYER_HOMES = new ConfigFile("player_homes.json","player",null);
    public static ConfigFile CHAT_REPLACEMENTS = new ConfigFile("chat_replacements.json","player",null);
    public static ConfigFile PLAYER_EULA = new ConfigFile("player_eula.json","player",null);
    public static ConfigFile CONFIG;
    public static void init(){
        mainConfigs.addProperty("version","v1.5.1");
        mainConfigs.addProperty("allowCreativeMode",false);
        mainConfigs.addProperty("balancedOp",true);
        CONFIG = new ConfigFile("config.json",null,mainConfigs);
    }
}
