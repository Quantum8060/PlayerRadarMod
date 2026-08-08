package xyz.qmc.client.journeymap;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;
import xyz.qmc.PlayerRadarMod;
import xyz.qmc.client.LocatorPlayerTracker;

/** JourneyMap's soft-dependency entrypoint. */
@JourneyMapPlugin(apiVersion = "2.0.0")
public final class PlayerRadarJourneyMapPlugin implements IClientPlugin {
	@Override
	public void initialize(IClientAPI jmAPI) {
		LocatorPlayerTracker.addListener(new JourneyMapPlayerOverlaySynchronizer(jmAPI));
		PlayerRadarMod.LOGGER.info("JourneyMap player overlay integration initialized");
	}

	@Override
	public String getModId() {
		return PlayerRadarMod.MOD_ID;
	}
}
