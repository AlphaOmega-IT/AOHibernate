package de.alphaomegait.aohibernate.database.converter;

import jakarta.persistence.AttributeConverter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UUIDConverter implements AttributeConverter<UUID, String> {

	/**
	 * Converts the given UUID to a string representation.
	 *
	 * @param uuid the UUID to convert
	 *
	 * @return the string representation of the UUID
	 */
	public String convertToDatabaseColumn(
		final @NotNull UUID uuid
	) {
		return uuid.toString();
	}

	/**
	 * Converts the given string representation of a UUID to a UUID object.
	 *
	 * @param uuid the string representation of a UUID
	 *
	 * @return the UUID object
	 */
	public UUID convertToEntityAttribute(
		final @NotNull String uuid
	) {
		return UUID.fromString(uuid);
	}
}