package net.minecraft.command;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import java.util.Objects;

public class CommandSenderWrapper implements ICommandSender {

	private final ICommandSender delegate;

	
	private final Vec3d positionVector;

	
	private final BlockPos position;

	
	private final Integer permissionLevel;

	
	private final Entity entity;

	
	private final Boolean sendCommandFeedback;

	public CommandSenderWrapper(ICommandSender delegateIn, Vec3d positionVectorIn, BlockPos positionIn, Integer permissionLevelIn, Entity entityIn, Boolean sendCommandFeedbackIn) {

		delegate = delegateIn;
		positionVector = positionVectorIn;
		position = positionIn;
		permissionLevel = permissionLevelIn;
		entity = entityIn;
		sendCommandFeedback = sendCommandFeedbackIn;
	}

	public static CommandSenderWrapper create(ICommandSender sender) {

		return sender instanceof CommandSenderWrapper ? (CommandSenderWrapper) sender : new CommandSenderWrapper(sender, null, null, null, null, null);
	}

	public CommandSenderWrapper withEntity(Entity entityIn, Vec3d p_193997_2_) {

		return entity == entityIn && Objects.equals(positionVector, p_193997_2_) ? this : new CommandSenderWrapper(delegate, p_193997_2_, new BlockPos(p_193997_2_), permissionLevel, entityIn, sendCommandFeedback);
	}

	public CommandSenderWrapper withPermissionLevel(int level) {

		return permissionLevel != null && permissionLevel <= level ? this : new CommandSenderWrapper(delegate, positionVector, position, level, entity, sendCommandFeedback);
	}

	public CommandSenderWrapper withSendCommandFeedback(boolean sendCommandFeedbackIn) {

		return sendCommandFeedback == null || sendCommandFeedback && !sendCommandFeedbackIn ? new CommandSenderWrapper(delegate, positionVector, position, permissionLevel, entity, sendCommandFeedbackIn) : this;
	}

	public CommandSenderWrapper computePositionVector() {

		return positionVector != null ? this : new CommandSenderWrapper(delegate, getPositionVector(), getPosition(), permissionLevel, entity, sendCommandFeedback);
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return entity != null ? entity.getName() : delegate.getName();
	}

	/**
	 * Get the formatted ChatComponent that will be used for the sender's username in chat
	 */
	public ITextComponent getDisplayName() {

		return entity != null ? entity.getDisplayName() : delegate.getDisplayName();
	}

	/**
	 * Send a chat message to the CommandSender
	 */
	public void sendMessage(ITextComponent component) {

		if (sendCommandFeedback == null || sendCommandFeedback) {
			delegate.sendMessage(component);
		}
	}

	/**
	 * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
	 */
	public boolean canUseCommand(int permLevel, String commandName) {

		return (permissionLevel == null || permissionLevel >= permLevel) && delegate.canUseCommand(permLevel, commandName);
	}

	/**
	 * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
	 * the coordinates 0, 0, 0
	 */
	public BlockPos getPosition() {

		if (position != null) {
			return position;
		} else {
			return entity != null ? entity.getPosition() : delegate.getPosition();
		}
	}

	/**
	 * Get the position vector. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return 0.0D,
	 * 0.0D, 0.0D
	 */
	public Vec3d getPositionVector() {

		if (positionVector != null) {
			return positionVector;
		} else {
			return entity != null ? entity.getPositionVector() : delegate.getPositionVector();
		}
	}

	/**
	 * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
	 * the overworld
	 */
	public World getEntityWorld() {

		return entity != null ? entity.getEntityWorld() : delegate.getEntityWorld();
	}

	

	/**
	 * Returns the entity associated with the command sender. MAY BE NULL!
	 */
	public Entity getCommandSenderEntity() {

		return entity != null ? entity.getCommandSenderEntity() : delegate.getCommandSenderEntity();
	}

	/**
	 * Returns true if the command sender should be sent feedback about executed commands
	 */
	public boolean sendCommandFeedback() {

		return sendCommandFeedback != null ? sendCommandFeedback : delegate.sendCommandFeedback();
	}

	public void setCommandStat(CommandResultStats.Type type, int amount) {

		if (entity != null) {
			entity.setCommandStat(type, amount);
		} else {
			delegate.setCommandStat(type, amount);
		}
	}

	

	/**
	 * Get the Minecraft server instance
	 */
	public MinecraftServer getServer() {

		return delegate.getServer();
	}

}
