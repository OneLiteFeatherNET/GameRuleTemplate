package net.onelitefeather.gameruletemplate.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.onelitefeather.gameruletemplate.GameRuleTemplate;
import net.onelitefeather.gameruletemplate.models.SavedGameRule;
import net.onelitefeather.gameruletemplate.models.SavedGameRuleTemplate;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static net.onelitefeather.gameruletemplate.commands.Helper.BLOCKED_NAME;
import static net.onelitefeather.gameruletemplate.commands.Helper.FILE_NAME_TEMPLATE;
import static net.onelitefeather.gameruletemplate.commands.Helper.generateDifferenceMessageFromDefaultToNew;

public final class CreateRuleTemplate implements CommandExecutionHandler<PlayerSource> {
    private static final Logger LOGGER = ComponentLogger.logger();

    private final GameRuleTemplate plugin;

    public CreateRuleTemplate(GameRuleTemplate plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NonNull CommandContext<PlayerSource> commandContext) {
        boolean override = commandContext.flags().isPresent("override");
        PlayerSource sender = commandContext.sender();
        String name = commandContext.get("name");
        if (name.equalsIgnoreCase(BLOCKED_NAME)) {
            sender.source().sendMessage(Component.translatable("gameruletemplate.template.blocked", Component.text(name)));
            return;
        }
        Player player = sender.source();
        Path saveFile = this.plugin.getDataPath().resolve(FILE_NAME_TEMPLATE.formatted(name.toLowerCase()));
        if (Files.exists(saveFile) && !override) {
            player.sendMessage(Component.translatable("gameruletemplate.template.exists", Component.text(name)));
            return;
        }
        World world = player.getWorld();
        Predicate<GameRule<?>> isDefaultValue = gameRule -> world.getGameRuleValue(gameRule) == world.getGameRuleDefault(gameRule);
        List<@NotNull GameRule<?>> notDefaultValuesList = Stream
                .of(GameRule.values())
                .filter(not(isDefaultValue))
                .toList();
        if (notDefaultValuesList.isEmpty()) {
            player.sendMessage(Component.translatable("gameruletemplate.no.rules.changed"));
            return;
        }
        List<SavedGameRule> savedGameRules = notDefaultValuesList
                .stream()
                .map(gameRule -> SavedGameRule.of(gameRule, world.getGameRuleValue(gameRule)))
                .toList();
        List<TranslatableComponent> translatableComponents = generateDifferenceMessageFromDefaultToNew(world);
        Component diff = Component.join(JoinConfiguration.newlines(), translatableComponents);

        SavedGameRuleTemplate gameRuleTemplate = SavedGameRuleTemplate.of(name, savedGameRules, System.currentTimeMillis(), player.getUniqueId());
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("template", gameRuleTemplate);
        try {
            yamlConfiguration.save(saveFile.toFile());
            player.sendMessage(Component.translatable("gameruletemplate.template.saved.success", Component.text(name)));
            player.sendMessage(diff);
        } catch (Exception e) {
            player.sendMessage(Component.translatable("gameruletemplate.template.saved.failed", Component.text(name)));
            LOGGER.error("Failed to save template", e);
        }
    }
}
