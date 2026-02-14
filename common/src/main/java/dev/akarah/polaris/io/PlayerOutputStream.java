package dev.akarah.polaris.io;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.OutputStream;

public class PlayerOutputStream extends OutputStream {
    String builtUpText = "";
    ServerPlayer player;

    public PlayerOutputStream(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void write(int i) throws IOException {

        if(i == '\n') {
            this.flush();
        } else {
            builtUpText = builtUpText + (char) i;
        }
    }

    @Override
    public void flush() throws IOException {
        player.sendSystemMessage(Component.literal(builtUpText));
        this.builtUpText = "";
    }

    @Override
    public void close() throws IOException {

        player.sendSystemMessage(Component.literal(builtUpText));
        this.builtUpText = "";
    }
}
