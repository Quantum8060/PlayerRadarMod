package xyz.qmc.client;

import net.minecraft.client.gui.screens.Screen;
import xyz.qmc.qolib.api.client.config.ClientConfig;
import xyz.qmc.qolib.api.client.config.ConfigValue;

/** Client-side settings for the JourneyMap Locator Bar overlay. */
public final class PlayerRadarConfig {
	private static final ConfigValue<Boolean> OVERLAY_ENABLED = ConfigValue.booleanValue("overlayEnabled", true);
	private static final ClientConfig CONFIG = ClientConfig.builder("player-radar-mod")
		.add(OVERLAY_ENABLED)
		.build();

	private PlayerRadarConfig() {
	}

	public static void load() {
		CONFIG.load();
	}

	public static boolean overlayEnabled() {
		return OVERLAY_ENABLED.get();
	}

	public static void setOverlayEnabled(boolean enabled) {
		OVERLAY_ENABLED.set(enabled);
	}

	public static void save() {
		CONFIG.save();
	}

	public static Screen createScreen(Screen parent) {
		return CONFIG.screen("Player Radar Mod")
			.category("journeymap_overlay", "JourneyMap Overlay", menu -> menu
				.toggle("Show Locator Bar players", "Show players received through Minecraft's Locator Bar on JourneyMap.", OVERLAY_ENABLED))
			.build(parent);
	}
}
