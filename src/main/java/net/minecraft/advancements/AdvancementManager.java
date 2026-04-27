package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class AdvancementManager {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).registerTypeHierarchyAdapter(Advancement.Builder.class, (JsonDeserializer<Advancement.Builder>) (p_deserialize_1_, p_deserialize_2_, p_deserialize_3_) -> {

		JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "advancement");
		return Advancement.Builder.deserialize(jsonobject, p_deserialize_3_);
	}).registerTypeAdapter(AdvancementRewards.class, new AdvancementRewards.Deserializer()).registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer()).registerTypeHierarchyAdapter(Style.class, new Style.Serializer()).registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();
	private static final AdvancementList ADVANCEMENT_LIST = new AdvancementList();

	/**
	 * The directory where this manager looks for custom advancement files.
	 */
	private final File advancementsDir;
	private boolean hasErrored;

	public AdvancementManager(@Nullable File advancementsDirIn) {

		advancementsDir = advancementsDirIn;
		reload();
	}

	public void reload() {

		hasErrored = false;
		ADVANCEMENT_LIST.clear();
		Map<ResourceLocation, Advancement.Builder> map = loadCustomAdvancements();
		loadBuiltInAdvancements(map);
		ADVANCEMENT_LIST.loadAdvancements(map);

		for (Advancement advancement : ADVANCEMENT_LIST.getRoots()) {
			if (advancement.getDisplay() != null) {
				AdvancementTreeNode.layout(advancement);
			}
		}
	}

	public boolean hasErrored() {

		return hasErrored;
	}

	private Map<ResourceLocation, Advancement.Builder> loadCustomAdvancements() {

		if (advancementsDir == null) {
			return Maps.newHashMap();
		} else {
			Map<ResourceLocation, Advancement.Builder> map = Maps.newHashMap();
			advancementsDir.mkdirs();

			for (File file1 : FileUtils.listFiles(advancementsDir, new String[]{"json"}, true)) {
				String s = FilenameUtils.removeExtension(advancementsDir.toURI().relativize(file1.toURI()).toString());
				String[] astring = s.split("/", 2);

				if (astring.length == 2) {
					ResourceLocation resourcelocation = new ResourceLocation(astring[0], astring[1]);

					try {
						Advancement.Builder advancement$builder = JsonUtils.gsonDeserialize(GSON, FileUtils.readFileToString(file1, StandardCharsets.UTF_8), Advancement.Builder.class);

						if (advancement$builder == null) {
							LOGGER.error("Couldn't load custom advancement {} from {} as it's empty or null", resourcelocation, file1);
						} else {
							map.put(resourcelocation, advancement$builder);
						}
					} catch (IllegalArgumentException | JsonParseException jsonparseexception) {
						LOGGER.error("Parsing error loading custom advancement {}", resourcelocation, jsonparseexception);
						hasErrored = true;
					} catch (IOException ioexception) {
						LOGGER.error("Couldn't read custom advancement {} from {}", resourcelocation, file1, ioexception);
						hasErrored = true;
					}
				}
			}
			return map;
		}
	}

	private void loadBuiltInAdvancements(Map<ResourceLocation, Advancement.Builder> map) {

		FileSystem filesystem = null;

		try {
			URL url = AdvancementManager.class.getResource("/assets/.mcassetsroot");

			if (url != null) {
				URI uri = url.toURI();
				Path path;

				if ("file".equals(uri.getScheme())) {
					path = Paths.get(CraftingManager.class.getResource("/assets/minecraft/advancements").toURI());
				} else {
					if (!"jar".equals(uri.getScheme())) {
						LOGGER.error("Unsupported scheme {} trying to list all built-in advancements (NYI?)", uri);
						hasErrored = true;
						return;
					}

					filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
					path = filesystem.getPath("/assets/minecraft/advancements");
				}

				Iterator<Path> iterator = Files.walk(path).iterator();

				while (iterator.hasNext()) {
					Path path1 = iterator.next();

					if ("json".equals(FilenameUtils.getExtension(path1.toString()))) {
						Path path2 = path.relativize(path1);
						String s = FilenameUtils.removeExtension(path2.toString()).replaceAll("\\\\", "/");
						ResourceLocation resourcelocation = new ResourceLocation("minecraft", s);

						if (!map.containsKey(resourcelocation)) {
							BufferedReader bufferedreader = null;

							try {
								bufferedreader = Files.newBufferedReader(path1);
								Advancement.Builder advancement$builder = JsonUtils.fromJson(GSON, bufferedreader, Advancement.Builder.class);
								map.put(resourcelocation, advancement$builder);
							} catch (JsonParseException jsonparseexception) {
								LOGGER.error("Parsing error loading built-in advancement {}", resourcelocation, jsonparseexception);
								hasErrored = true;
							} catch (IOException ioexception) {
								LOGGER.error("Couldn't read advancement {} from {}", resourcelocation, path1, ioexception);
								hasErrored = true;
							} finally {
								IOUtils.closeQuietly(bufferedreader);
							}
						}
					}
				}

				return;
			}

			LOGGER.error("Couldn't find .mcassetsroot");
			hasErrored = true;
		} catch (IOException | URISyntaxException urisyntaxexception) {
			LOGGER.error("Couldn't get a list of all built-in advancement files", urisyntaxexception);
			hasErrored = true;
		} finally {
			IOUtils.closeQuietly(filesystem);
		}
	}

	@Nullable
	public Advancement getAdvancement(ResourceLocation id) {

		return ADVANCEMENT_LIST.getAdvancement(id);
	}

	public Iterable<Advancement> getAdvancements() {

		return ADVANCEMENT_LIST.getAdvancements();
	}

}
