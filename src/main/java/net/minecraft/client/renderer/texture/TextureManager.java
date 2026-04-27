package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TextureManager implements ITickable, IResourceManagerReloadListener {

	private static final Logger LOGGER = LogManager.getLogger();
	public static final ResourceLocation RESOURCE_LOCATION_EMPTY = new ResourceLocation("");
	private final Map<ResourceLocation, ITextureObject> mapTextureObjects = Maps.newHashMap();
	private final List<ITickable> listTickables = Lists.newArrayList();
	private final Map<String, Integer> mapTextureCounters = Maps.newHashMap();
	private final IResourceManager resourceManager;

	public TextureManager(IResourceManager resourceManager) {

		this.resourceManager = resourceManager;
	}

	public void bindTexture(ResourceLocation resource) {

		ITextureObject itextureobject = mapTextureObjects.get(resource);

		if (itextureobject == null) {
			itextureobject = new SimpleTexture(resource);
			loadTexture(resource, itextureobject);
		}

		TextureUtil.bindTexture(itextureobject.getGlTextureId());
	}

	public boolean loadTickableTexture(ResourceLocation textureLocation, ITickableTextureObject textureObj) {

		if (loadTexture(textureLocation, textureObj)) {
			listTickables.add(textureObj);
			return true;
		} else {
			return false;
		}
	}

	public boolean loadTexture(ResourceLocation textureLocation, ITextureObject textureObj) {

		boolean flag = true;

		try {
			textureObj.loadTexture(resourceManager);
		} catch (IOException ioexception) {
			if (textureLocation != RESOURCE_LOCATION_EMPTY) {
				LOGGER.warn("Failed to load texture: {}", textureLocation, ioexception);
			}

			textureObj = TextureUtil.MISSING_TEXTURE;
			mapTextureObjects.put(textureLocation, textureObj);
			flag = false;
		} catch (Throwable throwable) {
			final ITextureObject textureObjf = textureObj;
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Registering texture");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Resource location being registered");
			crashreportcategory.addCrashSection("Resource location", textureLocation);
			crashreportcategory.addDetail("Texture object class", new ICrashReportDetail<String>() {
				public String call() throws Exception {

					return textureObjf.getClass().getName();
				}
			});
			throw new ReportedException(crashreport);
		}

		mapTextureObjects.put(textureLocation, textureObj);
		return flag;
	}

	public ITextureObject getTexture(ResourceLocation textureLocation) {

		return mapTextureObjects.get(textureLocation);
	}

	public ResourceLocation getDynamicTextureLocation(String name, DynamicTexture texture) {

		Integer integer = mapTextureCounters.get(name);

		if (integer == null) {
			integer = Integer.valueOf(1);
		} else {
			integer = integer.intValue() + 1;
		}

		mapTextureCounters.put(name, integer);
		ResourceLocation resourcelocation = new ResourceLocation(String.format("dynamic/%s_%d", name, integer));
		loadTexture(resourcelocation, texture);
		return resourcelocation;
	}

	public void tick() {

		for (ITickable itickable : listTickables) {
			itickable.tick();
		}
	}

	public void deleteTexture(ResourceLocation textureLocation) {

		ITextureObject itextureobject = getTexture(textureLocation);

		if (itextureobject != null) {
			TextureUtil.deleteTexture(itextureobject.getGlTextureId());
		}
	}

	public void onResourceManagerReload(IResourceManager resourceManager) {

		Iterator<Entry<ResourceLocation, ITextureObject>> iterator = mapTextureObjects.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<ResourceLocation, ITextureObject> entry = iterator.next();
			ITextureObject itextureobject = entry.getValue();

			if (itextureobject == TextureUtil.MISSING_TEXTURE) {
				iterator.remove();
			} else {
				loadTexture(entry.getKey(), itextureobject);
			}
		}
	}

}
