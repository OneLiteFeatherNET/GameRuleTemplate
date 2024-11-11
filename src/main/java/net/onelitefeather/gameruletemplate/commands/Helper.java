package net.onelitefeather.gameruletemplate.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.onelitefeather.gameruletemplate.models.SavedGameRule;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

final class Helper {

    private Helper() {
        throw new IllegalStateException("Utility class");
    }

    static final String FILE_NAME_TEMPLATE = "%s.yml";

    static final String BLOCKED_NAME = "config";

    static List<TranslatableComponent> generateDifferenceMessageFromSaveState(List<SavedGameRule> savedGameRules, World world) {
        return savedGameRules.stream()
                .filter(savedGameRule -> !savedGameRule.value().equals(world.getGameRuleValue(savedGameRule.getGameRule())))
                .map(savedGameRule -> {
                    Object oldValue = world.getGameRuleValue(savedGameRule.getGameRule());
                    Object newValue = savedGameRule.value();
                    return Component.translatable("gameruletemplate.game_rule_difference", Component.text(savedGameRule.getGameRule().getName()), Component.text(String.valueOf(oldValue)), Component.text(newValue.toString()));
                }).toList();
    }

    static List<TranslatableComponent> generateDifferenceMessageFromDefaultToNew(World world) {
        Predicate<GameRule<?>> isDefaultValue = gameRule -> world.getGameRuleValue(gameRule) == world.getGameRuleDefault(gameRule);
        List<@NotNull GameRule<?>> notDefaultValuesList = Stream
                .of(GameRule.values())
                .filter(not(isDefaultValue))
                .toList();
        return notDefaultValuesList.stream()
                .map(savedGameRule -> {
                    Object oldValue = world.getGameRuleDefault(savedGameRule);
                    Object newValue = world.getGameRuleValue(savedGameRule);
                    return Component.translatable("gameruletemplate.game_rule_difference", Component.text(savedGameRule.getName()), Component.text(String.valueOf(oldValue)), Component.text(String.valueOf(newValue)));
                }).toList();
    }

}
