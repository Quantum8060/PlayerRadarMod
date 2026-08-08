package xyz.qmc.client;

import net.fabricmc.api.ClientModInitializer;

public class PlayerRadarModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PlayerRadarConfig.load();
		LocatorPlayerTracker.initialize();
	}
}
