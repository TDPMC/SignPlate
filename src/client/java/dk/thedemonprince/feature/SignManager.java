package dk.thedemonprince.feature;

import dk.thedemonprince.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.math.BlockPos;

public class SignManager {
    private static SignManager instance;

    private SignManager() {}

    public static SignManager getInstance() {
        if (instance == null) {
            instance = new SignManager();
        }
        return instance;
    }

    public boolean isSignPlateEnabled() {
        return ConfigManager.getInstance().getConfig().signPlateEnabled;
    }

    public ConfigManager.SignTemplate getSelectedTemplate() {
        ConfigManager.ConfigData config = ConfigManager.getInstance().getConfig();
        if (config.selectedSignTemplate >= 0 && config.selectedSignTemplate < config.signTemplates.size()) {
            return config.signTemplates.get(config.selectedSignTemplate);
        }
        return null;
    }

    public void applyTemplate(BlockPos pos, boolean front) {
        ConfigManager.SignTemplate template = getSelectedTemplate();
        if (template == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;

        // Send update packet to server
        client.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(
                pos,
                front,
                template.line1,
                template.line2,
                template.line3,
                template.line4
        ));
    }
}
