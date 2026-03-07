package io.ula.drng.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.ula.drng.Main;
import io.ula.drng.config.ConfigFile;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static io.ula.drng.config.Configs.*;

public class NoticeCmd {
    public static Main ownerPlugin;
    static LiteralArgumentBuilder<CommandSourceStack> noticeCmdBuilder = Commands.literal("notice")
            .then(Commands.literal("list")
                    .executes(commandContext -> {
                        Player player = (Player)commandContext.getSource().getSender();
                        player.openBook(getNoticeBook());
                        return 0;
                    }
            ))
            .then(Commands.literal("write")
                    .executes(context -> {
                        Player player = (Player)context.getSource().getSender();
                        DialogLike dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key("drng:noticedialog"));
                        player.showDialog(dialog);
                        return 0;
                    })
            )
            .requires(commandSourceStack -> (commandSourceStack.getSender() instanceof Player));
    public static LiteralCommandNode<CommandSourceStack> noticeCmd = noticeCmdBuilder.build();
    public static Book getNoticeBook(){
        Book.Builder book = Book.book(Component.text("公告栏"),Component.text("Server")).toBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                .withZone(ZoneId.of("Asia/Shanghai"));

        ConfigFile DRNG_NOTICES = ownerPlugin.getConfigManager().getConfig(Key.key("drng:notices"));

        if(DRNG_NOTICES.has("notices")) {
            for (int i=0;i<DRNG_NOTICES.getKey("notices").getAsJsonArray().size();i++) {
                JsonElement notice = DRNG_NOTICES.getKey("notices").getAsJsonArray().get(i);
                String author = notice.getAsJsonObject().get("author").getAsString();
                String content = notice.getAsJsonObject().get("content").getAsString();
                String deadline = notice.getAsJsonObject().get("deadline").getAsString();
                if (LocalDate.now(ZoneId.of("Asia/Shanghai")).isAfter(LocalDate.parse(deadline,formatter))){
                    notice.getAsJsonObject().addProperty("removed",true);
                    DRNG_NOTICES.getKey("notices").getAsJsonArray().set(i,notice);
                    continue;
                }
                book.addPage(Component.empty()
                        .append(Component.text("发布者:").color(TextColor.color(Color.GRAY.getRGB())).decorate(TextDecoration.BOLD))
                        .append(Component.space())
                        .append(Component.text(author))
                        .append(Component.newline())
                        .append(Component.text(content))
                        .append(Component.newline())
                        .append(Component.text("发布时间：").color(TextColor.color(Color.GRAY.getRGB())).decorate(TextDecoration.BOLD))
                        .append(Component.text(notice.getAsJsonObject().get("created_time").getAsString()))
                        .append(Component.newline())
                        .append(Component.text("截止时间：").color(TextColor.color(Color.GRAY.getRGB())).decorate(TextDecoration.BOLD))
                        .append(Component.text(notice.getAsJsonObject().get("deadline").getAsString()))
                );
            }
            for(int i=0;i<DRNG_NOTICES.getKey("notices").getAsJsonArray().size();i++){
                JsonElement notice = DRNG_NOTICES.getKey("notices").getAsJsonArray().get(i);
                if(notice.getAsJsonObject().has("removed")) {
                    DRNG_NOTICES.getKey("notices").getAsJsonArray().remove(i);
                    i=0;
                }
            }
        }
        return book.build();
    }
}
