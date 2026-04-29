package net.minecraft.client.renderer.block.model;

import net.minecraft.util.Facing;
import org.lwjgl.util.vector.Vector3f;

public record BlockPartRotation(Vector3f origin, Facing.Axis axis, float angle, boolean rescale) {

}
