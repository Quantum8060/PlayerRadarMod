package xyz.qmc.client;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint;
import xyz.qmc.PlayerRadarMod;
import xyz.qmc.client.mixin.TrackedChunkWaypointAccessor;
import xyz.qmc.client.mixin.TrackedWaypointAccessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Converts positions received by the vanilla Locator Bar into client-only snapshots. */
public final class LocatorPlayerTracker {
	private static final CopyOnWriteArrayList<Consumer<Map<UUID, LocatorPlayer>>> LISTENERS = new CopyOnWriteArrayList<>();
	private static final Map<UUID, CachedPosition> LAST_KNOWN_POSITIONS = new HashMap<>();

	private LocatorPlayerTracker() {
	}

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> publishSnapshot());
	}

	public static void addListener(Consumer<Map<UUID, LocatorPlayer>> listener) {
		LISTENERS.add(listener);
	}

	private static void publishSnapshot() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) {
			LAST_KNOWN_POSITIONS.clear();
			notifyListeners(Map.of());
			return;
		}

		Map<UUID, LocatorPlayer> snapshot = new HashMap<>();
		Set<UUID> activeWaypointIds = new HashSet<>();
		minecraft.getConnection().getWaypointManager().forEachWaypoint(minecraft.player, waypoint -> {
			Either<UUID, String> id = waypoint.id();
			id.left().ifPresent(uuid -> {
				if (uuid.equals(minecraft.player.getUUID())) {
					return;
				}
				activeWaypointIds.add(uuid);

				BlockPos blockPos = positionOf(waypoint, minecraft.player.blockPosition());
				if (blockPos == null) {
					CachedPosition cachedPosition = LAST_KNOWN_POSITIONS.get(uuid);
					if (cachedPosition == null) {
						return;
					}
					blockPos = cachedPosition.position();
				} else {
					LAST_KNOWN_POSITIONS.put(uuid, new CachedPosition(blockPos, minecraft.level.dimension()));
				}
				CachedPosition position = LAST_KNOWN_POSITIONS.get(uuid);
				snapshot.put(uuid, new LocatorPlayer(uuid, blockPos, position.dimension(), displayName(minecraft, uuid)));
			});
		});
		LAST_KNOWN_POSITIONS.keySet().retainAll(activeWaypointIds);
		notifyListeners(Map.copyOf(snapshot));
	}

	private static BlockPos positionOf(TrackedWaypoint waypoint, BlockPos localPlayerPosition) {
		if (waypoint instanceof TrackedWaypointAccessor positionWaypoint) {
			Vec3i position = positionWaypoint.playerRadarMod$getVector();
			return new BlockPos(position.getX(), position.getY(), position.getZ());
		}
		if (waypoint instanceof TrackedChunkWaypointAccessor chunkWaypoint) {
			var chunkPos = chunkWaypoint.playerRadarMod$getChunkPos();
			return new BlockPos(chunkPos.getMiddleBlockX(), localPlayerPosition.getY(), chunkPos.getMiddleBlockZ());
		}
		return null;
	}

	private static String displayName(Minecraft minecraft, UUID uuid) {
		var playerInfo = minecraft.getConnection().getPlayerInfo(uuid);
		if (playerInfo != null && playerInfo.getProfile().name() != null && !playerInfo.getProfile().name().isBlank()) {
			return playerInfo.getProfile().name();
		}
		return "Player " + uuid.toString().substring(0, 8);
	}

	private static void notifyListeners(Map<UUID, LocatorPlayer> snapshot) {
		for (Consumer<Map<UUID, LocatorPlayer>> listener : LISTENERS) {
			try {
				listener.accept(snapshot);
			} catch (RuntimeException exception) {
				PlayerRadarMod.LOGGER.error("Unable to synchronize Locator Bar players", exception);
			}
		}
	}

	public record LocatorPlayer(UUID uuid, BlockPos position, ResourceKey<Level> dimension, String displayName) {
	}

	private record CachedPosition(BlockPos position, ResourceKey<Level> dimension) {
	}
}
