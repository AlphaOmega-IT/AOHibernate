package de.alphaomegait.aohibernate.listener;

import de.alphaomegait.aohibernate.AOHibernate;
import de.alphaomegait.aohibernate.database.entities.AOPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OnJoin implements Listener {

	private final AOHibernate aoHibernate;
	private int counter;

	public OnJoin(
		final @NotNull AOHibernate aoHibernate
	) {
		this.aoHibernate = aoHibernate;
	}

	@EventHandler(
		priority = EventPriority.LOWEST
	)
	public void onJoin(
		final @NotNull PlayerJoinEvent event
	) {
		final Player player = event.getPlayer();

		counter++;

		CompletableFuture.runAsync(() -> {
			final Optional<AOPlayer> aoPlayer = this.aoHibernate.getAoPlayerDao().findByUUID(player.getUniqueId());

			if (aoPlayer.isEmpty()) {
				final AOPlayer newPlayer = new AOPlayer(player);
				this.aoHibernate.getAoPlayerDao().persistEntity(
					newPlayer
				);

				player.sendMessage("§aYou have been added to the database.");
				player.sendMessage("§5Name: " + newPlayer.getPlayerName());
				player.sendMessage("§5UUID: " + newPlayer.getPlayerUUID());
				return;
			}

			player.sendMessage("§cYou are already in the database.");
			player.sendMessage("§5Name: " + aoPlayer.get().getPlayerName());
			player.sendMessage("§5UUID: " + aoPlayer.get().getPlayerUUID());

			if (counter == 3) {
				aoPlayer.get().setPlayerName("updatedName");
				this.aoHibernate.getAoPlayerDao().updateCriteria(
					aoPlayer.get(),
					aoPlayer.get().getId()
				);
				player.sendMessage("§aYou have been updated.");
				player.sendMessage("§5Name: " + aoPlayer.get().getPlayerName());
				player.sendMessage("§5UUID: " + aoPlayer.get().getPlayerUUID());
			}
		}).join();
	}
}