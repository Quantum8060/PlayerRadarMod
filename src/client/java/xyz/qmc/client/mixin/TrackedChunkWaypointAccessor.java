package xyz.qmc.client.mixin;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the coarse chunk position carried by a vanilla Locator Bar waypoint. */
@Mixin(targets = "net.minecraft.world.waypoints.TrackedWaypoint$ChunkWaypoint")
public interface TrackedChunkWaypointAccessor {
	@Accessor("chunkPos")
	ChunkPos playerRadarMod$getChunkPos();
}
