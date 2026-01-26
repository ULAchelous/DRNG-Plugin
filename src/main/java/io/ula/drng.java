package io.ula;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.ula.commands.BanExtraCmd;
import io.ula.commands.ControlCmd;
import io.ula.commands.PermissionCmd;
import io.ula.config.ConfigFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class drng extends JavaPlugin {
    public static final String PLG_ID = "dr-next";
    public static final Logger LOGGER = LogManager.getLogger(PLG_ID);
    public static final File serverRoot = Bukkit.getServer().getWorldContainer();

    public static ConfigFile PMS_CODES = new ConfigFile("permission_codes.json");
    public static ConfigFile DRNG_PERMISSIONS = new ConfigFile("permissions.json");
    public static ConfigFile PLAYER_TITLES = new ConfigFile("player_titles.json");
    public static ConfigFile BANNED_PLAYERS = new ConfigFile("banned_players.json");

    @Override
    public void onEnable() {
        ControlCmd.plugin = this;
        BanExtraCmd.plugin = this;
        //为命令类的plugin对象传值
        getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS , commandsReloadableRegistrarEvent ->
            commandsReloadableRegistrarEvent.registrar().register(PermissionCmd.buildpms));
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS , commandsReloadableRegistrarEvent ->
                commandsReloadableRegistrarEvent.registrar().register(ControlCmd.buildCCmd));
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS , commandsReloadableRegistrarEvent ->
                commandsReloadableRegistrarEvent.registrar().register(BanExtraCmd.buildBanExtraCmd));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
