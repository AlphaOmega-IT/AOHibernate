package de.alphaomegait.aohibernate;

import de.alphaomegait.aohibernate.database.DatabaseFactory;
import de.alphaomegait.aohibernate.database.daos.AOPlayerDao;
import de.alphaomegait.aohibernate.listener.OnJoin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class AOHibernate extends JavaPlugin {

	private final Logger logger = Logger.getLogger(AOHibernate.class.getName());

	private DatabaseFactory databaseFactory;

	private AOPlayerDao aoPlayerDao;

	@Override
	public void onLoad() {
		if (
			this.getDataFolder().mkdir()
		) this.logger.info("Plugin folder got created.");

		this.saveResource("database-config.yml", false);
	}

	@Override
	public void onDisable() {
		this.logger.info("Plugin disabled.");
	}

	@Override
	public void onEnable() {
		this.databaseFactory = new DatabaseFactory(this);
		this.aoPlayerDao = new AOPlayerDao(this);

		Bukkit.getPluginManager().registerEvents(
			new OnJoin(this), this
		);

		this.logger.info("Plugin enabled.");
	}

	@NotNull
	@Override
	public Logger getLogger() {
		return this.logger;
	}

	public AOPlayerDao getAoPlayerDao() {
		return this.aoPlayerDao;
	}
}