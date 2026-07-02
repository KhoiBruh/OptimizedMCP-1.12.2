package net.minecraft.client.gui.inventory;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;

public class BeaconScreen extends ContainerScreen {

	private static final ResourceLocation BEACON_GUI_TEXTURES = new ResourceLocation("textures/gui/container/beacon.png");
	private final IInventory tileBeacon;
	private BeaconScreen.ConfirmButton beaconConfirmButton;
	private boolean buttonsNotDrawn;

	public BeaconScreen(InventoryPlayer playerInventory, IInventory tileBeaconIn) {
		super(new ContainerBeacon(playerInventory, tileBeaconIn));
		tileBeacon = tileBeaconIn;
		xSize = 230;
		ySize = 219;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		super.init();
		beaconConfirmButton = new BeaconScreen.ConfirmButton(-1, guiLeft + 164, guiTop + 107);
		buttons.add(beaconConfirmButton);
		buttons.add(new BeaconScreen.CancelButton(-2, guiLeft + 190, guiTop + 107));
		buttonsNotDrawn = true;
		beaconConfirmButton.enabled = false;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {
		super.update();
		int i = tileBeacon.getField(0);
		Potion potion = Potion.getPotionById(tileBeacon.getField(1));
		Potion potion1 = Potion.getPotionById(tileBeacon.getField(2));

		if (buttonsNotDrawn && i >= 0) {
			buttonsNotDrawn = false;
			int j = 100;

			for (int k = 0; k <= 2; ++k) {
				int l = TileEntityBeacon.EFFECTS_LIST[k].length;
				int i1 = l * 22 + (l - 1) * 2;

				for (int j1 = 0; j1 < l; ++j1) {
					Potion potion2 = TileEntityBeacon.EFFECTS_LIST[k][j1];
					BeaconScreen.PowerButton guibeacon$powerbutton = new BeaconScreen.PowerButton(j++, guiLeft + 76 + j1 * 24 - i1 / 2, guiTop + 22 + k * 25, potion2, k);
					buttons.add(guibeacon$powerbutton);

					if (k >= i) {
						guibeacon$powerbutton.enabled = false;
					} else if (potion2 == potion) {
						guibeacon$powerbutton.setSelected(true);
					}
				}
			}

			int l1 = TileEntityBeacon.EFFECTS_LIST[3].length + 1;
			int i2 = l1 * 22 + (l1 - 1) * 2;

			for (int j2 = 0; j2 < l1 - 1; ++j2) {
				Potion potion3 = TileEntityBeacon.EFFECTS_LIST[3][j2];
				BeaconScreen.PowerButton guibeacon$powerbutton2 = new BeaconScreen.PowerButton(j++, guiLeft + 167 + j2 * 24 - i2 / 2, guiTop + 47, potion3, 3);
				buttons.add(guibeacon$powerbutton2);

				if (3 >= i) {
					guibeacon$powerbutton2.enabled = false;
				} else if (potion3 == potion1) {
					guibeacon$powerbutton2.setSelected(true);
				}
			}

			if (potion != null) {
				BeaconScreen.PowerButton guibeacon$powerbutton1 = new BeaconScreen.PowerButton(j++, guiLeft + 167 + (l1 - 1) * 24 - i2 / 2, guiTop + 47, potion, 3);
				buttons.add(guibeacon$powerbutton1);

				if (3 >= i) {
					guibeacon$powerbutton1.enabled = false;
				} else if (potion == potion1) {
					guibeacon$powerbutton1.setSelected(true);
				}
			}
		}

		beaconConfirmButton.enabled = !tileBeacon.getStackInSlot(0).isEmpty() && potion != null;
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(net.minecraft.client.gui.component.Button button) {
		if (button.id == -2) {
			mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.openContainer.windowId));
			mc.displayScreen(null);
		} else if (button.id == -1) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeInt(tileBeacon.getField(1));
			packetbuffer.writeInt(tileBeacon.getField(2));
			mc.getConnection().sendPacket(new CPacketCustomPayload("MC|Beacon", packetbuffer));
			mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.openContainer.windowId));
			mc.displayScreen(null);
		} else if (button instanceof PowerButton guibeacon$powerbutton) {

			if (guibeacon$powerbutton.isSelected()) {
				return;
			}

			int i = Potion.getIdFromPotion(guibeacon$powerbutton.effect);

			if (guibeacon$powerbutton.tier < 3) {
				tileBeacon.setField(1, i);
			} else {
				tileBeacon.setField(2, i);
			}

			buttons.clear();
			this.init();
			this.update();
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.draw(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		RenderHelper.disableStandardItemLighting();
		drawCenteredString(fontRenderer, I18n.format("tile.beacon.primary"), 62, 10, 14737632);
		drawCenteredString(fontRenderer, I18n.format("tile.beacon.secondary"), 169, 10, 14737632);

		for (net.minecraft.client.gui.component.Button guibutton : buttons) {
			if (guibutton.isMouseOver()) {
				guibutton.drawButtonForegroundLayer(mouseX - guiLeft, mouseY - guiTop);
				break;
			}
		}

		RenderHelper.enableGUIStandardItemLighting();
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GLS.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(BEACON_GUI_TEXTURES);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
		itemRender.zLevel = 100F;
		itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.EMERALD), i + 42, j + 109);
		itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.DIAMOND), i + 42 + 22, j + 109);
		itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.GOLD_INGOT), i + 42 + 44, j + 109);
		itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.IRON_INGOT), i + 42 + 66, j + 109);
		itemRender.zLevel = 0F;
	}

	static class Button extends net.minecraft.client.gui.component.Button {

		private final ResourceLocation iconTexture;
		private final int iconX;
		private final int iconY;
		private boolean selected;

		protected Button(int buttonId, int x, int y, ResourceLocation iconTextureIn, int iconXIn, int iconYIn) {
			super(buttonId, x, y, 22, 22, "");
			iconTexture = iconTextureIn;
			iconX = iconXIn;
			iconY = iconYIn;
		}

		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if (visible) {
				mc.getTextureManager().bindTexture(BeaconScreen.BEACON_GUI_TEXTURES);
				GLS.color(1F, 1F, 1F, 1F);
				hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
				int j = 0;

				if (!enabled) {
					j += width * 2;
				} else if (selected) {
					j += width;
				} else if (hovered) {
					j += width * 3;
				}

				drawTexturedModalRect(x, y, j, 219, width, height);

				if (!BeaconScreen.BEACON_GUI_TEXTURES.equals(iconTexture)) {
					mc.getTextureManager().bindTexture(iconTexture);
				}

				drawTexturedModalRect(x + 2, y + 2, iconX, iconY, 18, 18);
			}
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selectedIn) {
			selected = selectedIn;
		}

	}

	class CancelButton extends BeaconScreen.Button {

		public CancelButton(int buttonId, int x, int y) {
			super(buttonId, x, y, BeaconScreen.BEACON_GUI_TEXTURES, 112, 220);
		}

		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.format("gui.cancel"), mouseX, mouseY);
		}

	}

	class ConfirmButton extends BeaconScreen.Button {

		public ConfirmButton(int buttonId, int x, int y) {
			super(buttonId, x, y, BeaconScreen.BEACON_GUI_TEXTURES, 90, 220);
		}

		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.format("gui.done"), mouseX, mouseY);
		}

	}

	class PowerButton extends BeaconScreen.Button {

		private final Potion effect;
		private final int tier;

		public PowerButton(int buttonId, int x, int y, Potion effectIn, int tierIn) {
			super(buttonId, x, y, ContainerScreen.INVENTORY_BACKGROUND, effectIn.getStatusIconIndex() % 8 * 18, 198 + effectIn.getStatusIconIndex() / 8 * 18);
			effect = effectIn;
			tier = tierIn;
		}

		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			String s = I18n.format(effect.getName());

			if (tier >= 3 && effect != MobEffects.REGENERATION) {
				s = s + " II";
			}

			drawHoveringText(s, mouseX, mouseY);
		}

	}

}
