package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Session;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

	private static final String DEFAULT_USER_TYPE = "legacy";
	private static final String DEFAULT_VERSION_TYPE = "release";
	private static final String DEFAULT_PROPERTIES = "{}";
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(PropertyMap.class, new Serializer())
		.create();

	public static void main(String[] args) {
		LaunchOptions options = parseLaunchOptions(args);
		System.out.println("Completely ignored arguments: " + options.unmatcheds);

		Proxy proxy = createProxy(options.proxyHost, options.proxyPort);
		proxyAuth(proxy, options.proxyUser, options.proxyPass);

		Session session = new Session(options.username, options.uuid, options.accessToken, options.userType);
		PropertyMap userProperties = JsonUtils.gsonDeserialize(GSON, options.userProperties, PropertyMap.class);
		PropertyMap profileProperties = JsonUtils.gsonDeserialize(GSON, options.profileProperties, PropertyMap.class);

		GameConfig config = new GameConfig(
			new GameConfig.User(session, userProperties, profileProperties, proxy),
			new GameConfig.Display(options.width, options.height, options.fullscreen),
			new GameConfig.Folder(options.gameDir, options.resourcePackDir, options.assetsDir, options.assetIndex),
			new GameConfig.Game(options.version, options.versionType)
		);

		Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
			public void run() {
				Minecraft.stopIntegratedServer();
			}
		});

		Thread.currentThread().setName("Client Thread");
		new Minecraft(config).run();
	}

	private static LaunchOptions parseLaunchOptions(String[] args) {
		LaunchOptions options = new LaunchOptions();
		new CommandLine(options).setUnmatchedArgumentsAllowed(true).parseArgs(args);
		options.useDefault();
		return options;
	}

	private static Proxy createProxy(String host, int port) {
		if (host == null) return Proxy.NO_PROXY;

		try {
			return new Proxy(Type.SOCKS, new InetSocketAddress(host, port));
		} catch (Exception ignored) {
			return Proxy.NO_PROXY;
		}
	}

	private static void proxyAuth(Proxy proxy, String username, String password) {
		if (!proxy.equals(Proxy.NO_PROXY) && isNotNullOrEmpty(username) && isNotNullOrEmpty(password)) {
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});
		}
	}

	private static boolean isNotNullOrEmpty(String str) {
		return str != null && !str.isEmpty();
	}

	private static class LaunchOptions {

		@Unmatched
		List<String> unmatcheds = new ArrayList<>();

		@Option(names = "--gameDir", defaultValue = ".")
		File gameDir;

		@Option(names = "--assetsDir")
		File assetsDir;

		@Option(names = "--resourcePackDir")
		File resourcePackDir;

		@Option(names = "--proxyHost")
		String proxyHost;

		@Option(names = "--proxyPort", defaultValue = "8080")
		int proxyPort;

		@Option(names = "--proxyUser")
		String proxyUser;

		@Option(names = "--proxyPass")
		String proxyPass;

		@Option(names = "--username")
		String username;

		@Option(names = "--uuid")
		String uuid;

		@Option(names = "--accessToken", defaultValue = "0")
		String accessToken;

		@Option(names = "--version", required = true)
		String version;

		@Option(names = "--width", defaultValue = "854")
		int width;

		@Option(names = "--height", defaultValue = "480")
		int height;

		@Option(names = "--fullscreen")
		boolean fullscreen;

		@Option(names = "--userProperties", defaultValue = DEFAULT_PROPERTIES)
		String userProperties;

		@Option(names = "--profileProperties", defaultValue = DEFAULT_PROPERTIES)
		String profileProperties;

		@Option(names = "--assetIndex")
		String assetIndex;

		@Option(names = "--userType", defaultValue = DEFAULT_USER_TYPE)
		String userType;

		@Option(names = "--type", defaultValue = DEFAULT_VERSION_TYPE)
		String versionType;

		private void useDefault() {
			if (username == null) username = "Player" + Minecraft.getSystemTime() % 1000L;
			if (uuid == null) uuid = username;
			if (assetsDir == null) assetsDir = new File(gameDir, "assets/");
			if (resourcePackDir == null) resourcePackDir = new File(gameDir, "resourcepacks/");
		}

	}

}
