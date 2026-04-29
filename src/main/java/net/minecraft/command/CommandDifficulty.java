package net.minecraft.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Difficulty;
import java.util.Collections;
import java.util.List;

public class CommandDifficulty extends CommandBase {

	/**
	 * Gets the name of the command
	 */
	public String getName() {

		return "difficulty";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {

		return 2;
	}

	/**
	 * Gets the usage string for the command.
	 */
	public String getUsage(ICommandSender sender) {

		return "commands.difficulty.usage";
	}

	/**
	 * Callback for when the command is executed
	 */
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		if (args.length == 0) {
			throw new WrongUsageException("commands.difficulty.usage");
		} else {
			Difficulty enumdifficulty = getDifficultyFromCommand(args[0]);
			server.setDifficultyForAllWorlds(enumdifficulty);
			notifyCommandListener(sender, this, "commands.difficulty.success", new TextComponentTranslation(enumdifficulty.getDifficultyResourceKey()));
		}
	}

	protected Difficulty getDifficultyFromCommand(String difficultyString) throws CommandException {

		if (!"peaceful".equalsIgnoreCase(difficultyString) && !"p".equalsIgnoreCase(difficultyString)) {
			if (!"easy".equalsIgnoreCase(difficultyString) && !"e".equalsIgnoreCase(difficultyString)) {
				if (!"normal".equalsIgnoreCase(difficultyString) && !"n".equalsIgnoreCase(difficultyString)) {
					return !"hard".equalsIgnoreCase(difficultyString) && !"h".equalsIgnoreCase(difficultyString) ? Difficulty.getDifficultyEnum(parseInt(difficultyString, 0, 3)) : Difficulty.HARD;
				} else {
					return Difficulty.NORMAL;
				}
			} else {
				return Difficulty.EASY;
			}
		} else {
			return Difficulty.PEACEFUL;
		}
	}

	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {

		return args.length == 1 ? getListOfStringsMatchingLastWord(args, "peaceful", "easy", "normal", "hard") : Collections.emptyList();
	}

}
