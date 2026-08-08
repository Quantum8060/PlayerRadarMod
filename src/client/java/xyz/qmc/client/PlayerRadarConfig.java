package xyz.qmc.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.qmc.PlayerRadarMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Client-side settings for the JourneyMap Locator Bar overlay. */
public final class PlayerRadarConfig {
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("player-radar-mod.json");
	private static boolean overlayEnabled = true;

	private PlayerRadarConfig() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) return;
		try {
			JsonObject root = JsonParser.parseString(Files.readString(CONFIG_PATH)).getAsJsonObject();
			if (root.has("overlayEnabled")) overlayEnabled = root.get("overlayEnabled").getAsBoolean();
		} catch (Exception exception) {
			PlayerRadarMod.LOGGER.warn("Unable to load Player Radar Mod settings; using defaults", exception);
		}
	}

	public static boolean overlayEnabled() { return overlayEnabled; }
	public static void setOverlayEnabled(boolean enabled) { overlayEnabled = enabled; }

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, "{\n  \"overlayEnabled\": " + overlayEnabled + "\n}\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (IOException exception) {
			PlayerRadarMod.LOGGER.error("Unable to save Player Radar Mod settings", exception);
		}
	}

	public static Screen createScreen(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
			.title(Component.literal("Player Radar Mod"))
			.category(ConfigCategory.createBuilder().name(Component.literal("JourneyMap Overlay"))
				.option(Option.<Boolean>createBuilder().name(Component.literal("Show Locator Bar players"))
					.description(OptionDescription.of(Component.literal("Show players received through Minecraft's Locator Bar on JourneyMap.")))
					.binding(true, PlayerRadarConfig::overlayEnabled, PlayerRadarConfig::setOverlayEnabled)
					.controller(BooleanControllerBuilder::create).build())
				.build())
			.save(PlayerRadarConfig::save).build().generateScreen(parent);
	}
}
