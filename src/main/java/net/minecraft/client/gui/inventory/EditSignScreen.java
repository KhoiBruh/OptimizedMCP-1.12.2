package net.minecraft.client.gui.inventory;

import net.minecraft.block.Block;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.util.Keyboard;

public class EditSignScreen extends Screen {

	/**
	 * Reference to the sign object.
	 */
	private final TileEntitySign tileSign;

	/**
	 * Counts the number of screen updates.
	 */
	private int updateCounter;

	/**
	 * The index of the line that is being edited.
	 */
	private int editLine;

	/**
	 * "Done" button for the GUI.
	 */
	private Button doneBtn;

	public EditSignScreen(TileEntitySign teSign) {

		tileSign = teSign;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		buttons.clear();
		Keyboard.setRepeat(true);
		doneBtn = addButton(new Button(0, width / 2 - 100, height / 4 + 120, I18n.format("gui.done")));
		tileSign.setEditable(false);
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void close() {

		Keyboard.setRepeat(false);
		NetHandlerPlayClient nethandlerplayclient = mc.getConnection();

		if (nethandlerplayclient != null) {
			nethandlerplayclient.sendPacket(new CPacketUpdateSign(tileSign.getPos(), tileSign.signText));
		}

		tileSign.setEditable(true);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {

		++updateCounter;
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		if (button.enabled) {
			if (button.id == 0) {
				tileSign.markDirty();
				mc.displayScreen(null);
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {

		if (keyCode == 265) {
			editLine = editLine - 1 & 3;
		}

		if (keyCode == 264 || keyCode == 257 || keyCode == 335) {
			editLine = editLine + 1 & 3;
		}

		String s = tileSign.signText[editLine].getUnformattedText();

		if (keyCode == 259 && !s.isEmpty()) {
			s = s.substring(0, s.length() - 1);
		}

		if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && fontRenderer.getStringWidth(s + typedChar) <= 90) {
			s = s + typedChar;
		}

		tileSign.signText[editLine] = new TextComponentString(s);

		if (keyCode == 256) {
			action(doneBtn);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("sign.edit"), width / 2, 40, 16777215);
		GLS.color(1F, 1F, 1F, 1F);
		GLS.pushMatrix();
		GLS.translate((float) (width / 2), 0F, 50F);
		float f = 93.75F;
		GLS.scale(-93.75F, -93.75F, -93.75F);
		GLS.rotate(180F, 0F, 1F, 0F);
		Block block = tileSign.getBlockType();

		if (block == Blocks.STANDING_SIGN) {
			float f1 = (float) (tileSign.getBlockMetadata() * 360) / 16F;
			GLS.rotate(f1, 0F, 1F, 0F);
			GLS.translate(0F, -1.0625F, 0F);
		} else {
			int i = tileSign.getBlockMetadata();
			float f2 = 0F;

			if (i == 2) {
				f2 = 180F;
			}

			if (i == 4) {
				f2 = 90F;
			}

			if (i == 5) {
				f2 = -90F;
			}

			GLS.rotate(f2, 0F, 1F, 0F);
			GLS.translate(0F, -1.0625F, 0F);
		}

		if (updateCounter / 6 % 2 == 0) {
			tileSign.lineBeingEdited = editLine;
		}

		TileEntityRendererDispatcher.instance.render(tileSign, -0.5D, -0.75D, -0.5D, 0F);
		tileSign.lineBeingEdited = -1;
		GLS.popMatrix();
		super.draw(mouseX, mouseY, partialTicks);
	}

}
