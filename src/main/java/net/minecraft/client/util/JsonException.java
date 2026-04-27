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

	public void prependJsonKey(String key) {

		entries.getFirst().addJsonKey(key);
	}

	public void setFilenameAndFlush(String filenameIn) {

		(entries.getFirst()).filename = filenameIn;
		entries.addFirst(new JsonException.Entry());
	}

	public String getMessage() {

		return "Invalid " + entries.getLast() + ": " + message;
	}

	public static class Entry {

		private final List<String> jsonKeys;
		private String filename;

		private Entry() {

			jsonKeys = Lists.newArrayList();
		}

		private void addJsonKey(String key) {

			jsonKeys.addFirst(key);
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
