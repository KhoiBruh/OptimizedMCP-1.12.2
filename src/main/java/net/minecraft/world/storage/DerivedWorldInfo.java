package net.minecraft.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

public class DerivedWorldInfo extends WorldInfo {

	/**
	 * Instance of WorldInfo.
	 */
	private final WorldInfo delegate;

	public DerivedWorldInfo(WorldInfo worldInfoIn) {

		delegate = worldInfoIn;
	}

	/**
	 * Creates a new NBTTagCompound for the world, with the given NBTTag as the "Player"
	 */
	public NBTTagCompound cloneNBTCompound(NBTTagCompound nbt) {

		return delegate.cloneNBTCompound(nbt);
	}

	/**
	 * Returns the seed of current world.
	 */
	public long getSeed() {

		return delegate.getSeed();
	}

	/**
	 * Returns the x spawn position
	 */
	public int getSpawnX() {

		return delegate.getSpawnX();
	}

	/**
	 * Set the x spawn position to the passed in value
	 */
	public void setSpawnX(int x) {

	}

	/**
	 * Return the Y axis spawning point of the player.
	 */
	public int getSpawnY() {

		return delegate.getSpawnY();
	}

	/**
	 * Sets the y spawn position
	 */
	public void setSpawnY(int y) {

	}

	/**
	 * Returns the z spawn position
	 */
	public int getSpawnZ() {

		return delegate.getSpawnZ();
	}

	/**
	 * Set the z spawn position to the passed in value
	 */
	public void setSpawnZ(int z) {

	}

	public long getWorldTotalTime() {

		return delegate.getWorldTotalTime();
	}

	public void setWorldTotalTime(long time) {

	}

	/**
	 * Get current world time
	 */
	public long getWorldTime() {

		return delegate.getWorldTime();
	}

	/**
	 * Set current world time
	 */
	public void setWorldTime(long time) {

	}

	public long getSizeOnDisk() {

		return delegate.getSizeOnDisk();
	}

	/**
	 * Returns the player's NBTTagCompound to be loaded
	 */
	public NBTTagCompound getPlayerNBTTagCompound() {

		return delegate.getPlayerNBTTagCompound();
	}

	/**
	 * Get current world name
	 */
	public String getWorldName() {

		return delegate.getWorldName();
	}

	public void setWorldName(String worldName) {

	}

	/**
	 * Returns the save version of this world
	 */
	public int getSaveVersion() {

		return delegate.getSaveVersion();
	}

	/**
	 * Sets the save version of the world
	 */
	public void setSaveVersion(int version) {

	}

	/**
	 * Return the last time the player was in this world.
	 */
	public long getLastTimePlayed() {

		return delegate.getLastTimePlayed();
	}

	/**
	 * Returns true if it is thundering, false otherwise.
	 */
	public boolean isThundering() {

		return delegate.isThundering();
	}

	/**
	 * Sets whether it is thundering or not.
	 */
	public void setThundering(boolean thunderingIn) {

	}

	/**
	 * Returns the number of ticks until next thunderbolt.
	 */
	public int getThunderTime() {

		return delegate.getThunderTime();
	}

	/**
	 * Defines the number of ticks until next thunderbolt.
	 */
	public void setThunderTime(int time) {

	}

	/**
	 * Returns true if it is raining, false otherwise.
	 */
	public boolean isRaining() {

		return delegate.isRaining();
	}

	/**
	 * Sets whether it is raining or not.
	 */
	public void setRaining(boolean isRaining) {

	}

	/**
	 * Return the number of ticks until rain.
	 */
	public int getRainTime() {

		return delegate.getRainTime();
	}

	/**
	 * Sets the number of ticks until rain.
	 */
	public void setRainTime(int time) {

	}

	/**
	 * Gets the GameType.
	 */
	public GameType getGameType() {

		return delegate.getGameType();
	}

	public void setSpawn(BlockPos spawnPoint) {

	}

	/**
	 * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
	 */
	public boolean isMapFeaturesEnabled() {

		return delegate.isMapFeaturesEnabled();
	}

	/**
	 * Returns true if hardcore mode is enabled, otherwise false
	 */
	public boolean isHardcoreModeEnabled() {

		return delegate.isHardcoreModeEnabled();
	}

	public WorldType getTerrainType() {

		return delegate.getTerrainType();
	}

	public void setTerrainType(WorldType type) {

	}

	/**
	 * Returns true if commands are allowed on this World.
	 */
	public boolean areCommandsAllowed() {

		return delegate.areCommandsAllowed();
	}

	public void setAllowCommands(boolean allow) {

	}

	/**
	 * Returns true if the World is initialized.
	 */
	public boolean isInitialized() {

		return delegate.isInitialized();
	}

	/**
	 * Sets the initialization status of the World.
	 */
	public void setServerInitialized(boolean initializedIn) {

	}

	/**
	 * Gets the GameRules class Instance.
	 */
	public GameRules getGameRulesInstance() {

		return delegate.getGameRulesInstance();
	}

	public EnumDifficulty getDifficulty() {

		return delegate.getDifficulty();
	}

	public void setDifficulty(EnumDifficulty newDifficulty) {

	}

	public boolean isDifficultyLocked() {

		return delegate.isDifficultyLocked();
	}

	public void setDifficultyLocked(boolean locked) {

	}

	public void setDimensionData(DimensionType dimensionIn, NBTTagCompound compound) {

		delegate.setDimensionData(dimensionIn, compound);
	}

	public NBTTagCompound getDimensionData(DimensionType dimensionIn) {

		return delegate.getDimensionData(dimensionIn);
	}

}
