package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.command.FunctionObject;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FunctionManager implements ITickable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final File functionDir;
    private final MinecraftServer server;
    private final Map<ResourceLocation, FunctionObject> functions = Maps.<ResourceLocation, FunctionObject>newHashMap();
    private String currentGameLoopFunctionId = "-";
    private FunctionObject gameLoopFunction;
    private final ArrayDeque<FunctionManager.QueuedCommand> commandQueue = new ArrayDeque<FunctionManager.QueuedCommand>();
    private boolean isExecuting = false;
    private final ICommandSender gameLoopFunctionSender = new ICommandSender()
    {
        public String getName()
        {
            return currentGameLoopFunctionId;
        }
        public boolean canUseCommand(int permLevel, String commandName)
        {
            return permLevel <= 2;
        }
        public World getEntityWorld()
        {
            return server.worlds[0];
        }
        public MinecraftServer getServer()
        {
            return server;
        }
    };

    public FunctionManager(@Nullable File functionDirIn, MinecraftServer serverIn)
    {
        functionDir = functionDirIn;
        server = serverIn;
        reload();
    }

    @Nullable
    public FunctionObject getFunction(ResourceLocation id)
    {
        return functions.get(id);
    }

    public ICommandManager getCommandManager()
    {
        return server.getCommandManager();
    }

    public int getMaxCommandChainLength()
    {
        return server.worlds[0].getGameRules().getInt("maxCommandChainLength");
    }

    public Map<ResourceLocation, FunctionObject> getFunctions()
    {
        return functions;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        String s = server.worlds[0].getGameRules().getString("gameLoopFunction");

        if (!s.equals(currentGameLoopFunctionId))
        {
            currentGameLoopFunctionId = s;
            gameLoopFunction = getFunction(new ResourceLocation(s));
        }

        if (gameLoopFunction != null)
        {
            execute(gameLoopFunction, gameLoopFunctionSender);
        }
    }

    public int execute(FunctionObject function, ICommandSender sender)
    {
        int i = getMaxCommandChainLength();

        if (isExecuting)
        {
            if (commandQueue.size() < i)
            {
                commandQueue.addFirst(new FunctionManager.QueuedCommand(this, sender, new FunctionObject.FunctionEntry(function)));
            }

            return 0;
        }
        else
        {
            int l;

            try
            {
                isExecuting = true;
                int j = 0;
                FunctionObject.Entry[] afunctionobject$entry = function.getEntries();

                for (int k = afunctionobject$entry.length - 1; k >= 0; --k)
                {
                    commandQueue.push(new FunctionManager.QueuedCommand(this, sender, afunctionobject$entry[k]));
                }

                while (true)
                {
                    if (commandQueue.isEmpty())
                    {
                        l = j;
                        return l;
                    }

                    (commandQueue.removeFirst()).execute(commandQueue, i);
                    ++j;

                    if (j >= i)
                    {
                        break;
                    }
                }

                l = j;
            }
            finally
            {
                commandQueue.clear();
                isExecuting = false;
            }

            return l;
        }
    }

    public void reload()
    {
        functions.clear();
        gameLoopFunction = null;
        currentGameLoopFunctionId = "-";
        loadFunctions();
    }

    private void loadFunctions()
    {
        if (functionDir != null)
        {
            functionDir.mkdirs();

            for (File file1 : FileUtils.listFiles(functionDir, new String[] {"mcfunction"}, true))
            {
                String s = FilenameUtils.removeExtension(functionDir.toURI().relativize(file1.toURI()).toString());
                String[] astring = s.split("/", 2);

                if (astring.length == 2)
                {
                    ResourceLocation resourcelocation = new ResourceLocation(astring[0], astring[1]);

                    try
                    {
                        functions.put(resourcelocation, FunctionObject.create(this, Files.readLines(file1, StandardCharsets.UTF_8)));
                    }
                    catch (Throwable throwable)
                    {
                        LOGGER.error("Couldn't read custom function " + resourcelocation + " from " + file1, throwable);
                    }
                }
            }

            if (!functions.isEmpty())
            {
                LOGGER.info("Loaded " + functions.size() + " custom command functions");
            }
        }
    }

    public static class QueuedCommand
    {
        private final FunctionManager functionManager;
        private final ICommandSender sender;
        private final FunctionObject.Entry entry;

        public QueuedCommand(FunctionManager functionManagerIn, ICommandSender senderIn, FunctionObject.Entry entryIn)
        {
            functionManager = functionManagerIn;
            sender = senderIn;
            entry = entryIn;
        }

        public void execute(ArrayDeque<FunctionManager.QueuedCommand> commandQueue, int maxCommandChainLength)
        {
            entry.execute(functionManager, sender, commandQueue, maxCommandChainLength);
        }

        public String toString()
        {
            return entry.toString();
        }
    }
}
