package net.onelitefeather.gameruletemplate.models;

import org.bukkit.GameRule;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record SavedGameRule(String name, Object value)  implements ConfigurationSerializable {
    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of(
                "name", this.name(),
                "value", this.value()
        );
    }

    public static SavedGameRule deserialize(Map<String, Object> map) {
        return new SavedGameRule(
                (String) map.get("name"),
                map.get("value")
        );
    }

    public static SavedGameRule of(GameRule<?> rule, Object value) {
        return new SavedGameRule(rule.getName(), value);
    }

    public GameRule<?> getGameRule() {
        return GameRule.getByName(this.name);
    }
}
