package net.minecraft.client.gui.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.component.GuiListExtended;
import net.minecraft.client.gui.loading.WorkingScreen;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Validate;
import net.minecraft.util.text.TextFormat;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import net.minecraft.client.renderer.NativeImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GuiListWorldSelectionEntry implements GuiListExtended.IGuiListEntry {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
	private final Minecraft client;
	private final WorldSelectScreen worldSelScreen;
	private final WorldSummary worldSummary;
	private final ResourceLocation iconLocation;
	private final GuiListWorldSelection containingListSel;
	private File iconFile;
	private DynamicTexture icon;
	private long lastClickTime;

	public GuiListWorldSelectionEntry(GuiListWorldSelection listWorldSelIn, WorldSummary worldSummaryIn, ISaveFormat saveFormat) {
		containingListSel = listWorldSelIn;
		worldSelScreen = listWorldSelIn.getGuiWorldSelection();
		worldSummary = worldSummaryIn;
		client = Minecraft.getMinecraft();
		iconLocation = new ResourceLocation("worlds/" + worldSummaryIn.getFileName() + "/icon");
		iconFile = saveFormat.getFile(worldSummaryIn.getFileName(), "icon.png");

		if (!iconFile.isFile()) {
			iconFile = null;
		}

		loadServerIcon();
	}

	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
		String s = worldSummary.getDisplayName();
		String s1 = worldSummary.getFileName() + " (" + DATE_FORMAT.format(new Date(worldSummary.getLastTimePlayed())) + ")";
		String s2 = "";

		if (s == null || s.isEmpty()) {
			s = I18n.format("selectWorld.world") + " " + (slotIndex + 1);
		}

		if (worldSummary.requiresConversion()) {
			s2 = I18n.format("selectWorld.conversion") + " " + s2;
		} else {
			s2 = I18n.format("gameMode." + worldSummary.getEnumGameType().getName());

			if (worldSummary.isHardcoreModeEnabled()) {
				s2 = TextFormat.DARK_RED + I18n.format("gameMode.hardcore") + TextFormat.RESET;
			}

			if (worldSummary.getCheatsEnabled()) {
				s2 = s2 + ", " + I18n.format("selectWorld.cheats");
			}

			String s3 = worldSummary.getVersionName();

			if (worldSummary.markVersionInList()) {
				if (worldSummary.askToOpenWorld()) {
					s2 = s2 + ", " + I18n.format("selectWorld.version") + " " + TextFormat.RED + s3 + TextFormat.RESET;
				} else {
					s2 = s2 + ", " + I18n.format("selectWorld.version") + " " + TextFormat.ITALIC + s3 + TextFormat.RESET;
				}
			} else {
				s2 = s2 + ", " + I18n.format("selectWorld.version") + " " + s3;
			}
		}

		client.fontRenderer.drawText(s, x + 32 + 3, y + 1, 16777215);
		client.fontRenderer.drawText(s1, x + 32 + 3, y + client.fontRenderer.FONT_HEIGHT + 3, 8421504);
		client.fontRenderer.drawText(s2, x + 32 + 3, y + client.fontRenderer.FONT_HEIGHT + client.fontRenderer.FONT_HEIGHT + 3, 8421504);
		GLS.color(1F, 1F, 1F, 1F);
		client.getTextureManager().bindTexture(icon != null ? iconLocation : ICON_MISSING);
		GLS.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0F, 0F, 32, 32, 32F, 32F);
		GLS.disableBlend();

		if (client.gameSettings.touchscreen || isSelected) {
			client.getTextureManager().bindTexture(ICON_OVERLAY_LOCATION);
			Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
			GLS.color(1F, 1F, 1F, 1F);
			int j = mouseX - x;
			int i = j < 32 ? 32 : 0;

			if (worldSummary.markVersionInList()) {
				Gui.drawModalRectWithCustomSizedTexture(x, y, 32F, (float) i, 32, 32, 256F, 256F);

				if (worldSummary.askToOpenWorld()) {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 96F, (float) i, 32, 32, 256F, 256F);

					if (j < 32) {
						worldSelScreen.setVersionTooltip(TextFormat.RED + I18n.format("selectWorld.tooltip.fromNewerVersion1") + "\n" + TextFormat.RED + I18n.format("selectWorld.tooltip.fromNewerVersion2"));
					}
				} else {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 64F, (float) i, 32, 32, 256F, 256F);

					if (j < 32) {
						worldSelScreen.setVersionTooltip(TextFormat.GOLD + I18n.format("selectWorld.tooltip.snapshot1") + "\n" + TextFormat.GOLD + I18n.format("selectWorld.tooltip.snapshot2"));
					}
				}
			} else {
				Gui.drawModalRectWithCustomSizedTexture(x, y, 0F, (float) i, 32, 32, 256F, 256F);
			}
		}
	}

	/**
	 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
	 * clicked and the list should not be dragged.
	 */
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
		containingListSel.selectWorld(slotIndex);

		if (relativeX < 32) {
			joinWorld();
			return true;
		} else if (Minecraft.getSystemTime() - lastClickTime < 250L) {
			joinWorld();
			return true;
		} else {
			lastClickTime = Minecraft.getSystemTime();
			return false;
		}
	}

	public void joinWorld() {
		if (worldSummary.askToOpenWorld()) {
			client.displayScreen(new YesNoScreen((result, id) -> {

				if (result) {
					loadWorld();
				} else {
					client.displayScreen(worldSelScreen);
				}
			}, I18n.format("selectWorld.versionQuestion"), I18n.format("selectWorld.versionWarning", worldSummary.getVersionName()), I18n.format("selectWorld.versionJoinButton"), I18n.format("gui.cancel"), 0));
		} else {
			loadWorld();
		}
	}

	public void deleteWorld() {
		client.displayScreen(new YesNoScreen((result, id) -> {

			if (result) {
				client.displayScreen(new WorkingScreen());
				ISaveFormat isaveformat = client.getSaveLoader();
				isaveformat.flushCache();
				isaveformat.deleteWorldDirectory(worldSummary.getFileName());
				containingListSel.refreshList();
			}

			client.displayScreen(worldSelScreen);
		}, I18n.format("selectWorld.deleteQuestion"), "'" + worldSummary.getDisplayName() + "' " + I18n.format("selectWorld.deleteWarning"), I18n.format("selectWorld.deleteButton"), I18n.format("gui.cancel"), 0));
	}

	public void editWorld() {
		client.displayScreen(new WorldEditScreen(worldSelScreen, worldSummary.getFileName()));
	}

	public void recreateWorld() {
		client.displayScreen(new WorkingScreen());
		CreateWorldScreen guicreateworld = new CreateWorldScreen(worldSelScreen);
		ISaveHandler isavehandler = client.getSaveLoader().getSaveLoader(worldSummary.getFileName(), false);
		WorldInfo worldinfo = isavehandler.loadWorldInfo();
		isavehandler.flush();

		if (worldinfo != null) {
			guicreateworld.recreateFromExistingWorld(worldinfo);
			client.displayScreen(guicreateworld);
		}
	}

	private void loadWorld() {
		client.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));

		if (client.getSaveLoader().canLoadWorld(worldSummary.getFileName())) {
			client.launchIntegratedServer(worldSummary.getFileName(), worldSummary.getDisplayName(), null);
		}
	}

	private void loadServerIcon() {
		boolean flag = iconFile != null && iconFile.isFile();

		if (flag) {
			NativeImage bufferedimage;

			try {
				bufferedimage = NativeImage.read(iconFile);
				Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
				Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
			} catch (Throwable throwable) {
				LOGGER.error("Invalid icon for world {}", worldSummary.getFileName(), throwable);
				iconFile = null;
				return;
			}

			if (icon == null) {
				icon = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
				client.getTextureManager().loadTexture(iconLocation, icon);
			}

			bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), icon.getTextureData(), 0, bufferedimage.getWidth());
						bufferedimage.close();
			icon.updateDynamicTexture();
		} else {
			client.getTextureManager().deleteTexture(iconLocation);
			icon = null;
		}
	}

	/**
	 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
	 */
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
	}

	public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
	}

}
