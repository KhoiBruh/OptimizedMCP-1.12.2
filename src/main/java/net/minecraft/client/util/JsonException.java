package net.minecraft.client.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class JsonException extends IOException {

	private final List<JsonException.Entry> entries = Lists.newArrayList();
	private final String message;

	public JsonException(String messageIn) {

		entries.add(new JsonException.Entry());
		message = messageIn;
	}

	public JsonException(String messageIn, Throwable cause) {

		super(cause);
		entries.add(new JsonException.Entry());
		message = messageIn;
	}

	public void prependJsonKey(String key) {

		entries.get(0).addJsonKey(key);
	}

	public void setFilenameAndFlush(String filenameIn) {

		(entries.get(0)).filename = filenameIn;
		entries.add(0, new JsonException.Entry());
	}

	public String getMessage() {

		return "Invalid " + entries.get(entries.size() - 1) + ": " + message;
	}

	public static JsonException forException(Exception exception) {

		if (exception instanceof JsonException) {
			return (JsonException) exception;
		} else {
			String s = exception.getMessage();

			if (exception instanceof FileNotFoundException) {
				s = "File not found";
			}

			return new JsonException(s, exception);
		}
	}

	public static class Entry {

		private String filename;
		private final List<String> jsonKeys;

		private Entry() {

			jsonKeys = Lists.newArrayList();
		}

		private void addJsonKey(String key) {

			jsonKeys.add(0, key);
		}

		public String getJsonKeys() {

			return StringUtils.join(jsonKeys, "->");
		}

		public String toString() {

			if (filename != null) {
				return jsonKeys.isEmpty() ? filename : filename + " " + getJsonKeys();
			} else {
				return jsonKeys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + getJsonKeys();
			}
		}

	}

}
