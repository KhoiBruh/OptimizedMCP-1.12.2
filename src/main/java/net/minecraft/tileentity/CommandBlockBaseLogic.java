package net.minecraft.tileentity;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class CommandBlockBaseLogic implements ICommandSender {

	/**
	 * The formatting for the timestamp on commands run.
	 */
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private long lastExecution = -1L;
	private boolean updateLastExecution = true;

	/**
	 * The number of successful commands run. (used for redstone output)
	 */
	private int successCount;
	private boolean trackOutput = true;

	/**
	 * The previously run command.
	 */
	private ITextComponent lastOutput;

	/**
	 * The command stored in the command block.
	 */
	private String commandStored = "";

	/**
	 * The custom name of the command block. (defaults to "@")
	 */
	private String customName = "@";
	private final CommandResultStats resultStats = new CommandResultStats();

	/**
	 * returns the successCount int.
	 */
	public int getSuccessCount() {

		return successCount;
	}

	public void setSuccessCount(int successCountIn) {

		successCount = successCountIn;
	}

	/**
	 * Returns the lastOutput.
	 */
	public ITextComponent getLastOutput() {

		return lastOutput == null ? new TextComponentString("") : lastOutput;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound p_189510_1_) {

		p_189510_1_.setString("Command", commandStored);
		p_189510_1_.setInteger("SuccessCount", successCount);
		p_189510_1_.setString("CustomName", customName);
		p_189510_1_.setBoolean("TrackOutput", trackOutput);

		if (lastOutput != null && trackOutput) {
			p_189510_1_.setString("LastOutput", ITextComponent.Serializer.componentToJson(lastOutput));
		}

		p_189510_1_.setBoolean("UpdateLastExecution", updateLastExecution);

		if (updateLastExecution && lastExecution > 0L) {
			p_189510_1_.setLong("LastExecution", lastExecution);
		}

		resultStats.writeStatsToNBT(p_189510_1_);
		return p_189510_1_;
	}

	/**
	 * Reads NBT formatting and stored data into variables.
	 */
	public void readDataFromNBT(NBTTagCompound nbt) {

		commandStored = nbt.getString("Command");
		successCount = nbt.getInteger("SuccessCount");

		if (nbt.hasKey("CustomName", 8)) {
			customName = nbt.getString("CustomName");
		}

		if (nbt.hasKey("TrackOutput", 1)) {
			trackOutput = nbt.getBoolean("TrackOutput");
		}

		if (nbt.hasKey("LastOutput", 8) && trackOutput) {
			try {
				lastOutput = ITextComponent.Serializer.jsonToComponent(nbt.getString("LastOutput"));
			} catch (Throwable throwable) {
				lastOutput = new TextComponentString(throwable.getMessage());
			}
		} else {
			lastOutput = null;
		}

		if (nbt.hasKey("UpdateLastExecution")) {
			updateLastExecution = nbt.getBoolean("UpdateLastExecution");
		}

		if (updateLastExecution && nbt.hasKey("LastExecution")) {
			lastExecution = nbt.getLong("LastExecution");
		} else {
			lastExecution = -1L;
		}

		resultStats.readStatsFromNBT(nbt);
	}

	/**
	 * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
	 */
	public boolean canUseCommand(int permLevel, String commandName) {

		return permLevel <= 2;
	}

	/**
	 * Sets the command.
	 */
	public void setCommand(String command) {

		commandStored = command;
		successCount = 0;
	}

	/**
	 * Returns the command of the command block.
	 */
	public String getCommand() {

		return commandStored;
	}

	public boolean trigger(World worldIn) {

		if (!worldIn.isRemote && worldIn.getTotalWorldTime() != lastExecution) {
			if ("Searge".equalsIgnoreCase(commandStored)) {
				lastOutput = new TextComponentString("#itzlipofutzli");
				successCount = 1;
				return true;
			} else {
				MinecraftServer minecraftserver = getServer();

				if (minecraftserver != null && minecraftserver.isAnvilFileSet() && minecraftserver.isCommandBlockEnabled()) {
					try {
						lastOutput = null;
						successCount = minecraftserver.getCommandManager().executeCommand(this, commandStored);
					} catch (Throwable throwable) {
						CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Executing command block");
						CrashReportCategory crashreportcategory = crashreport.makeCategory("Command to be executed");
						crashreportcategory.addDetail("Command", new ICrashReportDetail<String>() {
							public String call() throws Exception {

								return getCommand();
							}
						});
						crashreportcategory.addDetail("Name", new ICrashReportDetail<String>() {
							public String call() throws Exception {

								return getName();
							}
						});
						throw new ReportedException(crashreport);
					}
				} else {
					successCount = 0;
				}

				if (updateLastExecution) {
					lastExecution = worldIn.getTotalWorldTime();
				} else {
					lastExecution = -1L;
				}

				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return customName;
	}

	public void setName(String name) {

		customName = name;
	}

	/**
	 * Send a chat message to the CommandSender
	 */
	public void sendMessage(ITextComponent component) {

		if (trackOutput && getEntityWorld() != null && !getEntityWorld().isRemote) {
			lastOutput = (new TextComponentString("[" + TIMESTAMP_FORMAT.format(new Date()) + "] ")).appendSibling(component);
			updateCommand();
		}
	}

	/**
	 * Returns true if the command sender should be sent feedback about executed commands
	 */
	public boolean sendCommandFeedback() {

		MinecraftServer minecraftserver = getServer();
		return minecraftserver == null || !minecraftserver.isAnvilFileSet() || minecraftserver.worlds[0].getGameRules().getBoolean("commandBlockOutput");
	}

	public void setCommandStat(CommandResultStats.Type type, int amount) {

		resultStats.setCommandStatForSender(getServer(), this, type, amount);
	}

	public abstract void updateCommand();

	/**
	 * Currently this returns 0 for the traditional command block, and 1 for the minecart command block
	 */
	public abstract int getCommandBlockType();

	/**
	 * Fills in information about the command block for the packet. entityId for the minecart version, and X/Y/Z for the
	 * traditional version
	 */
	public abstract void fillInInfo(ByteBuf buf);

	public void setLastOutput(@Nullable ITextComponent lastOutputMessage) {

		lastOutput = lastOutputMessage;
	}

	public void setTrackOutput(boolean shouldTrackOutput) {

		trackOutput = shouldTrackOutput;
	}

	public boolean shouldTrackOutput() {

		return trackOutput;
	}

	public boolean tryOpenEditCommandBlock(EntityPlayer playerIn) {

		if (!playerIn.canUseCommandBlock()) {
			return false;
		} else {
			if (playerIn.getEntityWorld().isRemote) {
				playerIn.displayGuiEditCommandCart(this);
			}

			return true;
		}
	}

	public CommandResultStats getCommandResultStats() {

		return resultStats;
	}

}
