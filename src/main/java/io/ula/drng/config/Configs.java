package io.ula.drng.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ula.drng.Main;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;


public class Configs {
    public static class ConfigPath{
        public static final String PLAYER = "player";
        public static final String PERMISSION = "permission";
        public static final String TALKBAR = "talkbar";
        public static final String NOTICE = "notice";
        public static final String COMMAND = "command";
        public static final String INLINE_TRANSLATABLE= "translatable";
    }

//    public static ConfigFile PMS_CODES;
//    public static ConfigFile DRNG_PERMISSIONS;
//    public static ConfigFile PLAYER_TITLES ;
//    public static ConfigFile DRNG_TIPS;
//    public static ConfigFile DRNG_NOTICES;
//    public static ConfigFile LOG_CMD;
//    public static ConfigFile PLAYER_HOMES;
//    public static ConfigFile CHAT_REPLACEMENTS;
//    public static ConfigFile PLAYER_EULA;
//    public static ConfigFile CONFIG;
//    public static InlineConfigFile COMMENTARY;
    public static void init(Main ownerPlugin){
        String version = ownerPlugin.getPluginMeta().getVersion();
        JsonObject mainConfigs = new JsonObject();
        JsonObject log_cmd = new JsonObject();
        mainConfigs.addProperty("version",version);
        mainConfigs.addProperty("allowCreativeMode",false);
        mainConfigs.addProperty("balancedOp",true);
        mainConfigs.addProperty("hg_finished",false);
        log_cmd.addProperty("version",version);
        log_cmd.add("commands",new JsonArray());

        ConfigManager configManager = ownerPlugin.getConfigManager();
         configManager.register(Key.key("drng:main"),new ConfigFile("config.json",null,mainConfigs,ownerPlugin));
        configManager.register(Key.key("drng:log_cmd"),new ConfigFile("log_cmd.json",ConfigPath.COMMAND,log_cmd,ownerPlugin));
        configManager.register(Key.key("drng:pms_codes"),new ConfigFile("permission_codes.json",ConfigPath.PERMISSION,null,ownerPlugin));
        configManager.register(Key.key("drng:pms"),new ConfigFile("permissions.json",ConfigPath.PERMISSION,null,ownerPlugin));
        configManager.register(Key.key("drng:titles"),new ConfigFile("player_titles.json",ConfigPath.PLAYER,null,ownerPlugin));
        configManager.register(Key.key("drng:tips"),new ConfigFile("tips.json",ConfigPath.TALKBAR,null,ownerPlugin));
        configManager.register(Key.key("drng:notices"),new ConfigFile("notices.json",ConfigPath.NOTICE,null,ownerPlugin));
        configManager.register(Key.key("drng:homes"),new ConfigFile("player_homes.json",ConfigPath.PLAYER,null,ownerPlugin));
        configManager.register(Key.key("drng:chat_replacements"),new ConfigFile("chat_replacements.json",ConfigPath.PLAYER,null,ownerPlugin));
        configManager.register(Key.key("drng:eula"),new ConfigFile("player_eula.json",ConfigPath.PLAYER,null,ownerPlugin));
    }
}
