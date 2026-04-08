package dk.thedemonprince;

import dk.thedemonprince.config.ConfigManager;
import net.fabricmc.api.ModInitializer;

public class Signplate implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigManager.getInstance().load();
    }
}
