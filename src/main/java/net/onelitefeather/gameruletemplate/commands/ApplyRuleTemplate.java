package net.onelitefeather.gameruletemplate.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.onelitefeather.gameruletemplate.GameRuleTemplate;
import net.onelitefeather.gameruletemplate.models.SavedGameRule;
import net.onelitefeather.gameruletemplate.models.SavedGameRuleTemplate;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static net.onelitefeather.gameruletemplate.commands.Helper.BLOCKED_NAME;
import static net.onelitefeather.gameruletemplate.commands.Helper.FILE_NAME_TEMPLATE;
import static net.onelitefeather.gameruletemplate.commands.Helper.generateDifferenceMessageFromSaveState;

public final class ApplyRuleTemplate implements CommandExecutionHandler<PlayerSource> {

    private final GameRuleTemplate plugin;

    public ApplyRuleTemplate(GameRuleTemplate plugin) {
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
        World world = player.getWorld();
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(saveFile.toFile());
        Optional.ofNullable(yamlConfiguration.getObject("template", SavedGameRuleTemplate.class))
                .map(SavedGameRuleTemplate::rules)
                .ifPresent(savedGameRules -> applyRules(world, name, savedGameRules, player));
    }

    private void applyRules(World world, String name, List<SavedGameRule> savedGameRules, Player player) {
        var differenceMessage = generateDifferenceMessageFromSaveState(savedGameRules, world);
        savedGameRules.forEach(savedGameRule -> world.setGameRuleValue(savedGameRule.getGameRule().getName(), savedGameRule.value().toString()));
        if (differenceMessage.isEmpty()) {
            player.sendMessage(Component.translatable("gameruletemplate.game_rule_needs_not_applied", Component.text(name)));
            return;
        }
        Component diff = Component.join(JoinConfiguration.newlines(), differenceMessage);
        player.sendMessage(Component.translatable("gameruletemplate.game_rule_applied", Component.text(name)));
        player.sendMessage(diff);
    }
}
