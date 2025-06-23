package dev.akarah.cdata.registry.text;

import dev.akarah.cdata.property.PropertyMap;

import java.util.Optional;

public record ParseContext(
        Optional<PropertyMap> entityProperties,
        Optional<PropertyMap> itemProperties
) {
    public static ParseContext empty() {
        return new ParseContext(Optional.empty(), Optional.empty());
    }

    public static ParseContext entity(PropertyMap entityProperties) {
        return new ParseContext(Optional.of(entityProperties), Optional.empty());
    }

    public static ParseContext item(PropertyMap itemProperties) {
        return new ParseContext(Optional.empty(), Optional.of(itemProperties));
    }

    public PropertyMap defaultProperties() {
        return this.entityProperties
                .or(() -> this.itemProperties)
                .orElse(PropertyMap.EMPTY);
    }
}
