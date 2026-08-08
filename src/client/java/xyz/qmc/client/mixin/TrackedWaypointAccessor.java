package xyz.qmc.client.mixin;

import net.minecraft.core.Vec3i;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the exact block position carried by a vanilla locator-bar waypoint. */
@Mixin(targets = "net.minecraft.world.waypoints.TrackedWaypoint$Vec3iWaypoint")
public interface TrackedWaypointAccessor {
	@Accessor("vector")
	Vec3i playerRadarMod$getVector();
}
