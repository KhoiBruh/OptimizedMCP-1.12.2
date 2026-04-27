package net.minecraft.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class CreativeSettings {

	private static final Logger LOGGER = LogManager.getLogger();
	private final File dataFile;
	private final HotbarSnapshot[] hotbarSnapshots = new HotbarSnapshot[9];
	protected Minecraft minecraft;

	public CreativeSettings(Minecraft minecraftIn, File dataDir) {

		minecraft = minecraftIn;
		dataFile = new File(dataDir, "hotbar.nbt");

		for (int i = 0; i < 9; ++i) {
			hotbarSnapshots[i] = new HotbarSnapshot();
		}

		read();
	}

	public void read() {

		try {
			NBTTagCompound nbttagcompound = CompressedStreamTools.read(dataFile);

			if (nbttagcompound == null) {
				return;
			}

			for (int i = 0; i < 9; ++i) {
				hotbarSnapshots[i].fromTag(nbttagcompound.getTagList(String.valueOf(i), 10));
			}
		} catch (Exception exception) {
			LOGGER.error("Failed to load creative mode options", exception);
		}
	}

	public void write() {

		try {
			NBTTagCompound nbttagcompound = new NBTTagCompound();

			for (int i = 0; i < 9; ++i) {
				nbttagcompound.setTag(String.valueOf(i), hotbarSnapshots[i].createTag());
			}

			CompressedStreamTools.write(nbttagcompound, dataFile);
		} catch (Exception exception) {
			LOGGER.error("Failed to save creative mode options", exception);
		}
	}

	public HotbarSnapshot getHotbarSnapshot(int p_192563_1_) {

		return hotbarSnapshots[p_192563_1_];
	}

}
