package net.onelitefeather.gameruletemplate.models;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SavedGameRuleTemplate(String name, List<SavedGameRule> rules, long timestamp, String creator) implements ConfigurationSerializable {

    public SavedGameRuleTemplate(String name, List<SavedGameRule> rules, long timestamp, UUID creator) {
        this(name, rules, timestamp, creator.toString());
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of(
                "name", this.name(),
                "rules", this.rules(),
                "timestamp", this.timestamp(),
                "creator", this.creator()
        );
    }

    public static SavedGameRuleTemplate deserialize(Map<String, Object> map) {
        return new SavedGameRuleTemplate(
                (String) map.get("name"),
                (List<SavedGameRule>) map.get("rules"),
                (long) map.get("timestamp"),
                (String) map.get("creator")
        );
    }

    public static SavedGameRuleTemplate of(String name, List<SavedGameRule> rules, long timestamp, UUID creator) {
        return new SavedGameRuleTemplate(name, rules, timestamp, creator);
    }
}
