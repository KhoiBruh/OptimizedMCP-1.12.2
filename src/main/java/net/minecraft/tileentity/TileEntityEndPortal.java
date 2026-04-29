package net.minecraft.tileentity;

import net.minecraft.util.Facing;

public class TileEntityEndPortal extends TileEntity {

	public boolean shouldRenderFace(Facing p_184313_1_) {

		return p_184313_1_ == Facing.UP;
	}

}
