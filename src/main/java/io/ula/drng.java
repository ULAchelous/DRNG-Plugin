package io.ula;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.ula.commands.permissionCmd;
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

    @Override
    public void onEnable() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS , commandsReloadableRegistrarEvent -> {
            commandsReloadableRegistrarEvent.registrar().register(permissionCmd.buildpms);
        });
        getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
