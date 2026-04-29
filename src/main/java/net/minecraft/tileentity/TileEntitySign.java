package net.minecraft.tileentity;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

public class TileEntitySign extends TileEntity {

	public final ITextComponent[] signText = new ITextComponent[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};
	private final CommandResultStats stats = new CommandResultStats();
	/**
	 * The index of the line currently being edited. Only used on client side, but defined on both. Note this is only
	 * really used when the > < are going to be visible.
	 */
	public int lineBeingEdited = -1;
	private boolean isEditable = true;
	private EntityPlayer player;

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		super.writeToNBT(compound);

		for (int i = 0; i < 4; ++i) {
			String s = ITextComponent.Serializer.componentToJson(signText[i]);
			compound.setString("Text" + (i + 1), s);
		}

		stats.writeStatsToNBT(compound);
		return compound;
	}

	protected void setWorldCreate(World worldIn) {

		setWorld(worldIn);
	}

	public void readFromNBT(NBTTagCompound compound) {

		isEditable = false;
		super.readFromNBT(compound);
		ICommandSender icommandsender = new ICommandSender() {
			public String getName() {

				return "Sign";
			}

			public boolean canUseCommand(int permLevel, String commandName) {

				return true;
			}

			public BlockPos getPosition() {

				return pos;
			}

			public Vec3d getPositionVector() {

				return new Vec3d((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
			}

			public World getEntityWorld() {

				return world;
			}

			public MinecraftServer getServer() {

				return world.getMinecraftServer();
			}
		};

		for (int i = 0; i < 4; ++i) {
			String s = compound.getString("Text" + (i + 1));
			ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(s);

			try {
				signText[i] = TextComponentUtils.processComponent(icommandsender, itextcomponent, null);
			} catch (CommandException var7) {
				signText[i] = itextcomponent;
			}
		}

		stats.readStatsFromNBT(compound);
	}

	
	public SPacketUpdateTileEntity getUpdatePacket() {

		return new SPacketUpdateTileEntity(pos, 9, getUpdateTag());
	}

	public NBTTagCompound getUpdateTag() {

		return writeToNBT(new NBTTagCompound());
	}

	public boolean onlyOpsCanSetNbt() {

		return true;
	}

	public boolean getIsEditable() {

		return isEditable;
	}

	/**
	 * Sets the sign's isEditable flag to the specified parameter.
	 */
	public void setEditable(boolean isEditableIn) {

		isEditable = isEditableIn;

		if (!isEditableIn) {
			player = null;
		}
	}

	public EntityPlayer getPlayer() {

		return player;
	}

	public void setPlayer(EntityPlayer playerIn) {

		player = playerIn;
	}

	public boolean executeCommand(final EntityPlayer playerIn) {

		ICommandSender icommandsender = new ICommandSender() {
			public String getName() {

				return playerIn.getName();
			}

			public ITextComponent getDisplayName() {

				return playerIn.getDisplayName();
			}

			public boolean canUseCommand(int permLevel, String commandName) {

				return permLevel <= 2;
			}

			public BlockPos getPosition() {

				return pos;
			}

			public Vec3d getPositionVector() {

				return new Vec3d((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
			}

			public World getEntityWorld() {

				return playerIn.getEntityWorld();
			}

			public Entity getCommandSenderEntity() {

				return playerIn;
			}

			public void setCommandStat(CommandResultStats.Type type, int amount) {

				if (world != null && !world.isRemote) {
					stats.setCommandStatForSender(world.getMinecraftServer(), this, type, amount);
				}
			}

			public MinecraftServer getServer() {

				return playerIn.getServer();
			}
		};

		for (ITextComponent itextcomponent : signText) {
			Style style = itextcomponent == null ? null : itextcomponent.getStyle();

			if (style != null && style.getClickEvent() != null) {
				ClickEvent clickevent = style.getClickEvent();

				if (clickevent.action() == ClickEvent.Action.RUN_COMMAND) {
					playerIn.getServer().getCommandManager().executeCommand(icommandsender, clickevent.value());
				}
			}
		}

		return true;
	}

	public CommandResultStats getStats() {

		return stats;
	}

}
