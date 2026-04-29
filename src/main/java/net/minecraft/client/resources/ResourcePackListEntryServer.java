package net.minecraft.client.resources;

import com.google.gson.JsonParseException;
import net.minecraft.client.gui.menu.GuiScreenResourcePacks;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ResourcePackListEntryServer extends ResourcePackListEntry {

	private static final Logger LOGGER = LogManager.getLogger();
	private final IResourcePack resourcePack;
	private final ResourceLocation resourcePackIcon;

	public ResourcePackListEntryServer(GuiScreenResourcePacks resourcePacksGUIIn, IResourcePack resourcePackIn) {

		super(resourcePacksGUIIn);
		resourcePack = resourcePackIn;
		DynamicTexture dynamictexture;

		try {
			dynamictexture = new DynamicTexture(resourcePackIn.getPackImage());
		} catch (IOException var5) {
			dynamictexture = TextureUtil.MISSING_TEXTURE;
		}

		resourcePackIcon = mc.getTextureManager().getDynamicTextureLocation("texturepackicon", dynamictexture);
	}

	protected int getResourcePackFormat() {

		return 3;
	}

	protected String getResourcePackDescription() {

		try {
			PackMetadataSection packmetadatasection = resourcePack.getPackMetadata(mc.getResourcePackRepository().rprMetadataSerializer, "pack");

			if (packmetadatasection != null) {
				return packmetadatasection.packDescription().getFormattedText();
			}
		} catch (JsonParseException | IOException jsonparseexception) {
			LOGGER.error("Couldn't load metadata info", jsonparseexception);
		}

		return TextFormatting.RED + "Missing " + "pack.mcmeta" + " :(";
	}

	protected boolean canMoveRight() {

		return false;
	}

	protected boolean canMoveLeft() {

		return false;
	}

	protected boolean canMoveUp() {

		return false;
	}

	protected boolean canMoveDown() {

		return false;
	}

	protected String getResourcePackName() {

		return "Server";
	}

	protected void bindResourcePackIcon() {

		mc.getTextureManager().bindTexture(resourcePackIcon);
	}

	protected boolean showHoverOverlay() {

		return false;
	}

	public boolean isServerPack() {

		return true;
	}

}
