package com.stemcraft.core.util;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Utility class for connecting to Mojang servers to get the players name from a UUID.
 */
public class SMUUIDtoNameConverter {
	/**
	 * The URL to connect to
	 */
	private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

	/**
	 * The JSON parser library
	 */
	private final Gson gson = new Gson();

	/**
	 * Attempts to connect to Mojangs servers to retrieve the current player
	 * username from his unique id
	 */
	public String convert(UUID uuid) throws Exception {
		final HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "")).openConnection();
		final JsonObject response = this.gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
		final String name = response.get("name").getAsString();

		if (name == null)
			return "";

		final String cause = response.get("cause").getAsString();
		final String errorMessage = response.get("errorMessage").getAsString();

		if (cause != null && cause.length() > 0)
			throw new IllegalStateException(errorMessage);

		return name;
	}
}
