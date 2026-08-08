package xyz.qmc.client.journeymap;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.display.DisplayType;
import journeymap.api.v2.client.display.MarkerOverlay;
import journeymap.api.v2.client.event.EntityRadarUpdateEvent;
import journeymap.api.v2.client.model.MapImage;
import journeymap.api.v2.common.event.ClientEventRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import xyz.qmc.PlayerRadarMod;
import xyz.qmc.client.PlayerRadarConfig;
import xyz.qmc.client.LocatorPlayerTracker.LocatorPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** Maintains the far-away half of the Player Radar / Locator Bar handoff. */
final class JourneyMapPlayerOverlaySynchronizer implements Consumer<Map<UUID, LocatorPlayer>> {
	private static final Identifier PLAYER_ICON = Identifier.fromNamespaceAndPath(PlayerRadarMod.MOD_ID, "images/player_marker.png");
	private static final long STANDARD_RADAR_GRACE_PERIOD_MILLIS = 1_500L;
	private static final long OVERLAY_REFRESH_INTERVAL_MILLIS = 1_000L;
	private final IClientAPI api;
	private final Map<UUID, MarkerOverlay> overlays = new HashMap<>();
	private final Map<UUID, Long> standardRadarLastSeen = new HashMap<>();
	private long lastOverlayRefreshMillis;

	JourneyMapPlayerOverlaySynchronizer(IClientAPI api) {
		this.api = api;
		ClientEventRegistry.ENTITY_RADAR_UPDATE_EVENT.subscribe(PlayerRadarMod.MOD_ID, this::onStandardRadarUpdate);
	}

	@Override
	public void accept(Map<UUID, LocatorPlayer> players) {
		if (!PlayerRadarConfig.overlayEnabled() || !api.playerAccepts(PlayerRadarMod.MOD_ID, DisplayType.Marker)) {
			removeAll();
			return;
		}

		long now = System.currentTimeMillis();
		boolean forceRefresh = now - lastOverlayRefreshMillis >= OVERLAY_REFRESH_INTERVAL_MILLIS;
		if (forceRefresh) lastOverlayRefreshMillis = now;

		Set<UUID> active = new HashSet<>();
		for (LocatorPlayer player : players.values()) {
			if (isShownByStandardRadar(player.uuid())) continue;
			active.add(player.uuid());
			showOrMove(player, forceRefresh);
		}

		overlays.keySet().removeIf(uuid -> {
			if (!active.contains(uuid)) {
				api.remove(overlays.get(uuid));
				return true;
			}
			return false;
		});
	}

	private void onStandardRadarUpdate(EntityRadarUpdateEvent event) {
		var entity = event.getWrappedEntity().getEntityRef().get();
		if (entity instanceof Player player && !player.isLocalPlayer()) {
			standardRadarLastSeen.put(player.getUUID(), System.currentTimeMillis());
		}
	}

	private boolean isShownByStandardRadar(UUID uuid) {
		Long lastSeen = standardRadarLastSeen.get(uuid);
		if (lastSeen == null) return false;
		if (System.currentTimeMillis() - lastSeen <= STANDARD_RADAR_GRACE_PERIOD_MILLIS) return true;
		standardRadarLastSeen.remove(uuid);
		return false;
	}

	private void showOrMove(LocatorPlayer player, boolean forceRefresh) {
		MarkerOverlay overlay = overlays.get(player.uuid());
		if (forceRefresh && overlay != null) {
			api.remove(overlay);
			overlays.remove(player.uuid());
			overlay = null;
		}
		if (overlay == null) {
			MapImage image = new MapImage(PLAYER_ICON, 0, 0, 16, 16, colorFor(player.uuid()), 1.0F);
			image.centerAnchors();
			overlay = new MarkerOverlay(PlayerRadarMod.MOD_ID, player.position(), image);
			overlay.setDimension(player.dimension()).setTitle("Locator Bar player").setLabel(player.displayName());
			overlays.put(player.uuid(), overlay);
		} else {
			overlay.setPoint(player.position());
			overlay.setLabel(player.displayName());
		}
		try {
			api.show(overlay);
		} catch (Exception exception) {
			PlayerRadarMod.LOGGER.warn("Unable to display Locator Bar player overlay", exception);
		}
	}

	private void removeAll() {
		for (MarkerOverlay overlay : overlays.values()) api.remove(overlay);
		overlays.clear();
	}

	private static int colorFor(UUID uuid) {
		return 0xFF000000 | (uuid.hashCode() & 0x00FFFFFF);
	}
}
