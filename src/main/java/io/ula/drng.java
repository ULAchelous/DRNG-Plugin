package io.ula;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.ula.commands.ControlCmd;
import io.ula.commands.PermissionCmd;
import io.ula.config.ConfigFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.awt.*;
import java.io.File;
import java.util.Random;

public final class drng extends JavaPlugin {
    public static final String PLG_ID = "dr-next";
    public static final Logger LOGGER = LogManager.getLogger(PLG_ID);
    public static final File serverRoot = Bukkit.getServer().getWorldContainer();

    public static ConfigFile PMS_CODES = new ConfigFile("permission_codes.json");
    public static ConfigFile DRNG_PERMISSIONS = new ConfigFile("permissions.json");
    public static ConfigFile PLAYER_TITLES = new ConfigFile("player_titles.json");
    public static ConfigFile DRNG_TIPS = new ConfigFile("tips.json");

    @Override
    public void onEnable() {
        ControlCmd.plugin = this;
        //为命令类的plugin对象传值
        ScoreBoardHelper.init(this);
        registCommands();
        startTasks();
    }

    private void registCommands(){
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS , commandsReloadableRegistrarEvent ->
                commandsReloadableRegistrarEvent.registrar().register(PermissionCmd.buildpms));
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS , commandsReloadableRegistrarEvent ->
                commandsReloadableRegistrarEvent.registrar().register(ControlCmd.buildCCmd));
    }

    private void startTasks(){
        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.runTaskTimer(this,() ->{
            DRNG_TIPS.reload();
            JsonArray tips = DRNG_TIPS.getKey("tips").getAsJsonArray();
            getServer().sendMessage(Component.text("[")
                    .decorate(TextDecoration.BOLD)
                    .append(Component.text("提示").color(TextColor.color(Color.YELLOW.getRGB())))
                    .append(Component.text("]:"))
            );
            getServer().sendMessage(Component.text(tips.get(new Random().nextInt(tips.size())).getAsString()));
        },0,1800*20);//定时发送提示
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
