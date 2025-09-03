package dev.akarah.polaris.io;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class PlayerWriter extends Writer {
    ServerPlayer player;
    StringWriter stringWriter = new StringWriter();

    public PlayerWriter(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void write(char @NotNull[] chars, int i, int i1) {
        stringWriter.write(chars, i, i1);
    }

    @Override
    public void flush() throws IOException {
        this.player.sendSystemMessage(Component.literal(stringWriter.toString()));
        this.stringWriter = new StringWriter();
    }

    @Override
    public void close() throws IOException {
        stringWriter.close();
    }
}
