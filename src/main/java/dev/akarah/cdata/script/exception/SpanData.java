package dev.akarah.cdata.script.exception;

import com.mojang.brigadier.StringReader;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SpanData(int cursorStart, int cursorEnd, String originalString, ResourceLocation fileName) {
    @Override
    public String toString() {
        return this.fileName + " at " + cursorStart + ".." + cursorEnd;
    }

    public static SpanData merge(SpanData from, SpanData to) {
        return new SpanData(from.cursorStart(), to.cursorEnd(), to.originalString(), to.fileName());
    }

    public DebugInfo debugInfo() {
        var sr = new StringReader(originalString);
        int column = 1;
        int row = 1;
        for(int i = 0; i < cursorStart; i++) {
            if (sr.read() == '\n') {
                column = 1;
                row += 1;
            } else {
                column += 1;
            }
        }

        var lines = this.originalString.split("\n");

        return new DebugInfo(this.fileName.toString(), lines[row - 1], column - 1, column + (cursorEnd - cursorStart), row);
    }

    public record DebugInfo(
            String fileName,
            String linePreview,
            int from,
            int to,
            int line
    ) {
        @Override
        public @NotNull String toString() {
            var startingWs = 0;
            for(var ch : linePreview.toCharArray()) {
                if(ch == ' ') {
                    startingWs += 1;
                } else if (ch == '\t') {
                    startingWs += 4;
                } else {
                    break;
                }
            }
            return linePreview.trim() +
                    "\n" +
                    " ".repeat(from - startingWs) +
                    "^".repeat(to - from)
                    + "\n in " + fileName + ", line " + line;
        }
    }
}
