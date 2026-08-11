package xyz.qmc.client;

import net.fabricmc.api.ClientModInitializer;
import xyz.qmc.qolib.api.client.config.ConfigScreenRegistry;

public class PlayerRadarModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PlayerRadarConfig.load();
		ConfigScreenRegistry.register("player-radar-mod", "Player Radar Mod", PlayerRadarConfig::createScreen);
		LocatorPlayerTracker.initialize();
	}
}
