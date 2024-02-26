package de.alphaomegait.aohibernate.config;

import de.alphaomegait.aohibernate.AOHibernate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

public class DatabaseConfigurationSection {

	private String host;

	private Integer port;

	private String databaseName;

	private String tableCreation;

	private String username;

	private String password;

	private Boolean showSQL;

	private String entityAnnotatedClassParentPath;

	private final YamlConfiguration yamlConfiguration;

	public DatabaseConfigurationSection(
		final @NotNull AOHibernate aoHibernate
	) {
		this.yamlConfiguration = new YamlConfiguration();

		try {
			yamlConfiguration.load(new File(aoHibernate.getDataFolder() + "/database-config.yml"));
		} catch (
			final Exception exception
		) {
			aoHibernate.getLogger().log(
				Level.SEVERE,
				"Failed to load database-config.yml",
				exception
			);
		}
	}

	/**
	 * Get the host value, or an empty string if it is null.
	 *
	 * @return the host value or an empty string
	 */
	public String getHost() {
		this.host = this.yamlConfiguration.getString("host", "");
		return this.host;
	}

	/**
	 * Retrieves the port number. If the port is not set, the default port 3306 is returned.
	 *
	 * @return  the port number
	 */
	public Integer getPort() {
		this.port = this.yamlConfiguration.getInt("port", 3306);
		return this.port;
	}

	/**
	 * Retrieves the database name. If the database name is null, it returns an
	 * empty string.
	 *
	 * @return  the database name
	 */
	public String getDatabaseName() {
		this.databaseName = this.yamlConfiguration.getString("database-name", "");
		return this.databaseName;
	}

	/**
	 * Gets the table creation value.
	 *
	 * @return  the table creation value, or "update" if it is null
	 */
	public String getTableCreation() {
		this.tableCreation = this.yamlConfiguration.getString("table-creation", "update");
		return this.tableCreation;
	}

	/**
	 * Returns the username, or "root" if the username is null.
	 *
	 * @return  the username or "root" if null
	 */
	public String getUsername() {
		this.username = this.yamlConfiguration.getString("username", "root");
		return this.username;
	}

	/**
	 * Retrieves the password.
	 *
	 * @return         the password if not null, otherwise an empty string
	 */
	public String getPassword() {
		this.password = this.yamlConfiguration.getString("password", "");
		return this.password;
	}

	/**
	 * Returns a string representation of the value of showSQL as "true" or "false".
	 *
	 * @return         	a string representation of the value of showSQL
	 */
	public Boolean getIsShowSQL() {
		this.showSQL = this.yamlConfiguration.getBoolean("show-sql", true);
		return this.showSQL;
	}

	/**
	 * Retrieves the parent path of the entity annotated class.
	 *
	 * @return         	the parent path of the entity annotated class
	 */
	public String getEntityAnnotatedClassParentPath() {
		this.entityAnnotatedClassParentPath = this.yamlConfiguration.getString("entity-annotated-class-parent-path", "database.entities.");
		return this.entityAnnotatedClassParentPath;
	}

	/**
	 * Returns the connection URL for the database.
	 *
	 * @return  the connection URL
	 */
	public String getConnectionURL() {
		return "jdbc:mysql://" + this.getHost() + ":" + this.getPort() + "/" + this.getDatabaseName() + "?useUnicode=true";
	}
}