package io.ula.drng;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import io.ula.drng.commands.*;
import io.ula.drng.config.ConfigFile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;

import static io.ula.drng.Main.DRNG_NOTICES;
import static io.ula.drng.Main.PLAYER_TITLES;
import static io.ula.drng.PlayerListener.getPlayerTitles;

public class DrngBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context){
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,commands -> {
            commands.registrar().register(PermissionCmd.buildpms);
            commands.registrar().register(ControlCmd.buildCCmd);
            commands.registrar().register(NoticeCmd.noticeCmd);
        });//注册命令

        context.getLifecycleManager().registerEventHandler(RegistryEvents.DIALOG.compose()
                .newHandler(content -> content.registry().register(
                        DialogKeys.create(Key.key("drng:noticedialog")),
                        builder -> builder
                                .base(DialogBase
                                        .builder(Component.text("发布公告"))
                                        .inputs(List.of(
                                                DialogInput.text("author",Component.text("发布者")).build(),
                                                DialogInput.text("content",400,Component.text("正文"),true,"",132, TextDialogInput.MultilineOptions.create(15,100)),//正文
                                                DialogInput.singleOption("time_limit",Component.text("时间期限"),List.of(
                                                        SingleOptionDialogInput.OptionEntry.create("1",Component.text("1天"),true),
                                                        SingleOptionDialogInput.OptionEntry.create("5",Component.text("5天"),false),
                                                        SingleOptionDialogInput.OptionEntry.create("30",Component.text("30天"),false)
                                                )).build(),
                                                DialogInput.bool("accept",Component.text("我承诺，我同意希望之地的最终用户许可协议")).build()
                                            )
                                        )//对话框中的输入部分
                                        .build())
                                .type(DialogType.confirmation(
                                        ActionButton.create(Component.text("取消")
                                                .append(Component.object(ObjectContents.sprite(Key.key("gui"),Key.key("pending_invite/reject")))),
                                                Component.text("点击以取消发布公告"),
                                                120,
                                                null
                                        )
                                        ,ActionButton.create(Component.text("确认")
                                                .append(Component.object(ObjectContents.sprite(Key.key("gui"),Key.key("pending_invite/accept")))),
                                                Component.text(("点击以发布公告")),
                                                120,
                                                DialogAction.customClick(
                                                        (view,audience) -> {
                                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                                                                    .withZone(ZoneId.of("Asia/Shanghai"));
                                                            String author = view.getText("author");
                                                            String notice_content = view.getText("content");
                                                            JsonObject notice = new JsonObject();
                                                            notice.addProperty("author",author);
                                                            notice.addProperty("content",notice_content);
                                                            notice.addProperty("created_time",formatter.format(LocalDate.now(ZoneId.of("Asia/Shanghai"))));
                                                            notice.addProperty("deadline",formatter.format(LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(Integer.parseInt(view.getText("time_limit")))));
                                                            if(view.getBoolean("accept")){
                                                                if(DRNG_NOTICES.has("notices")){
                                                                    DRNG_NOTICES.getKey("notices").getAsJsonArray().add(notice);
                                                                    DRNG_NOTICES.write();
                                                                }else{
                                                                    DRNG_NOTICES.addKey("notices",new JsonArray());
                                                                    DRNG_NOTICES.getKey("notices").getAsJsonArray().add(notice);
                                                                    DRNG_NOTICES.write();
                                                                }
                                                                Bukkit.getServer().sendMessage(Component.text("公告板上有新的信息！").color(TextColor.color(Color.yellow.getRGB())).decorate(TextDecoration.BOLD));
                                                            }else{
                                                                audience.sendMessage(Component.text("因为未同意许可，公告未发送").color(TextColor.color(Color.RED.getRGB())));
                                                            }
                                                        },//发布公告后对话框的相应逻辑
                                                        ClickCallback.Options.builder()
                                                                .uses(Integer.MAX_VALUE)
                                                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                                                .build()
                                                )
                                        )
                                        )
                                )
                ))
        );//注册对话框
    }
}
