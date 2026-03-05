package io.ula.drng.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;


public class Configs {
    public static class ConfigPath{
        public static final String PLAYER = "player";
        public static final String PERMISSION = "permission";
        public static final String TALKBAR = "talkbar";
        public static final String NOTICE = "notice";
        public static final String COMMAND = "command";
    }

    public static ConfigFile PMS_CODES;
    public static ConfigFile DRNG_PERMISSIONS;
    public static ConfigFile PLAYER_TITLES ;
    public static ConfigFile DRNG_TIPS;
    public static ConfigFile DRNG_NOTICES;
    public static ConfigFile LOG_CMD;
    public static ConfigFile PLAYER_HOMES;
    public static ConfigFile CHAT_REPLACEMENTS;
    public static ConfigFile PLAYER_EULA;
    public static ConfigFile CONFIG;
    public static void init(JavaPlugin ownerPlugin){
        String version = ownerPlugin.getPluginMeta().getVersion();
        JsonObject mainConfigs = new JsonObject();
        JsonObject log_cmd = new JsonObject();
        mainConfigs.addProperty("version",version);
        mainConfigs.addProperty("allowCreativeMode",false);
        mainConfigs.addProperty("balancedOp",true);
        log_cmd.addProperty("version",version);
        log_cmd.add("commands",new JsonArray());

        CONFIG = new ConfigFile("config.json",null,mainConfigs,ownerPlugin);
        LOG_CMD = new ConfigFile("log_cmd.json",ConfigPath.COMMAND,log_cmd,ownerPlugin);
        PMS_CODES = new ConfigFile("permission_codes.json",ConfigPath.PERMISSION,null,ownerPlugin);
        DRNG_PERMISSIONS = new ConfigFile("permissions.json",ConfigPath.PERMISSION,null,ownerPlugin);
        PLAYER_TITLES = new ConfigFile("player_titles.json",ConfigPath.PLAYER,null,ownerPlugin);
        DRNG_TIPS = new ConfigFile("tips.json",ConfigPath.TALKBAR,null,ownerPlugin);
        DRNG_NOTICES = new ConfigFile("notices.json",ConfigPath.NOTICE,null,ownerPlugin);
        PLAYER_HOMES = new ConfigFile("player_homes.json",ConfigPath.PLAYER,null,ownerPlugin);
        CHAT_REPLACEMENTS = new ConfigFile("chat_replacements.json",ConfigPath.PLAYER,null,ownerPlugin);
        PLAYER_EULA = new ConfigFile("player_eula.json",ConfigPath.PLAYER,null,ownerPlugin);
    }
}
