package dev.akarah.polaris.devext;

import com.mojang.serialization.JsonOps;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class DevExtensionServer extends WebSocketServer {

    public DevExtensionServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        var json = DevExtensionStatics.DataBundle.CODEC.encodeStart(JsonOps.INSTANCE, DevExtensionStatics.DATA_BUNDLE).getOrThrow();
        conn.send(json.toString());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }
}
