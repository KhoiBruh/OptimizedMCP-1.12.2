package net.minecraft.client.gui.game;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.chat.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.HandSide;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.border.WorldBorder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GuiIngame extends Gui {

	private static final ResourceLocation VIGNETTE_TEX_PATH = new ResourceLocation("textures/misc/vignette.png");
	private static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");
	private static final ResourceLocation PUMPKIN_BLUR_TEX_PATH = new ResourceLocation("textures/misc/pumpkinblur.png");
	private final Random rand = new Random();
	private final Minecraft mc;
	private final RenderItem itemRenderer;

	/**
	 * ChatGUI instance that retains all previous chat data
	 */
	private final GuiNewChat persistantChatGUI;
	private final GuiOverlayDebug overlayDebug;
	private final GuiSubtitleOverlay overlaySubtitle;
	/**
	 * The spectator GUI for this in-game GUI instance
	 */
	private final GuiSpectator spectatorGui;
	private final GuiPlayerTabOverlay overlayPlayerList;
	private final GuiBossOverlay overlayBoss;
	private final Map<ChatType, List<IChatListener>> chatListeners = Maps.newHashMap();
	/**
	 * Previous frame vignette brightness (slowly changes by 1% each frame)
	 */
	public float prevVignetteBrightness = 1F;
	private int updateCounter;
	/**
	 * The string specifying which record music is playing
	 */
	private String overlayMessage = "";
	/**
	 * How many ticks the record playing message will be displayed
	 */
	private int overlayMessageTime;
	private boolean animateOverlayMessageColor;
	/**
	 * Remaining ticks the item highlight should be visible
	 */
	private int remainingHighlightTicks;
	/**
	 * The ItemStack that is currently being highlighted
	 */
	private ItemStack highlightingItemStack = ItemStack.EMPTY;
	/**
	 * A timer for the current title and subtitle displayed
	 */
	private int titlesTimer;
	/**
	 * The current title displayed
	 */
	private String displayedTitle = "";
	/**
	 * The current sub-title displayed
	 */
	private String displayedSubTitle = "";
	/**
	 * The time that the title take to fade in
	 */
	private int titleFadeIn;
	/**
	 * The time that the title is display
	 */
	private int titleDisplayTime;
	/**
	 * The time that the title take to fade out
	 */
	private int titleFadeOut;
	private int playerHealth;
	private int lastPlayerHealth;
	/**
	 * The last recorded system time
	 */
	private long lastSystemTime;
	/**
	 * Used with updateCounter to make the heart bar flash
	 */
	private long healthUpdateCounter;

	public GuiIngame(Minecraft mcIn) {

		mc = mcIn;
		itemRenderer = mcIn.getRenderItem();
		overlayDebug = new GuiOverlayDebug(mcIn);
		spectatorGui = new GuiSpectator(mcIn);
		persistantChatGUI = new GuiNewChat(mcIn);
		overlayPlayerList = new GuiPlayerTabOverlay(mcIn, this);
		overlayBoss = new GuiBossOverlay(mcIn);
		overlaySubtitle = new GuiSubtitleOverlay(mcIn);

		for (ChatType chattype : ChatType.values()) {
			chatListeners.put(chattype, Lists.newArrayList());
		}

		IChatListener ichatlistener = NarratorChatListener.INSTANCE;
		(chatListeners.get(ChatType.CHAT)).add(new NormalChatListener(mcIn));
		(chatListeners.get(ChatType.CHAT)).add(ichatlistener);
		(chatListeners.get(ChatType.SYSTEM)).add(new NormalChatListener(mcIn));
		(chatListeners.get(ChatType.SYSTEM)).add(ichatlistener);
		(chatListeners.get(ChatType.GAME_INFO)).add(new OverlayChatListener(mcIn));
		setDefaultTitlesTimes();
	}

	/**
	 * Set the differents times for the titles to their default values
	 */
	public void setDefaultTitlesTimes() {

		titleFadeIn = 10;
		titleDisplayTime = 70;
		titleFadeOut = 20;
	}

	public void renderGameOverlay(float partialTicks) {

		ScaledResolution scaledResolution = mc.scaledResolution;
		int i = scaledResolution.getScaledWidth();
		int j = scaledResolution.getScaledHeight();
		FontRenderer fontrenderer = getFontRenderer();
		GlStateManager.enableBlend();

		if (Minecraft.isFancyGraphicsEnabled()) {
			renderVignette(mc.player.getBrightness(), scaledResolution);
		} else {
			GlStateManager.enableDepth();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		}

		ItemStack itemstack = mc.player.inventory.armorItemInSlot(3);

		if (mc.gameSettings.thirdPersonView == 0 && itemstack.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
			renderPumpkinOverlay(scaledResolution);
		}

		if (!mc.player.isPotionActive(MobEffects.NAUSEA)) {
			float f = mc.player.prevTimeInPortal + (mc.player.timeInPortal - mc.player.prevTimeInPortal) * partialTicks;

			if (f > 0F) {
				renderPortal(f, scaledResolution);
			}
		}

		if (mc.playerController.isSpectator()) {
			spectatorGui.renderTooltip(scaledResolution, partialTicks);
		} else {
			renderHotbar(scaledResolution, partialTicks);
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(ICONS);
		GlStateManager.enableBlend();
		renderAttackIndicator(partialTicks, scaledResolution);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		mc.profiler.startSection("bossHealth");
		overlayBoss.renderBossHealth();
		mc.profiler.endSection();
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(ICONS);

		if (mc.playerController.shouldDrawHUD()) {
			renderPlayerStats(scaledResolution);
		}

		renderMountHealth(scaledResolution);
		GlStateManager.disableBlend();

		if (mc.player.getSleepTimer() > 0) {
			mc.profiler.startSection("sleep");
			GlStateManager.disableDepth();
			GlStateManager.disableAlpha();
			int j1 = mc.player.getSleepTimer();
			float f1 = (float) j1 / 100F;

			if (f1 > 1F) {
				f1 = 1F - (float) (j1 - 100) / 10F;
			}

			int k = (int) (220F * f1) << 24 | 1052704;
			drawRect(0, 0, i, j, k);
			GlStateManager.enableAlpha();
			GlStateManager.enableDepth();
			mc.profiler.endSection();
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
		int k1 = i / 2 - 91;

		if (mc.player.isRidingHorse()) {
			renderHorseJumpBar(scaledResolution, k1);
		} else if (mc.playerController.gameIsSurvivalOrAdventure()) {
			renderExpBar(scaledResolution, k1);
		}

		if (mc.gameSettings.heldItemTooltips && !mc.playerController.isSpectator()) {
			renderSelectedItem(scaledResolution);
		} else if (mc.player.isSpectator()) {
			spectatorGui.renderSelectedItem(scaledResolution);
		}

		renderPotionEffects(scaledResolution);

		if (mc.gameSettings.showDebugInfo) {
			overlayDebug.renderDebugInfo(scaledResolution);
		}

		if (overlayMessageTime > 0) {
			mc.profiler.startSection("overlayMessage");
			float f2 = (float) overlayMessageTime - partialTicks;
			int l1 = (int) (f2 * 255F / 20F);

			if (l1 > 255) {
				l1 = 255;
			}

			if (l1 > 8) {
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) (i / 2), (float) (j - 68), 0F);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				int l = 16777215;

				if (animateOverlayMessageColor) {
					l = MathHelper.hsvToRGB(f2 / 50F, 0.7F, 0.6F) & 16777215;
				}

				fontrenderer.drawString(overlayMessage, -fontrenderer.getStringWidth(overlayMessage) / 2, -4, l + (l1 << 24 & -16777216));
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}

			mc.profiler.endSection();
		}

		overlaySubtitle.renderSubtitles(scaledResolution);

		if (titlesTimer > 0) {
			mc.profiler.startSection("titleAndSubtitle");
			float f3 = (float) titlesTimer - partialTicks;
			int i2 = 255;

			if (titlesTimer > titleFadeOut + titleDisplayTime) {
				float f4 = (float) (titleFadeIn + titleDisplayTime + titleFadeOut) - f3;
				i2 = (int) (f4 * 255F / (float) titleFadeIn);
			}

			if (titlesTimer <= titleFadeOut) {
				i2 = (int) (f3 * 255F / (float) titleFadeOut);
			}

			i2 = MathHelper.clamp(i2, 0, 255);

			if (i2 > 8) {
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) (i / 2), (float) (j / 2), 0F);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.pushMatrix();
				GlStateManager.scale(4F, 4F, 4F);
				int j2 = i2 << 24 & -16777216;
				fontrenderer.drawString(displayedTitle, (float) (-fontrenderer.getStringWidth(displayedTitle) / 2), -10F, 16777215 | j2, true);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.scale(2F, 2F, 2F);
				fontrenderer.drawString(displayedSubTitle, (float) (-fontrenderer.getStringWidth(displayedSubTitle) / 2), 5F, 16777215 | j2, true);
				GlStateManager.popMatrix();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}

			mc.profiler.endSection();
		}

		Scoreboard scoreboard = mc.world.getScoreboard();
		ScoreObjective scoreobjective = null;
		ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(mc.player.getName());

		if (scoreplayerteam != null) {
			int i1 = scoreplayerteam.getColor().getColorIndex();

			if (i1 >= 0) {
				scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
			}
		}

		ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

		if (scoreobjective1 != null) {
			renderScoreboard(scoreobjective1, scaledResolution);
		}

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, (float) (j - 48), 0F);
		mc.profiler.startSection("chat");
		persistantChatGUI.drawChat(updateCounter);
		mc.profiler.endSection();
		GlStateManager.popMatrix();
		scoreobjective1 = scoreboard.getObjectiveInDisplaySlot(0);

		if (!mc.gameSettings.keyBindPlayerList.isKeyDown() || mc.isIntegratedServerRunning() && mc.player.connection.getPlayerInfoMap().size() <= 1 && scoreobjective1 == null) {
			overlayPlayerList.updatePlayerList(false);
		} else {
			overlayPlayerList.updatePlayerList(true);
			overlayPlayerList.renderPlayerlist(i, scoreboard, scoreobjective1);
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
	}

	private void renderAttackIndicator(float p_184045_1_, ScaledResolution p_184045_2_) {

		GameSettings gamesettings = mc.gameSettings;

		if (gamesettings.thirdPersonView == 0) {
			if (mc.playerController.isSpectator() && mc.pointedEntity == null) {
				RayTraceResult raytraceresult = mc.objectMouseOver;

				if (raytraceresult == null || raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
					return;
				}

				BlockPos blockpos = raytraceresult.getBlockPos();

				if (!mc.world.getBlockState(blockpos).getBlock().hasTileEntity() || !(mc.world.getTileEntity(blockpos) instanceof IInventory)) {
					return;
				}
			}

			int l = p_184045_2_.getScaledWidth();
			int i1 = p_184045_2_.getScaledHeight();

			if (gamesettings.showDebugInfo && !gamesettings.hideGUI && !mc.player.hasReducedDebug() && !gamesettings.reducedDebugInfo) {
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) (l / 2), (float) (i1 / 2), zLevel);
				Entity entity = mc.getRenderViewEntity();
				GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * p_184045_1_, -1F, 0F, 0F);
				GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * p_184045_1_, 0F, 1F, 0F);
				GlStateManager.scale(-1F, -1F, -1F);
				OpenGlHelper.renderDirections(10);
				GlStateManager.popMatrix();
			} else {
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.enableAlpha();
				drawTexturedModalRect(l / 2 - 7, i1 / 2 - 7, 0, 0, 16, 16);

				if (mc.gameSettings.attackIndicator == 1) {
					float f = mc.player.getCooledAttackStrength(0F);
					boolean flag = false;

					if (mc.pointedEntity != null && mc.pointedEntity instanceof EntityLivingBase && f >= 1F) {
						flag = mc.player.getCooldownPeriod() > 5F;
						flag = flag & mc.pointedEntity.isEntityAlive();
					}

					int i = i1 / 2 - 7 + 16;
					int j = l / 2 - 8;

					if (flag) {
						drawTexturedModalRect(j, i, 68, 94, 16, 16);
					} else if (f < 1F) {
						int k = (int) (f * 17F);
						drawTexturedModalRect(j, i, 36, 94, 16, 4);
						drawTexturedModalRect(j, i, 52, 94, k, 4);
					}
				}
			}
		}
	}

	protected void renderPotionEffects(ScaledResolution resolution) {

		Collection<PotionEffect> collection = mc.player.getActivePotionEffects();

		if (!collection.isEmpty()) {
			mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
			GlStateManager.enableBlend();
			int i = 0;
			int j = 0;

			for (PotionEffect potioneffect : Ordering.natural().reverse().sortedCopy(collection)) {
				Potion potion = potioneffect.getPotion();

				if (potion.hasStatusIcon() && potioneffect.doesShowParticles()) {
					int k = resolution.getScaledWidth();
					int l = 1;

					int i1 = potion.getStatusIconIndex();

					if (potion.isBeneficial()) {
						++i;
						k = k - 25 * i;
					} else {
						++j;
						k = k - 25 * j;
						l += 26;
					}

					GlStateManager.color(1F, 1F, 1F, 1F);
					float f = 1F;

					if (potioneffect.getIsAmbient()) {
						drawTexturedModalRect(k, l, 165, 166, 24, 24);
					} else {
						drawTexturedModalRect(k, l, 141, 166, 24, 24);

						if (potioneffect.getDuration() <= 200) {
							int j1 = 10 - potioneffect.getDuration() / 20;
							f = MathHelper.clamp((float) potioneffect.getDuration() / 10F / 5F * 0.5F, 0F, 0.5F) + MathHelper.cos((float) potioneffect.getDuration() * (float) Math.PI / 5F) * MathHelper.clamp((float) j1 / 10F * 0.25F, 0F, 0.25F);
						}
					}

					GlStateManager.color(1F, 1F, 1F, f);
					drawTexturedModalRect(k + 3, l + 3, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
				}
			}
		}
	}

	protected void renderHotbar(ScaledResolution sr, float partialTicks) {

		if (mc.getRenderViewEntity() instanceof EntityPlayer entityplayer) {
			GlStateManager.color(1F, 1F, 1F, 1F);
			mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);
			ItemStack itemstack = entityplayer.getHeldItemOffhand();
			HandSide enumhandside = entityplayer.getPrimaryHand().opposite();
			int i = sr.getScaledWidth() / 2;
			float f = zLevel;
			int j = 182;
			int k = 91;
			zLevel = -90F;
			drawTexturedModalRect(i - 91, sr.getScaledHeight() - 22, 0, 0, 182, 22);
			drawTexturedModalRect(i - 91 - 1 + entityplayer.inventory.currentItem * 20, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);

			if (!itemstack.isEmpty()) {
				if (enumhandside == HandSide.LEFT) {
					drawTexturedModalRect(i - 91 - 29, sr.getScaledHeight() - 23, 24, 22, 29, 24);
				} else {
					drawTexturedModalRect(i + 91, sr.getScaledHeight() - 23, 53, 22, 29, 24);
				}
			}

			zLevel = f;
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderHelper.enableGUIStandardItemLighting();

			for (int l = 0; l < 9; ++l) {
				int i1 = i - 90 + l * 20 + 2;
				int j1 = sr.getScaledHeight() - 16 - 3;
				renderHotbarItem(i1, j1, partialTicks, entityplayer, entityplayer.inventory.mainInventory.get(l));
			}

			if (!itemstack.isEmpty()) {
				int l1 = sr.getScaledHeight() - 16 - 3;

				if (enumhandside == HandSide.LEFT) {
					renderHotbarItem(i - 91 - 26, l1, partialTicks, entityplayer, itemstack);
				} else {
					renderHotbarItem(i + 91 + 10, l1, partialTicks, entityplayer, itemstack);
				}
			}

			if (mc.gameSettings.attackIndicator == 2) {
				float f1 = mc.player.getCooledAttackStrength(0F);

				if (f1 < 1F) {
					int i2 = sr.getScaledHeight() - 20;
					int j2 = i + 91 + 6;

					if (enumhandside == HandSide.RIGHT) {
						j2 = i - 91 - 22;
					}

					mc.getTextureManager().bindTexture(Gui.ICONS);
					int k1 = (int) (f1 * 19F);
					GlStateManager.color(1F, 1F, 1F, 1F);
					drawTexturedModalRect(j2, i2, 0, 94, 18, 18);
					drawTexturedModalRect(j2, i2 + 18 - k1, 18, 112 - k1, 18, k1);
				}
			}

			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
		}
	}

	public void renderHorseJumpBar(ScaledResolution scaledRes, int x) {

		mc.profiler.startSection("jumpBar");
		mc.getTextureManager().bindTexture(Gui.ICONS);
		float f = mc.player.getHorseJumpPower();
		int i = 182;
		int j = (int) (f * 183F);
		int k = scaledRes.getScaledHeight() - 32 + 3;
		drawTexturedModalRect(x, k, 0, 84, 182, 5);

		if (j > 0) {
			drawTexturedModalRect(x, k, 0, 89, j, 5);
		}

		mc.profiler.endSection();
	}

	public void renderExpBar(ScaledResolution scaledRes, int x) {

		mc.profiler.startSection("expBar");
		mc.getTextureManager().bindTexture(Gui.ICONS);
		int i = mc.player.xpBarCap();

		if (i > 0) {
			int j = 182;
			int k = (int) (mc.player.experience * 183F);
			int l = scaledRes.getScaledHeight() - 32 + 3;
			drawTexturedModalRect(x, l, 0, 64, 182, 5);

			if (k > 0) {
				drawTexturedModalRect(x, l, 0, 69, k, 5);
			}
		}

		mc.profiler.endSection();

		if (mc.player.experienceLevel > 0) {
			mc.profiler.startSection("expLevel");
			String s = "" + mc.player.experienceLevel;
			int i1 = (scaledRes.getScaledWidth() - getFontRenderer().getStringWidth(s)) / 2;
			int j1 = scaledRes.getScaledHeight() - 31 - 4;
			getFontRenderer().drawString(s, i1 + 1, j1, 0);
			getFontRenderer().drawString(s, i1 - 1, j1, 0);
			getFontRenderer().drawString(s, i1, j1 + 1, 0);
			getFontRenderer().drawString(s, i1, j1 - 1, 0);
			getFontRenderer().drawString(s, i1, j1, 8453920);
			mc.profiler.endSection();
		}
	}

	public void renderSelectedItem(ScaledResolution scaledRes) {

		mc.profiler.startSection("selectedItemName");

		if (remainingHighlightTicks > 0 && !highlightingItemStack.isEmpty()) {
			String s = highlightingItemStack.getDisplayName();

			if (highlightingItemStack.hasDisplayName()) {
				s = TextFormatting.ITALIC + s;
			}

			int i = (scaledRes.getScaledWidth() - getFontRenderer().getStringWidth(s)) / 2;
			int j = scaledRes.getScaledHeight() - 59;

			if (!mc.playerController.shouldDrawHUD()) {
				j += 14;
			}

			int k = (int) ((float) remainingHighlightTicks * 256F / 10F);

			if (k > 255) {
				k = 255;
			}

			if (k > 0) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				getFontRenderer().drawStringWithShadow(s, (float) i, (float) j, 16777215 + (k << 24));
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}

		mc.profiler.endSection();
	}

	private void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes) {

		Scoreboard scoreboard = objective.getScoreboard();
		Collection<Score> collection = scoreboard.getSortedScores(objective);
		List<Score> list = Lists.newArrayList(Iterables.filter(collection, p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")));

		if (list.size() > 15) {
			collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
		} else {
			collection = list;
		}

		int i = getFontRenderer().getStringWidth(objective.getDisplayName());

		for (Score score : collection) {
			ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
			String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + TextFormatting.RED + score.getScorePoints();
			i = Math.max(i, getFontRenderer().getStringWidth(s));
		}

		int i1 = collection.size() * getFontRenderer().FONT_HEIGHT;
		int j1 = scaledRes.getScaledHeight() / 2 + i1 / 3;
		int k1 = 3;
		int l1 = scaledRes.getScaledWidth() - i - 3;
		int j = 0;

		for (Score score1 : collection) {
			++j;
			ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
			String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
			String s2 = TextFormatting.RED + "" + score1.getScorePoints();
			int k = j1 - j * getFontRenderer().FONT_HEIGHT;
			int l = scaledRes.getScaledWidth() - 3 + 2;
			drawRect(l1 - 2, k, l, k + getFontRenderer().FONT_HEIGHT, 1342177280);
			getFontRenderer().drawString(s1, l1, k, 553648127);
			getFontRenderer().drawString(s2, l - getFontRenderer().getStringWidth(s2), k, 553648127);

			if (j == collection.size()) {
				String s3 = objective.getDisplayName();
				drawRect(l1 - 2, k - getFontRenderer().FONT_HEIGHT - 1, l, k - 1, 1610612736);
				drawRect(l1 - 2, k - 1, l, k, 1342177280);
				getFontRenderer().drawString(s3, l1 + i / 2 - getFontRenderer().getStringWidth(s3) / 2, k - getFontRenderer().FONT_HEIGHT, 553648127);
			}
		}
	}

	private void renderPlayerStats(ScaledResolution scaledRes) {

		if (mc.getRenderViewEntity() instanceof EntityPlayer entityplayer) {
			int i = MathHelper.ceil(entityplayer.getHealth());
			boolean flag = healthUpdateCounter > (long) updateCounter && (healthUpdateCounter - (long) updateCounter) / 3L % 2L == 1L;

			if (i < playerHealth && entityplayer.hurtResistantTime > 0) {
				lastSystemTime = Minecraft.getSystemTime();
				healthUpdateCounter = updateCounter + 20;
			} else if (i > playerHealth && entityplayer.hurtResistantTime > 0) {
				lastSystemTime = Minecraft.getSystemTime();
				healthUpdateCounter = updateCounter + 10;
			}

			if (Minecraft.getSystemTime() - lastSystemTime > 1000L) {
				playerHealth = i;
				lastPlayerHealth = i;
				lastSystemTime = Minecraft.getSystemTime();
			}

			playerHealth = i;
			int j = lastPlayerHealth;
			rand.setSeed(updateCounter * 312871L);
			FoodStats foodstats = entityplayer.getFoodStats();
			int k = foodstats.getFoodLevel();
			IAttributeInstance iattributeinstance = entityplayer.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
			int l = scaledRes.getScaledWidth() / 2 - 91;
			int i1 = scaledRes.getScaledWidth() / 2 + 91;
			int j1 = scaledRes.getScaledHeight() - 39;
			float f = (float) iattributeinstance.getAttributeValue();
			int k1 = MathHelper.ceil(entityplayer.getAbsorptionAmount());
			int l1 = MathHelper.ceil((f + (float) k1) / 2F / 10F);
			int i2 = Math.max(10 - (l1 - 2), 3);
			int j2 = j1 - (l1 - 1) * i2 - 10;
			int k2 = j1 - 10;
			int l2 = k1;
			int i3 = entityplayer.getTotalArmorValue();
			int j3 = -1;

			if (entityplayer.isPotionActive(MobEffects.REGENERATION)) {
				j3 = updateCounter % MathHelper.ceil(f + 5F);
			}

			mc.profiler.startSection("armor");

			for (int k3 = 0; k3 < 10; ++k3) {
				if (i3 > 0) {
					int l3 = l + k3 * 8;

					if (k3 * 2 + 1 < i3) {
						drawTexturedModalRect(l3, j2, 34, 9, 9, 9);
					}

					if (k3 * 2 + 1 == i3) {
						drawTexturedModalRect(l3, j2, 25, 9, 9, 9);
					}

					if (k3 * 2 + 1 > i3) {
						drawTexturedModalRect(l3, j2, 16, 9, 9, 9);
					}
				}
			}

			mc.profiler.endStartSection("health");

			for (int j5 = MathHelper.ceil((f + (float) k1) / 2F) - 1; j5 >= 0; --j5) {
				int k5 = 16;

				if (entityplayer.isPotionActive(MobEffects.POISON)) {
					k5 += 36;
				} else if (entityplayer.isPotionActive(MobEffects.WITHER)) {
					k5 += 72;
				}

				int i4 = 0;

				if (flag) {
					i4 = 1;
				}

				int j4 = MathHelper.ceil((float) (j5 + 1) / 10F) - 1;
				int k4 = l + j5 % 10 * 8;
				int l4 = j1 - j4 * i2;

				if (i <= 4) {
					l4 += rand.nextInt(2);
				}

				if (l2 <= 0 && j5 == j3) {
					l4 -= 2;
				}

				int i5 = 0;

				if (entityplayer.world.getWorldInfo().isHardcoreModeEnabled()) {
					i5 = 5;
				}

				drawTexturedModalRect(k4, l4, 16 + i4 * 9, 9 * i5, 9, 9);

				if (flag) {
					if (j5 * 2 + 1 < j) {
						drawTexturedModalRect(k4, l4, k5 + 54, 9 * i5, 9, 9);
					}

					if (j5 * 2 + 1 == j) {
						drawTexturedModalRect(k4, l4, k5 + 63, 9 * i5, 9, 9);
					}
				}

				if (l2 > 0) {
					if (l2 == k1 && k1 % 2 == 1) {
						drawTexturedModalRect(k4, l4, k5 + 153, 9 * i5, 9, 9);
						--l2;
					} else {
						drawTexturedModalRect(k4, l4, k5 + 144, 9 * i5, 9, 9);
						l2 -= 2;
					}
				} else {
					if (j5 * 2 + 1 < i) {
						drawTexturedModalRect(k4, l4, k5 + 36, 9 * i5, 9, 9);
					}

					if (j5 * 2 + 1 == i) {
						drawTexturedModalRect(k4, l4, k5 + 45, 9 * i5, 9, 9);
					}
				}
			}

			Entity entity = entityplayer.getRidingEntity();

			if (!(entity instanceof EntityLivingBase)) {
				mc.profiler.endStartSection("food");

				for (int l5 = 0; l5 < 10; ++l5) {
					int j6 = j1;
					int l6 = 16;
					int j7 = 0;

					if (entityplayer.isPotionActive(MobEffects.HUNGER)) {
						l6 += 36;
						j7 = 13;
					}

					if (entityplayer.getFoodStats().getSaturationLevel() <= 0F && updateCounter % (k * 3 + 1) == 0) {
						j6 = j1 + (rand.nextInt(3) - 1);
					}

					int l7 = i1 - l5 * 8 - 9;
					drawTexturedModalRect(l7, j6, 16 + j7 * 9, 27, 9, 9);

					if (l5 * 2 + 1 < k) {
						drawTexturedModalRect(l7, j6, l6 + 36, 27, 9, 9);
					}

					if (l5 * 2 + 1 == k) {
						drawTexturedModalRect(l7, j6, l6 + 45, 27, 9, 9);
					}
				}
			}

			mc.profiler.endStartSection("air");

			if (entityplayer.isInsideOfMaterial(Material.WATER)) {
				int i6 = mc.player.getAir();
				int k6 = MathHelper.ceil((double) (i6 - 2) * 10D / 300D);
				int i7 = MathHelper.ceil((double) i6 * 10D / 300D) - k6;

				for (int k7 = 0; k7 < k6 + i7; ++k7) {
					if (k7 < k6) {
						drawTexturedModalRect(i1 - k7 * 8 - 9, k2, 16, 18, 9, 9);
					} else {
						drawTexturedModalRect(i1 - k7 * 8 - 9, k2, 25, 18, 9, 9);
					}
				}
			}

			mc.profiler.endSection();
		}
	}

	private void renderMountHealth(ScaledResolution p_184047_1_) {

		if (mc.getRenderViewEntity() instanceof EntityPlayer entityplayer) {
			Entity entity = entityplayer.getRidingEntity();

			if (entity instanceof EntityLivingBase entitylivingbase) {
				mc.profiler.endStartSection("mountHealth");
				int i = (int) Math.ceil(entitylivingbase.getHealth());
				float f = entitylivingbase.getMaxHealth();
				int j = (int) (f + 0.5F) / 2;

				if (j > 30) {
					j = 30;
				}

				int k = p_184047_1_.getScaledHeight() - 39;
				int l = p_184047_1_.getScaledWidth() / 2 + 91;
				int i1 = k;
				int j1 = 0;

				for (boolean flag = false; j > 0; j1 += 20) {
					int k1 = Math.min(j, 10);
					j -= k1;

					for (int l1 = 0; l1 < k1; ++l1) {
						int i2 = 52;
						int j2 = 0;
						int k2 = l - l1 * 8 - 9;
						drawTexturedModalRect(k2, i1, 52 + j2 * 9, 9, 9, 9);

						if (l1 * 2 + 1 + j1 < i) {
							drawTexturedModalRect(k2, i1, 88, 9, 9, 9);
						}

						if (l1 * 2 + 1 + j1 == i) {
							drawTexturedModalRect(k2, i1, 97, 9, 9, 9);
						}
					}

					i1 -= 10;
				}
			}
		}
	}

	private void renderPumpkinOverlay(ScaledResolution scaledRes) {

		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableAlpha();
		mc.getTextureManager().bindTexture(PUMPKIN_BLUR_TEX_PATH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(0D, scaledRes.getScaledHeight(), -90D).tex(0D, 1D).endVertex();
		bufferbuilder.pos(scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), -90D).tex(1D, 1D).endVertex();
		bufferbuilder.pos(scaledRes.getScaledWidth(), 0D, -90D).tex(1D, 0D).endVertex();
		bufferbuilder.pos(0D, 0D, -90D).tex(0D, 0D).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.color(1F, 1F, 1F, 1F);
	}

	/**
	 * Renders a Vignette arount the entire screen that changes with light level.
	 */
	private void renderVignette(float lightLevel, ScaledResolution scaledRes) {

		lightLevel = 1F - lightLevel;
		lightLevel = MathHelper.clamp(lightLevel, 0F, 1F);
		WorldBorder worldborder = mc.world.getWorldBorder();
		float f = (float) worldborder.getClosestDistance(mc.player);
		double d0 = Math.min(worldborder.getResizeSpeed() * (double) worldborder.getWarningTime() * 1000D, Math.abs(worldborder.getTargetSize() - worldborder.getDiameter()));
		double d1 = Math.max(worldborder.getWarningDistance(), d0);

		if ((double) f < d1) {
			f = 1F - (float) ((double) f / d1);
		} else {
			f = 0F;
		}

		prevVignetteBrightness = (float) ((double) prevVignetteBrightness + (double) (lightLevel - prevVignetteBrightness) * 0.01D);
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		if (f > 0F) {
			GlStateManager.color(0F, f, f, 1F);
		} else {
			GlStateManager.color(prevVignetteBrightness, prevVignetteBrightness, prevVignetteBrightness, 1F);
		}

		mc.getTextureManager().bindTexture(VIGNETTE_TEX_PATH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(0D, scaledRes.getScaledHeight(), -90D).tex(0D, 1D).endVertex();
		bufferbuilder.pos(scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), -90D).tex(1D, 1D).endVertex();
		bufferbuilder.pos(scaledRes.getScaledWidth(), 0D, -90D).tex(1D, 0D).endVertex();
		bufferbuilder.pos(0D, 0D, -90D).tex(0D, 0D).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	}

	private void renderPortal(float timeInPortal, ScaledResolution scaledRes) {

		if (timeInPortal < 1F) {
			timeInPortal = timeInPortal * timeInPortal;
			timeInPortal = timeInPortal * timeInPortal;
			timeInPortal = timeInPortal * 0.8F + 0.2F;
		}

		GlStateManager.disableAlpha();
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(1F, 1F, 1F, timeInPortal);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite textureatlassprite = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.PORTAL.getDefaultState());
		float f = textureatlassprite.getMinU();
		float f1 = textureatlassprite.getMinV();
		float f2 = textureatlassprite.getMaxU();
		float f3 = textureatlassprite.getMaxV();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(0D, scaledRes.getScaledHeight(), -90D).tex(f, f3).endVertex();
		bufferbuilder.pos(scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), -90D).tex(f2, f3).endVertex();
		bufferbuilder.pos(scaledRes.getScaledWidth(), 0D, -90D).tex(f2, f1).endVertex();
		bufferbuilder.pos(0D, 0D, -90D).tex(f, f1).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.color(1F, 1F, 1F, 1F);
	}

	private void renderHotbarItem(int p_184044_1_, int p_184044_2_, float p_184044_3_, EntityPlayer player, ItemStack stack) {

		if (!stack.isEmpty()) {
			float f = (float) stack.getAnimationsToGo() - p_184044_3_;

			if (f > 0F) {
				GlStateManager.pushMatrix();
				float f1 = 1F + f / 5F;
				GlStateManager.translate((float) (p_184044_1_ + 8), (float) (p_184044_2_ + 12), 0F);
				GlStateManager.scale(1F / f1, (f1 + 1F) / 2F, 1F);
				GlStateManager.translate((float) (-(p_184044_1_ + 8)), (float) (-(p_184044_2_ + 12)), 0F);
			}

			itemRenderer.renderItemAndEffectIntoGUI(player, stack, p_184044_1_, p_184044_2_);

			if (f > 0F) {
				GlStateManager.popMatrix();
			}

			itemRenderer.renderItemOverlays(mc.fontRenderer, stack, p_184044_1_, p_184044_2_);
		}
	}

	/**
	 * The update tick for the ingame UI
	 */
	public void updateTick() {

		if (overlayMessageTime > 0) {
			--overlayMessageTime;
		}

		if (titlesTimer > 0) {
			--titlesTimer;

			if (titlesTimer <= 0) {
				displayedTitle = "";
				displayedSubTitle = "";
			}
		}

		++updateCounter;

		if (mc.player != null) {
			ItemStack itemstack = mc.player.inventory.getCurrentItem();

			if (itemstack.isEmpty()) {
				remainingHighlightTicks = 0;
			} else if (!highlightingItemStack.isEmpty() && itemstack.getItem() == highlightingItemStack.getItem() && ItemStack.areItemStackTagsEqual(itemstack, highlightingItemStack) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == highlightingItemStack.getMetadata())) {
				if (remainingHighlightTicks > 0) {
					--remainingHighlightTicks;
				}
			} else {
				remainingHighlightTicks = 40;
			}

			highlightingItemStack = itemstack;
		}
	}

	public void setRecordPlayingMessage(String recordName) {

		setOverlayMessage(I18n.format("record.nowPlaying", recordName), true);
	}

	public void setOverlayMessage(String message, boolean animateColor) {

		overlayMessage = message;
		overlayMessageTime = 60;
		animateOverlayMessageColor = animateColor;
	}

	public void displayTitle(String title, String subTitle, int timeFadeIn, int displayTime, int timeFadeOut) {

		if (title == null && subTitle == null && timeFadeIn < 0 && displayTime < 0 && timeFadeOut < 0) {
			displayedTitle = "";
			displayedSubTitle = "";
			titlesTimer = 0;
		} else if (title != null) {
			displayedTitle = title;
			titlesTimer = titleFadeIn + titleDisplayTime + titleFadeOut;
		} else if (subTitle != null) {
			displayedSubTitle = subTitle;
		} else {
			if (timeFadeIn >= 0) {
				titleFadeIn = timeFadeIn;
			}

			if (displayTime >= 0) {
				titleDisplayTime = displayTime;
			}

			if (timeFadeOut >= 0) {
				titleFadeOut = timeFadeOut;
			}

			if (titlesTimer > 0) {
				titlesTimer = titleFadeIn + titleDisplayTime + titleFadeOut;
			}
		}
	}

	public void setOverlayMessage(ITextComponent component, boolean animateColor) {

		setOverlayMessage(component.getUnformattedText(), animateColor);
	}

	/**
	 * Forwards the given chat message to all listeners.
	 */
	public void addChatMessage(ChatType chatTypeIn, ITextComponent message) {

		for (IChatListener ichatlistener : chatListeners.get(chatTypeIn)) {
			ichatlistener.say(chatTypeIn, message);
		}
	}

	/**
	 * returns a pointer to the persistant Chat GUI, containing all previous chat messages and such
	 */
	public GuiNewChat getChatGUI() {

		return persistantChatGUI;
	}

	public int getUpdateCounter() {

		return updateCounter;
	}

	public FontRenderer getFontRenderer() {

		return mc.fontRenderer;
	}

	public GuiSpectator getSpectatorGui() {

		return spectatorGui;
	}

	public GuiPlayerTabOverlay getTabList() {

		return overlayPlayerList;
	}

	/**
	 * Reset the GuiPlayerTabOverlay's message header and footer
	 */
	public void resetPlayersOverlayFooterHeader() {

		overlayPlayerList.resetFooterHeader();
		overlayBoss.clearBossInfos();
		mc.getToastGui().clear();
	}

	/**
	 * Accessor for the GuiBossOverlay
	 */
	public GuiBossOverlay getBossOverlay() {

		return overlayBoss;
	}

}
