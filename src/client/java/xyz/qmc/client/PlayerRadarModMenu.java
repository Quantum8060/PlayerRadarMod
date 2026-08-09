package xyz.qmc.client;

import com.terraformersmc.modmenu.api.ModMenuApi;

/** Supplies Player Radar Mod's QoLib screen to Mod Menu when Mod Menu is installed. */
public final class PlayerRadarModMenu implements ModMenuApi {
	@Override
	public com.terraformersmc.modmenu.api.ConfigScreenFactory<?> getModConfigScreenFactory() {
		return PlayerRadarConfig::createScreen;
	}
}
