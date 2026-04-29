package net.minecraft.command;

import com.google.common.collect.Lists;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.util.ResourceLocation;
import java.util.ArrayDeque;
import java.util.List;

public record FunctionObject(Entry[] entries) {

	/**
	 * Create a Function from the given function definition.
	 */
	public static FunctionObject create(FunctionManager functionManagerIn, List<String> commands) {

		List<Entry> list = Lists.newArrayListWithCapacity(commands.size());

		for (String s : commands) {
			s = s.trim();

			if (!s.startsWith("#") && !s.isEmpty()) {
				String[] astring = s.split(" ", 2);
				String s1 = astring[0];

				if (!functionManagerIn.getCommandManager().getCommands().containsKey(s1)) {
					if (s1.startsWith("//")) {
						throw new IllegalArgumentException("Unknown or invalid command '" + s1 + "' (if you intended to make a comment, use '#' not '//')");
					}

					if (s1.startsWith("/") && s1.length() > 1) {
						throw new IllegalArgumentException("Unknown or invalid command '" + s1 + "' (did you mean '" + s1.substring(1) + "'? Do not use a preceding forwards slash.)");
					}

					throw new IllegalArgumentException("Unknown or invalid command '" + s1 + "'");
				}

				list.add(new CommandEntry(s));
			}
		}

		return new FunctionObject(list.toArray(new Entry[0]));
	}

	public interface Entry {

		void execute(FunctionManager functionManagerIn, ICommandSender sender, ArrayDeque<FunctionManager.QueuedCommand> commandQueue, int maxCommandChainLength);

	}

	public static class CacheableFunction {

		public static final CacheableFunction EMPTY = new CacheableFunction((ResourceLocation) null);

		
		private final ResourceLocation id;
		private boolean isValid;
		private FunctionObject function;

		public CacheableFunction(ResourceLocation idIn) {

			id = idIn;
		}

		public CacheableFunction(FunctionObject functionIn) {

			id = null;
			function = functionIn;
		}

		
		public FunctionObject get(FunctionManager functionManagerIn) {

			if (!isValid) {
				if (id != null) {
					function = functionManagerIn.getFunction(id);
				}

				isValid = true;
			}

			return function;
		}

		public String toString() {

			return String.valueOf(id);
		}

	}

	public static class CommandEntry implements Entry {

		private final String command;

		public CommandEntry(String p_i47534_1_) {

			command = p_i47534_1_;
		}

		public void execute(FunctionManager functionManagerIn, ICommandSender sender, ArrayDeque<FunctionManager.QueuedCommand> commandQueue, int maxCommandChainLength) {

			functionManagerIn.getCommandManager().executeCommand(sender, command);
		}

		public String toString() {

			return "/" + command;
		}

	}

	public static class FunctionEntry implements Entry {

		private final CacheableFunction function;

		public FunctionEntry(FunctionObject functionIn) {

			function = new CacheableFunction(functionIn);
		}

		public void execute(FunctionManager functionManagerIn, ICommandSender sender, ArrayDeque<FunctionManager.QueuedCommand> commandQueue, int maxCommandChainLength) {

			FunctionObject functionobject = function.get(functionManagerIn);

			if (functionobject != null) {
				Entry[] afunctionobject$entry = functionobject.entries();
				int i = maxCommandChainLength - commandQueue.size();
				int j = Math.min(afunctionobject$entry.length, i);

				for (int k = j - 1; k >= 0; --k) {
					commandQueue.addFirst(new FunctionManager.QueuedCommand(functionManagerIn, sender, afunctionobject$entry[k]));
				}
			}
		}

		public String toString() {

			return "/function " + function;
		}

	}

}
