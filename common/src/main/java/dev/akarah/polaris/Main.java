package dev.akarah.polaris;

import dev.akarah.polaris.io.ExceptionPrinter;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static MinecraftServer SERVER;
    public static Logger LOGGER = LoggerFactory.getLogger("polaris");

    public static MinecraftServer server() {
        return SERVER;
    }

    public static void handleError(Exception e) {
        String sb = "\n"
                + "A fatal error occurred while generating code! Server may fail to start."
                + "\n" + e.getMessage()
                + "\n";

        LOGGER.error(sb);

        try {
            ExceptionPrinter.writeExceptionToOps(e);
        } catch (Exception ignored) {

        }

        if(SERVER == null) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
