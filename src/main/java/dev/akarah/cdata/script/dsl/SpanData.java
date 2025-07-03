package dev.akarah.cdata.script.dsl;

import net.minecraft.resources.ResourceLocation;

public record SpanData(int cursorStart, int cursorEnd, String originalString, ResourceLocation fileName) {
    @Override
    public String toString() {
        return this.fileName + " at " + cursorStart + ".." + cursorEnd;
    }
}
