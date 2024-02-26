package de.alphaomegait.aohibernate.database;

import com.google.common.reflect.ClassPath;
import de.alphaomegait.aohibernate.AOHibernate;
import de.alphaomegait.aohibernate.config.DatabaseConfigurationSection;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to create a database connection and build a session factory.
 */
public class DatabaseFactory implements IDatabaseProvider {

	private Configuration configuration;

	private final Logger logger;

	private final DatabaseConfigurationSection databaseConfigurationSection;

	/**
	 * Constructs a DatabaseFactory with the provided configManager, classLoader, and logger.
	 * Initializes the database configuration section, classLoader, and logger. Calls the connect method to establish a database connection.
	 * @param configManager the configuration manager for retrieving database settings
	 * @param logger the logger for logging messages
	 * @throws Exception if an error occurs during the initialization process
	 */
	public DatabaseFactory(
		final @NotNull AOHibernate aoHibernate
	) {
		this.databaseConfigurationSection = new DatabaseConfigurationSection(aoHibernate);
		this.logger = aoHibernate.getLogger();
		this.connect();
	}

	/**
	 * This method establishes a connection to the database using the configuration settings.
	 * It sets up the properties for the database connection and initializes the configuration.
	 * @throws Exception if an error occurs during the database connection process.
	 */
	@Override
	public void connect() {
		Properties properties = new Properties();
		properties.setProperty(
			Environment.DRIVER,
			"com.mysql.cj.jdbc.Driver"
		);
		properties.setProperty(
			Environment.USER,
			this.databaseConfigurationSection.getUsername()
		);
		properties.setProperty(
			Environment.PASS,
			this.databaseConfigurationSection.getPassword()
		);
		properties.setProperty(
			Environment.HBM2DDL_AUTO,
			this.databaseConfigurationSection.getTableCreation()
		);
		properties.setProperty(
			Environment.URL,
			this.databaseConfigurationSection.getConnectionURL()
		);
		properties.setProperty(
			Environment.SHOW_SQL,
			String.valueOf(this.databaseConfigurationSection.getIsShowSQL())
		);
		properties.setProperty(
			Environment.AUTOCOMMIT,
			"true"
		);
		properties.setProperty(
			Environment.AUTO_CLOSE_SESSION,
			"true"
		);

		this.configuration = new Configuration().addProperties(properties);
		this.includeAnnotatedClasses();
	}


	/**
	 * This method includes annotated classes based on the entity annotated class parent path.
	 * It uses ClassPath to get the top-level classes and then filters them to include only classes with the @Entity annotation.
	 * Once the annotated classes are identified, they are loaded and added to the configuration.
	 * @throws Exception if an error occurs during the process of including annotated classes.
	 */
	@Override
	public void includeAnnotatedClasses() {
		try {
			ClassPath.from(this.getClass().getClassLoader())
							 .getAllClasses().stream().filter(
								 classInfo -> classInfo.getPackageName().contains(this.databaseConfigurationSection.getEntityAnnotatedClassParentPath()
								 )).toList().stream()
							 .map(ClassPath.ClassInfo::load)
							 .forEach(clazz -> {
								 try {
									 this.getClass().getClassLoader().loadClass(clazz.getName());
									 this.configuration.addAnnotatedClass(clazz);
								 } catch (
									 final Exception exception
								 ) {
									 this.logger.log(
										 Level.SEVERE,
										 "An exception occurred while loading the class: " + clazz.getName(),
										 exception
									 );
								 }
							 });
		} catch (
			final Exception exception
		) {
			this.logger.log(
				Level.SEVERE,
				"An exception occurred while including annotated classes",
				exception
			);
		}
	}

	/**
	 * Builds the session factory.
	 *
	 * @return the built session factory or null if the configuration is null.
	 */
	@Nullable
	@Override
	public SessionFactory buildSessionFactory() {
		return this.configuration == null ? null : this.configuration.buildSessionFactory();
	}
}