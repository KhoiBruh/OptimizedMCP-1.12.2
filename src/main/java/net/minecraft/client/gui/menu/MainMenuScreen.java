package net.minecraft.client.gui.menu;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Runnables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.LanguageButton;
import net.minecraft.client.gui.game.WinGameScreen;
import net.minecraft.client.gui.option.LanguageScreen;
import net.minecraft.client.gui.option.OptionsScreen;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.util.Mouse;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Maths;
import net.minecraft.util.text.TextFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainMenuScreen extends Screen {

	public static final String MORE_INFO_TEXT = "Please click " + TextFormat.UNDERLINE + "here" + TextFormat.RESET + " for more information.";
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Random RANDOM = new Random();
	private static final ResourceLocation SPLASH_TEXTS = new ResourceLocation("texts/splashes.txt");
	private static final ResourceLocation MINECRAFT_TITLE_TEXTURES = new ResourceLocation("textures/gui/title/minecraft.png");
	private static final ResourceLocation field_194400_H = new ResourceLocation("textures/gui/title/edition.png");
	/**
	 * A random number between 0.0 and 1.0, used to determine if the title screen says <a
	 * href="https://minecraft.gamepedia.com/Menu_screen#Minceraft">Minceraft</a> instead of Minecraft. Set during
	 * construction; if the value is less than .0001, then Minceraft is displayed.
	 */
	private final float minceraftRoll;
	/**
	 * The Object object utilized as a thread lock when performing non thread-safe operations
	 */
	private final Object threadLock = new Object();
	private String splashText;

	private int widthCopyright;
	private int widthCopyrightRest;

	public MainMenuScreen() {
		splashText = "missingno";

		try (IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(SPLASH_TEXTS)) {
			List<String> list = Lists.newArrayList();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8));
			String s;

			while ((s = bufferedreader.readLine()) != null) {
				s = s.trim();

				if (!s.isEmpty()) {
					list.add(s);
				}
			}

			if (!list.isEmpty()) {
				do {
					splashText = list.get(RANDOM.nextInt(list.size()));

				} while (splashText.hashCode() == 125780783);
			}
		} catch (IOException ignored) {
		}

		minceraftRoll = RANDOM.nextFloat();
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	public boolean pauseGame() {
		return false;
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		widthCopyright = fontRenderer.getWidth("Copyright Mojang AB. Do not distribute!");
		widthCopyrightRest = width - widthCopyright - 2;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) == 24) {
			splashText = "Merry X-mas!";
		} else if (calendar.get(Calendar.MONTH) + 1 == 1 && calendar.get(Calendar.DATE) == 1) {
			splashText = "Happy new year!";
		} else if (calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DATE) == 31) {
			splashText = "OOoooOOOoooo! Spooky!";
		}

		int j = height / 4 + 48;

		addSingleplayerMultiplayerButtons(j, 24);

		buttons.add(new Button(0, width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options")));
		buttons.add(new Button(4, width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit")));
		buttons.add(new LanguageButton(5, width / 2 - 124, j + 72 + 12));
	}

	/**
	 * Adds Singleplayer and Multiplayer buttons on net.minecraft.client.main.Main Menu for players who have bought the game.
	 */
	private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
		buttons.add(new Button(1, width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer")));
		buttons.add(new Button(2, width / 2 - 100, p_73969_1_ + p_73969_2_, I18n.format("menu.multiplayer")));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {
		if (button.id == 0) {
			mc.displayScreen(new OptionsScreen(this, mc.gameSettings));
		}

		if (button.id == 5) {
			mc.displayScreen(new LanguageScreen(this, mc.gameSettings, mc.getLanguageManager()));
		}

		if (button.id == 1) {
			mc.displayScreen(new WorldSelectScreen(this));
		}

		if (button.id == 2) {
			mc.displayScreen(new MultiplayerScreen(this));
		}

		if (button.id == 4) {
			mc.shutdown();
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		int j = width / 2 - 137;
		mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
		GLS.color(1F, 1F, 1F, 1F);

		if ((double) minceraftRoll < 1.0E-4D) {
			context.blit(j, 30, 0, 0, 99, 44);
			context.blit(j + 99, 30, 129, 0, 27, 44);
			context.blit(j + 99 + 26, 30, 126, 0, 3, 44);
			context.blit(j + 99 + 26 + 3, 30, 99, 0, 26, 44);
			context.blit(j + 155, 30, 0, 45, 155, 44);
		} else {
			context.blit(j, 30, 0, 0, 155, 44);
			context.blit(j + 155, 30, 0, 45, 155, 44);
		}

		mc.getTextureManager().bindTexture(field_194400_H);
		context.blit(j + 88, 67, 0F, 0F, 98, 14, 128F, 16F);
		GLS.pushMatrix();
		GLS.translate((float) (width / 2 + 90), 70F, 0F);
		GLS.rotate(-20F, 0F, 0F, 1F);
		float f = 1.8F - Maths.abs(Maths.sin((float) (Minecraft.getSystemTime() % 1000L) / 1000F * ((float) Math.PI * 2F)) * 0.1F);
		f = f * 100F / (float) (fontRenderer.getWidth(splashText) + 32);
		GLS.scale(f, f, f);
		context.drawCenteredString(fontRenderer, splashText, 0, -8, -256);
		GLS.popMatrix();
		String s = "Minecraft 1.12.2";

		s = s + ("release".equalsIgnoreCase(mc.getVersionType()) ? "" : "/" + mc.getVersionType());

		context.drawString(fontRenderer, s, 2, height - 10, -1);
		context.drawString(fontRenderer, "Copyright Mojang AB. Do not distribute!", widthCopyrightRest, height - 10, -1);

		if (mouseX > widthCopyrightRest && mouseX < widthCopyrightRest + widthCopyright && mouseY > height - 10 && mouseY < height && Mouse.isInsideWindow()) {
			context.fill(widthCopyrightRest, height - 1, widthCopyrightRest + widthCopyright, height, -1);
		}

		super.draw(context, mouseX, mouseY, partialTicks);
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouse) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouse);

		if (mouseX > widthCopyrightRest && mouseX < widthCopyrightRest + widthCopyright && mouseY > height - 10 && mouseY < height) {
			mc.displayScreen(new WinGameScreen(false, Runnables.doNothing()));
		}
	}

}
