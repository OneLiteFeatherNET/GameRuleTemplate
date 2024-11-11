package net.onelitefeather.gameruletemplate.commands;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.onelitefeather.gameruletemplate.GameRuleTemplate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class TemplateNameProvider implements SuggestionProvider<PlayerSource> {

    private final GameRuleTemplate plugin;
    private static final Logger LOGGER = ComponentLogger.logger();


    public TemplateNameProvider(GameRuleTemplate plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<PlayerSource> context, @NonNull CommandInput input) {
        String inputString = input.readString();
        List<Suggestion> suggestions = new ArrayList<>();
        try(Stream<Path> paths = Files.list(this.plugin.getDataPath())) {
            suggestions.addAll(paths.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(inputString))
                    .filter(name -> name.endsWith(".yml"))
                    .filter(Predicate.not(name -> name.equals("config.yml")))
                    .map(name -> name.substring(0, name.indexOf('.')))
                    .map(Suggestion::suggestion)
                    .toList());
        } catch (IOException e) {
            LOGGER.error("Error while listing files in data folder", e);
        }
        return CompletableFuture.completedFuture(suggestions);
    }
}
