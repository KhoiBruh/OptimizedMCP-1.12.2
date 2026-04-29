package net.minecraft.client.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class ResourcePackRepository {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final FileFilter RESOURCE_PACK_FILTER = p_accept_1_ -> {

		boolean flag = p_accept_1_.isFile() && p_accept_1_.getName().endsWith(".zip");
		boolean flag1 = p_accept_1_.isDirectory() && (new File(p_accept_1_, "pack.mcmeta")).isFile();
		return flag || flag1;
	};
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private static final ResourceLocation UNKNOWN_PACK_TEXTURE = new ResourceLocation("textures/misc/unknown_pack.png");
	public final IResourcePack rprDefaultResourcePack;
	public final MetadataSerializer rprMetadataSerializer;
	private final File dirResourcepacks;
	private final File dirServerResourcepacks;
	private final ReentrantLock lock = new ReentrantLock();
	private final List<ResourcePackRepository.Entry> repositoryEntries = Lists.newArrayList();
	private IResourcePack serverResourcePack;
	private ListenableFuture<Object> downloadingPacks;
	private List<ResourcePackRepository.Entry> repositoryEntriesAll = Lists.newArrayList();

	public ResourcePackRepository(File dirResourcepacksIn, File dirServerResourcepacksIn, IResourcePack rprDefaultResourcePackIn, MetadataSerializer rprMetadataSerializerIn, GameSettings settings) {

		dirResourcepacks = dirResourcepacksIn;
		dirServerResourcepacks = dirServerResourcepacksIn;
		rprDefaultResourcePack = rprDefaultResourcePackIn;
		rprMetadataSerializer = rprMetadataSerializerIn;
		fixDirResourcepacks();
		updateRepositoryEntriesAll();
		Iterator<String> iterator = settings.resourcePacks.iterator();

		while (iterator.hasNext()) {
			String s = iterator.next();

			for (ResourcePackRepository.Entry resourcepackrepository$entry : repositoryEntriesAll) {
				if (resourcepackrepository$entry.getResourcePackName().equals(s)) {
					if (resourcepackrepository$entry.getPackFormat() == 3 || settings.incompatibleResourcePacks.contains(resourcepackrepository$entry.getResourcePackName())) {
						repositoryEntries.add(resourcepackrepository$entry);
						break;
					}

					iterator.remove();
					LOGGER.warn("Removed selected resource pack {} because it's no longer compatible", resourcepackrepository$entry.getResourcePackName());
				}
			}
		}
	}

	public static Map<String, String> getDownloadHeaders() {

		Map<String, String> map = Maps.newHashMap();
		map.put("X-Minecraft-Username", Minecraft.getMinecraft().getSession().getUsername());
		map.put("X-Minecraft-UUID", Minecraft.getMinecraft().getSession().getPlayerID());
		map.put("X-Minecraft-Version", "1.12.2");
		return map;
	}

	private void fixDirResourcepacks() {

		if (dirResourcepacks.exists()) {
			if (!dirResourcepacks.isDirectory() && (!dirResourcepacks.delete() || !dirResourcepacks.mkdirs())) {
				LOGGER.warn("Unable to recreate resourcepack folder, it exists but is not a directory: {}", dirResourcepacks);
			}
		} else if (!dirResourcepacks.mkdirs()) {
			LOGGER.warn("Unable to create resourcepack folder: {}", dirResourcepacks);
		}
	}

	private List<File> getResourcePackFiles() {

		return dirResourcepacks.isDirectory() ? Arrays.asList(dirResourcepacks.listFiles(RESOURCE_PACK_FILTER)) : Collections.emptyList();
	}

	private IResourcePack getResourcePack(File p_191399_1_) {

		IResourcePack iresourcepack;

		if (p_191399_1_.isDirectory()) {
			iresourcepack = new FolderResourcePack(p_191399_1_);
		} else {
			iresourcepack = new FileResourcePack(p_191399_1_);
		}

		try {
			PackMetadataSection packmetadatasection = iresourcepack.getPackMetadata(rprMetadataSerializer, "pack");

			if (packmetadatasection != null && packmetadatasection.packFormat() == 2) {
				return new LegacyV2Adapter(iresourcepack);
			}
		} catch (Exception ignored) {
		}

		return iresourcepack;
	}

	public void updateRepositoryEntriesAll() {

		List<ResourcePackRepository.Entry> list = Lists.newArrayList();

		for (File file1 : getResourcePackFiles()) {
			ResourcePackRepository.Entry resourcepackrepository$entry = new ResourcePackRepository.Entry(file1);

			if (repositoryEntriesAll.contains(resourcepackrepository$entry)) {
				int i = repositoryEntriesAll.indexOf(resourcepackrepository$entry);

				if (i > -1 && i < repositoryEntriesAll.size()) {
					list.add(repositoryEntriesAll.get(i));
				}
			} else {
				try {
					resourcepackrepository$entry.updateResourcePack();
					list.add(resourcepackrepository$entry);
				} catch (Exception var6) {
					list.remove(resourcepackrepository$entry);
				}
			}
		}

		repositoryEntriesAll.removeAll(list);

		for (ResourcePackRepository.Entry resourcepackrepository$entry1 : repositoryEntriesAll) {
			resourcepackrepository$entry1.closeResourcePack();
		}

		repositoryEntriesAll = list;
	}

	
	public ResourcePackRepository.Entry getResourcePackEntry() {

		if (serverResourcePack != null) {
			ResourcePackRepository.Entry resourcepackrepository$entry = new ResourcePackRepository.Entry(serverResourcePack);

			try {
				resourcepackrepository$entry.updateResourcePack();
				return resourcepackrepository$entry;
			} catch (IOException ignored) {
			}
		}

		return null;
	}

	public List<ResourcePackRepository.Entry> getRepositoryEntriesAll() {

		return ImmutableList.copyOf(repositoryEntriesAll);
	}

	public List<ResourcePackRepository.Entry> getRepositoryEntries() {

		return ImmutableList.copyOf(repositoryEntries);
	}

	public void setRepositories(List<ResourcePackRepository.Entry> repositories) {

		repositoryEntries.clear();
		repositoryEntries.addAll(repositories);
	}

	public File getDirResourcepacks() {

		return dirResourcepacks;
	}

	public ListenableFuture<Object> downloadResourcePack(String url, String hash) {

		String s = DigestUtils.sha1Hex(url);
		final String s1 = SHA1.matcher(hash).matches() ? hash : "";
		final File file1 = new File(dirServerResourcepacks, s);
		lock.lock();

		try {
			clearResourcePack();

			if (file1.exists()) {
				if (checkHash(s1, file1)) {
					return setServerResourcePack(file1);
				}

				LOGGER.warn("Deleting file {}", file1);
				FileUtils.deleteQuietly(file1);
			}

			deleteOldServerResourcesPacks();
			final GuiScreenWorking guiscreenworking = new GuiScreenWorking();
			Map<String, String> map = getDownloadHeaders();
			final Minecraft minecraft = Minecraft.getMinecraft();
			Futures.getUnchecked(minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(guiscreenworking)));
			final SettableFuture<Object> settablefuture = SettableFuture.create();
			downloadingPacks = HttpUtil.downloadResourcePack(file1, url, map, 52428800, guiscreenworking, minecraft.getProxy());
			Futures.addCallback(downloadingPacks, new FutureCallback<>() {
				public void onSuccess(Object p_onSuccess_1_) {

					if (checkHash(s1, file1)) {
						setServerResourcePack(file1);
						settablefuture.set(null);
					} else {
						ResourcePackRepository.LOGGER.warn("Deleting file {}", file1);
						FileUtils.deleteQuietly(file1);
					}
				}

				public void onFailure(Throwable p_onFailure_1_) {

					FileUtils.deleteQuietly(file1);
					settablefuture.setException(p_onFailure_1_);
				}
			}, MoreExecutors.directExecutor());
			return downloadingPacks;
		} finally {
			lock.unlock();
		}
	}

	private boolean checkHash(String p_190113_1_, File p_190113_2_) {

		try {
			String s = DigestUtils.sha1Hex(new FileInputStream(p_190113_2_));

			if (p_190113_1_.isEmpty()) {
				LOGGER.info("Found file {} without verification hash", p_190113_2_);
				return true;
			}

			if (s.toLowerCase(java.util.Locale.ROOT).equals(p_190113_1_.toLowerCase(java.util.Locale.ROOT))) {
				LOGGER.info("Found file {} matching requested hash {}", p_190113_2_, p_190113_1_);
				return true;
			}

			LOGGER.warn("File {} had wrong hash (expected {}, found {}).", p_190113_2_, p_190113_1_, s);
		} catch (IOException ioexception) {
			LOGGER.warn("File {} couldn't be hashed.", p_190113_2_, ioexception);
		}

		return false;
	}

	private boolean validatePack(File p_190112_1_) {

		ResourcePackRepository.Entry resourcepackrepository$entry = new ResourcePackRepository.Entry(p_190112_1_);

		try {
			resourcepackrepository$entry.updateResourcePack();
			return true;
		} catch (Exception exception) {
			LOGGER.warn("Server resourcepack is invalid, ignoring it", exception);
			return false;
		}
	}

	/**
	 * Keep only the 10 most recent resources packs, delete the others
	 */
	private void deleteOldServerResourcesPacks() {

		try {
			List<File> list = Lists.newArrayList(FileUtils.listFiles(dirServerResourcepacks, TrueFileFilter.TRUE, null));
			list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
			int i = 0;

			for (File file1 : list) {
				if (i++ >= 10) {
					LOGGER.info("Deleting old server resource pack {}", file1.getName());
					FileUtils.deleteQuietly(file1);
				}
			}
		} catch (IllegalArgumentException illegalargumentexception) {
			LOGGER.error("Error while deleting old server resource pack : {}", illegalargumentexception.getMessage());
		}
	}

	public ListenableFuture<Object> setServerResourcePack(File resourceFile) {

		if (!validatePack(resourceFile)) {
			return Futures.immediateFailedFuture(new RuntimeException("Invalid resourcepack"));
		} else {
			serverResourcePack = new FileResourcePack(resourceFile);
			return Minecraft.getMinecraft().scheduleResourcesRefresh();
		}
	}

	

	/**
	 * Getter for the IResourcePack instance associated with this ResourcePackRepository
	 */
	public IResourcePack getServerResourcePack() {

		return serverResourcePack;
	}

	public void clearResourcePack() {

		lock.lock();

		try {
			if (downloadingPacks != null) {
				downloadingPacks.cancel(true);
			}

			downloadingPacks = null;

			if (serverResourcePack != null) {
				serverResourcePack = null;
				Minecraft.getMinecraft().scheduleResourcesRefresh();
			}
		} finally {
			lock.unlock();
		}
	}

	public class Entry {

		private final IResourcePack reResourcePack;
		private PackMetadataSection rePackMetadataSection;
		private ResourceLocation locationTexturePackIcon;

		private Entry(File resourcePackFileIn) {

			this(ResourcePackRepository.this.getResourcePack(resourcePackFileIn));
		}

		private Entry(IResourcePack reResourcePackIn) {

			reResourcePack = reResourcePackIn;
		}

		public void updateResourcePack() throws IOException {

			rePackMetadataSection = reResourcePack.getPackMetadata(rprMetadataSerializer, "pack");
			closeResourcePack();
		}

		public void bindTexturePackIcon(TextureManager textureManagerIn) {

			BufferedImage bufferedimage = null;

			try {
				bufferedimage = reResourcePack.getPackImage();
			} catch (IOException ignored) {
			}

			if (bufferedimage == null) {
				try {
					bufferedimage = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(ResourcePackRepository.UNKNOWN_PACK_TEXTURE).getInputStream());
				} catch (IOException ioexception) {
					throw new Error("Couldn't bind resource pack icon", ioexception);
				}
			}

			if (locationTexturePackIcon == null) {
				locationTexturePackIcon = textureManagerIn.getDynamicTextureLocation("texturepackicon", new DynamicTexture(bufferedimage));
			}

			textureManagerIn.bindTexture(locationTexturePackIcon);
		}

		public void closeResourcePack() {

			if (reResourcePack instanceof Closeable) {
				IOUtils.closeQuietly((Closeable) reResourcePack);
			}
		}

		public IResourcePack getResourcePack() {

			return reResourcePack;
		}

		public String getResourcePackName() {

			return reResourcePack.getPackName();
		}

		public String getTexturePackDescription() {

			return rePackMetadataSection == null ? TextFormatting.RED + "Invalid pack.mcmeta (or missing 'pack' section)" : rePackMetadataSection.packDescription().getFormattedText();
		}

		public int getPackFormat() {

			return rePackMetadataSection == null ? 0 : rePackMetadataSection.packFormat();
		}

		public boolean equals(Object p_equals_1_) {

			if (this == p_equals_1_) {
				return true;
			} else {
				return p_equals_1_ instanceof Entry && toString().equals(p_equals_1_.toString());
			}
		}

		public int hashCode() {

			return toString().hashCode();
		}

		public String toString() {

			return String.format("%s:%s", reResourcePack.getPackName(), reResourcePack instanceof FolderResourcePack ? "folder" : "zip");
		}

	}

}
