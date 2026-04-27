package net.minecraft.command;

public class PlayerNotFoundException extends CommandException {

	public PlayerNotFoundException(String message) {

		super(message);
	}

	public PlayerNotFoundException(String message, Object... replacements) {

		super(message, replacements);
	}

}
