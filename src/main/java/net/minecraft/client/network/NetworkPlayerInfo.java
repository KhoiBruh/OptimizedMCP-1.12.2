package net.minecraft.client.network;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import java.util.Map;

public class NetworkPlayerInfo {

	/**
	 * The GameProfile for the player represented by this NetworkPlayerInfo instance
	 */
	private final GameProfile gameProfile;
	Map<Type, ResourceLocation> playerTextures = Maps.newEnumMap(Type.class);
	private GameType gameType;

	/**
	 * Player response time to server in milliseconds
	 */
	private int responseTime;
	private boolean playerTexturesLoaded;
	private String skinType;

	/**
	 * When this is non-null, it is displayed instead of the player's real name
	 */
	private ITextComponent displayName;
	private int lastHealth;
	private int displayHealth;
	private long lastHealthTime;
	private long healthBlinkTime;
	private long renderVisibilityId;

	public NetworkPlayerInfo(GameProfile profile) {

		gameProfile = profile;
	}

	public NetworkPlayerInfo(SPacketPlayerListItem.AddPlayerData entry) {

		gameProfile = entry.getProfile();
		gameType = entry.getGameMode();
		responseTime = entry.getPing();
		displayName = entry.getDisplayName();
	}

	/**
	 * Returns the GameProfile for the player represented by this NetworkPlayerInfo instance
	 */
	public GameProfile getGameProfile() {

		return gameProfile;
	}

	public GameType getGameType() {

		return gameType;
	}

	protected void setGameType(GameType gameMode) {

		gameType = gameMode;
	}

	public int getResponseTime() {

		return responseTime;
	}

	protected void setResponseTime(int latency) {

		responseTime = latency;
	}

	public boolean hasLocationSkin() {

		return getLocationSkin() != null;
	}

	public String getSkinType() {

		return skinType == null ? DefaultPlayerSkin.getSkinType(gameProfile.getId()) : skinType;
	}

	public ResourceLocation getLocationSkin() {

		loadPlayerTextures();
		return MoreObjects.firstNonNull(playerTextures.get(Type.SKIN), DefaultPlayerSkin.getDefaultSkin(gameProfile.getId()));
	}

	
	public ResourceLocation getLocationCape() {

		loadPlayerTextures();
		return playerTextures.get(Type.CAPE);
	}

	

	/**
	 * Gets the special Elytra texture for the player.
	 */
	public ResourceLocation getLocationElytra() {

		loadPlayerTextures();
		return playerTextures.get(Type.ELYTRA);
	}

	
	public ScorePlayerTeam getPlayerTeam() {

		return Minecraft.getMinecraft().world.getScoreboard().getPlayersTeam(getGameProfile().getName());
	}

	protected void loadPlayerTextures() {

		synchronized (this) {
			if (!playerTexturesLoaded) {
				playerTexturesLoaded = true;
				Minecraft.getMinecraft().getSkinManager().loadProfileTextures(gameProfile, (typeIn, location, profileTexture) -> {

					switch (typeIn) {
						case SKIN:
							playerTextures.put(Type.SKIN, location);
							skinType = profileTexture.getMetadata("model");

							if (skinType == null) {
								skinType = "default";
							}

							break;

						case CAPE:
							playerTextures.put(Type.CAPE, location);
							break;

						case ELYTRA:
							playerTextures.put(Type.ELYTRA, location);
					}
				}, true);
			}
		}
	}

	
	public ITextComponent getDisplayName() {

		return displayName;
	}

	public void setDisplayName(ITextComponent displayNameIn) {

		displayName = displayNameIn;
	}

	public int getLastHealth() {

		return lastHealth;
	}

	public void setLastHealth(int p_178836_1_) {

		lastHealth = p_178836_1_;
	}

	public int getDisplayHealth() {

		return displayHealth;
	}

	public void setDisplayHealth(int p_178857_1_) {

		displayHealth = p_178857_1_;
	}

	public long getLastHealthTime() {

		return lastHealthTime;
	}

	public void setLastHealthTime(long p_178846_1_) {

		lastHealthTime = p_178846_1_;
	}

	public long getHealthBlinkTime() {

		return healthBlinkTime;
	}

	public void setHealthBlinkTime(long p_178844_1_) {

		healthBlinkTime = p_178844_1_;
	}

	public long getRenderVisibilityId() {

		return renderVisibilityId;
	}

	public void setRenderVisibilityId(long p_178843_1_) {

		renderVisibilityId = p_178843_1_;
	}

}
