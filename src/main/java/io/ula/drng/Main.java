package io.ula.drng;

import com.google.gson.JsonArray;
import io.ula.drng.commands.ControlCmd;
import io.ula.drng.config.ConfigFile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.Random;
import java.util.UUID;


public final class Main extends JavaPlugin {
    public static final String PLG_ID = "dr-next";
    public static final Logger LOGGER = LogManager.getLogger(PLG_ID);
    public static final File serverRoot = Bukkit.getServer().getWorldContainer();

    public static ConfigFile PMS_CODES = new ConfigFile("permission_codes.json");
    public static ConfigFile DRNG_PERMISSIONS = new ConfigFile("permissions.json");
    public static ConfigFile PLAYER_TITLES = new ConfigFile("player_titles.json");
    public static ConfigFile DRNG_TIPS = new ConfigFile("tips.json");
    public static ConfigFile DRNG_NOTICES = new ConfigFile("notices.json");
    public static ConfigFile COMMAND_EXECUTE = new ConfigFile("cmd_exe.json");
    public static ConfigFile COMMANDS_TO_LOG = new ConfigFile("log_cmd.json");

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
        },1800*20,1800*20);//定时发送提示
        scheduler.runTaskTimer(this,() ->{
            int owCnt=0,netherCnt=0,teCnt=0;
            World overworld = Bukkit.getWorld(Key.key("overworld"));
            World nether = Bukkit.getWorld(Key.key("the_nether"));
            World the_end = Bukkit.getWorld(Key.key("the_end"));
            for(Entity entity:overworld.getEntities()){
                if (entity.getType() == EntityType.ITEM){
                    entity.remove();
                    owCnt++;
                }
            }
            for(Entity entity:nether.getEntities()){
                if (entity.getType() == EntityType.ITEM){
                    entity.remove();
                    netherCnt++;
                }
            }
            for(Entity entity:the_end.getEntities()){
                if (entity.getType() == EntityType.ITEM){
                    entity.remove();
                    teCnt++;
                }
            }
           getServer().sendMessage(Component.empty()
                   .append(Component.object(ObjectContents.playerHead("AZ9C")))
                   .append(Component.text("["))
                   .append(Component.text("AL-1S").color(TextColor.fromCSSHexString("#76d7ea")).decorate(TextDecoration.BOLD))
                   .append(Component.text("]"))
                   .append(Component.space())
                   .append(Component.text("吃掉了").append(Component.space()).append(Component.text(owCnt+netherCnt+teCnt,TextColor.color(Color.YELLOW.getRGB()))).append(Component.space()).append(Component.text("个掉落物！")))
           );
            getServer().sendMessage(Component.text("主世界：")
                    .append(Component.text(owCnt,TextColor.color(Color.YELLOW.getRGB())).decorate(TextDecoration.BOLD))
                    .append(Component.space())
                    .append(Component.text("下界："))
                    .append(Component.text(netherCnt,TextColor.color(Color.YELLOW.getRGB())).decorate(TextDecoration.BOLD))
                    .append(Component.space())
                    .append(Component.text("末地："))
                    .append(Component.text(teCnt,TextColor.color(Color.YELLOW.getRGB())).decorate(TextDecoration.BOLD))
            );
        },1200*20,1200*20);//扫地机器人
        scheduler.runTaskTimer(this,() -> {
            if(getServer().getOnlinePlayers().size() >= 3000) {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player.hasMetadata("onlineTime")) {
                        int value = player.getMetadata("onlineTime").getFirst().asInt();
                        player.setMetadata("onlineTimeCache", new FixedMetadataValue(this, value));
                        player.setMetadata("onlineTime", new FixedMetadataValue(this, value + 1));
                        ScoreBoardHelper.updateScores(player, 3);
                    }
                }
            }
        },20,20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
