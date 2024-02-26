package de.alphaomegait.aohibernate.database.entities;

import de.alphaomegait.aohibernate.database.converter.UUIDConverter;
import jakarta.persistence.*;
import org.bukkit.entity.Player;
import org.hibernate.annotations.NamedQuery;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Entity
@NamedQuery(
	name = "AOPlayer.findAll",
	query = "SELECT p FROM AOPlayer p"
)
@NamedQuery(
	name = "AOPlayer.findByUUID",
	query = "SELECT p FROM AOPlayer p WHERE p.playerUUID = :uuid"
)
@Table(name = "ao_player")
public class AOPlayer implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(
		name = "player_id",
		unique = true,
		nullable = false
	)
	private Long id;

	@Column(
		name = "player_uuid",
		unique = true,
		nullable = false
	)
	@Convert(
		converter = UUIDConverter.class
	)
	private UUID playerUUID;

	@Column(
		name = "player_name",
		nullable = false
	)
	private String playerName;

	@Column(
		name = "created_at"
	)
	private String createdAt = new SimpleDateFormat(
		"MM-dd-yyyy HH:mm:ss",
		new Locale(
			"de",
			"DE"
		)
	).format(new Date());

	public AOPlayer() {

	}

	public AOPlayer(
		final @NotNull Player player
	) {
		this.playerUUID = player.getUniqueId();
		this.playerName = player.getName();
	}

	public Long getId() {
		return this.id;
	}

	public UUID getPlayerUUID() {
		return this.playerUUID;
	}

	public void setPlayerName(final @NotNull String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public String getCreatedAt() {
		return this.createdAt;
	}
}