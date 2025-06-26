package dev.akarah.cdata.registry.text;

import dev.akarah.cdata.property.Properties;
import dev.akarah.cdata.property.Property;
import dev.akarah.cdata.property.PropertyMap;
import dev.akarah.cdata.registry.stat.StatsObject;

import java.util.Optional;

public record ParseContext(
        Optional<PropertyMap> entityProperties,
        Optional<PropertyMap> itemProperties,
        Optional<StatsObject> playerStats,
        Optional<StatsObject> itemStats
) {
    public static ParseContext empty() {
        return new ParseContext(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static ParseContext entity(PropertyMap entityProperties) {
        return new ParseContext(
                Optional.of(entityProperties),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static ParseContext item(PropertyMap itemProperties) {
        return new ParseContext(
                Optional.empty(),
                Optional.of(itemProperties),
                Optional.empty(),
                Optional.of(itemProperties.get(Properties.STATS).orElse(StatsObject.EMPTY))
        );
    }

    public PropertyMap defaultProperties() {
        return this.entityProperties
                .or(() -> this.itemProperties)
                .orElse(PropertyMap.EMPTY);
    }

    public StatsObject defaultStatsObject() {
        return this.playerStats
                .or(() -> this.itemStats)
                .orElse(StatsObject.EMPTY);
    }
}
