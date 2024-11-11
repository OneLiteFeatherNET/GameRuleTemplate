package net.onelitefeather.gameruletemplate.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.onelitefeather.gameruletemplate.GameRuleTemplate;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.onelitefeather.gameruletemplate.commands.Helper.BLOCKED_NAME;
import static net.onelitefeather.gameruletemplate.commands.Helper.FILE_NAME_TEMPLATE;

public final class DeleteRuleTemplate implements CommandExecutionHandler<PlayerSource> {

    private static final Logger LOGGER = ComponentLogger.logger();

    private final GameRuleTemplate plugin;

    public DeleteRuleTemplate(GameRuleTemplate plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NonNull CommandContext<PlayerSource> commandContext) {
        PlayerSource sender = commandContext.sender();
        String name = commandContext.get("name");
        if (name.equalsIgnoreCase(BLOCKED_NAME)) {
            sender.source().sendMessage(Component.translatable("gameruletemplate.template.blocked", Component.text(name)));
            return;
        }
        Player player = sender.source();
        Path saveFile = this.plugin.getDataPath().resolve(FILE_NAME_TEMPLATE.formatted(name.toLowerCase()));
        if (Files.notExists(saveFile)) {
            player.sendMessage(Component.translatable("gameruletemplate.error.template_not_found", Component.text(name)));
            return;
        }
        try {
            Files.delete(saveFile);
            player.sendMessage(Component.translatable("gameruletemplate.template.deleted", Component.text(name)));
        } catch (IOException e) {
            player.sendMessage(Component.translatable("gameruletemplate.error.template_not_deleted", Component.text(name)));
            LOGGER.error("Failed to delete template file", e);
        }
    }
}
