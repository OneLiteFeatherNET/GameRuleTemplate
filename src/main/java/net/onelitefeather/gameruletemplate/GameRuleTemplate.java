package net.onelitefeather.gameruletemplate;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.gameruletemplate.commands.ApplyRuleTemplate;
import net.onelitefeather.gameruletemplate.commands.CreateRuleTemplate;
import net.onelitefeather.gameruletemplate.commands.DeleteRuleTemplate;
import net.onelitefeather.gameruletemplate.commands.TemplateNameProvider;
import net.onelitefeather.gameruletemplate.models.SavedGameRule;
import net.onelitefeather.gameruletemplate.models.SavedGameRuleTemplate;
import net.onelitefeather.gameruletemplate.translations.PluginTranslationRegistry;
import net.onelitefeather.gameruletemplate.utils.PermissionsList;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public final class GameRuleTemplate extends JavaPlugin {

    private Optional<PaperCommandManager<Source>> commandManager = Optional.empty();
    private static final TagResolver.Single PREFIX = Placeholder.component("prefix", MiniMessage.miniMessage().deserialize("<gradient:yellow:red:0.4>GameRuleTemplate ┃"));
    public static volatile MiniMessage miniMessage = MiniMessage.builder().tags(TagResolver.resolver(PREFIX, TagResolver.standard())).build();

    @Override
    public void onLoad() {
        saveConfig();
        installLanguages();
    }

    @Override
    public void onEnable() {
        registerConfigObjects();
        createCommandManager();
        createCommands();
        installLanguages();
    }


    @Override
    public void onDisable() {
    }

    private void createCommandManager() {
        PaperCommandManager<Source> manager = PaperCommandManager.builder(
                        PaperSimpleSenderMapper.simpleSenderMapper()
                )
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);
        this.commandManager = Optional.of(manager);
    }

    private void createCommands() {
        this.commandManager.ifPresent(manager -> {
            manager.command(manager
                    .commandBuilder("gameruletemplate","grt")
                    .flag(manager.flagBuilder("override").withPermission(PermissionsList.OVERRIDE).build())
                    .senderType(PlayerSource.class)
                    .permission(PermissionsList.CREATE_TEMPLATE)
                    .literal("create")
                    .required("name", stringParser(), new TemplateNameProvider(this))
                    .handler(new CreateRuleTemplate(this))
                    .build()
            );
            manager.command(manager
                    .commandBuilder("gameruletemplate","grt")
                    .senderType(PlayerSource.class)
                    .permission(PermissionsList.APPLY_TEMPLATE)
                    .literal("apply")
                    .required("name", stringParser(), new TemplateNameProvider(this))
                    .handler(new ApplyRuleTemplate(this))
                    .build()
            );
            manager.command(manager
                    .commandBuilder("gameruletemplate","grt")
                    .senderType(PlayerSource.class)
                    .permission(PermissionsList.DELETE_TEMPLATE)
                    .literal("delete")
                    .required("name", stringParser(), new TemplateNameProvider(this))
                    .handler(new DeleteRuleTemplate(this))
                    .build()
            );
        });
    }

    private void registerConfigObjects() {
        ConfigurationSerialization.registerClass(SavedGameRule.class);
        ConfigurationSerialization.registerClass(SavedGameRuleTemplate.class);
    }

    private void installLanguages() {
        final TranslationRegistry translationRegistry = new PluginTranslationRegistry(TranslationRegistry.create(Key.key("gameruletemplate", "translations")));
        translationRegistry.defaultLocale(Locale.US);
        Path langFolder = getDataPath().resolve("lang");
        HashSet<String> languages = new HashSet<>();
        languages.add("en-US");
        languages.addAll(getConfig().getStringList("languages"));
        if (Files.exists(langFolder)) {
            try (var urlClassLoader = new URLClassLoader(new URL[]{langFolder.toUri().toURL()})) {
                languages.stream().map(Locale::forLanguageTag).forEach(r -> {
                    var bundle = ResourceBundle.getBundle("gameruletemplate", r, urlClassLoader, UTF8ResourceBundleControl.get());
                    translationRegistry.registerAll(r, bundle, false);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            languages.stream().map(Locale::forLanguageTag).forEach(r -> {
                var bundle = ResourceBundle.getBundle("gameruletemplate", r, UTF8ResourceBundleControl.get());
                translationRegistry.registerAll(r, bundle, false);
            });
        }
        GlobalTranslator.translator().addSource(translationRegistry);
    }
}
