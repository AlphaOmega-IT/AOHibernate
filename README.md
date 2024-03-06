Hello everyone, a long time passed, and for those who are still looking for Hibernate / Spigot / Paper content, I'm updating my version of Hibernate since I'm still using it. If you want to use it by yourself but do not want to have a huge .jar file size. I can do another Thread where I give an example of the dynamic dependency loader for version 1.8-1.20.4 (each Java version works from 8-21).

Old Version: Resource - Setup Hibernate Framework for your Minecraft Plugin | SpigotMC - High Performance Minecraft

The setup of the hibernate framework for a Minecraft Plugin can be a struggle. You can find below many examples + a full tutorial for your own Plugin. The updated code can be found here: aohibernate

First of all, you have to add the following dependencies to your Maven project to get access to the hibernate library:
    [code]
    <dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.1.5.Final</version>
    <scope>compile</scope>
    </dependency>

    <dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.30</version>
    <scope>compile</scope>
    </dependency>
    [/code]

    Now I create a DatabaseFactory Java Class where the main functionalities regarding connection and settings happen.
  These are as following:

    [code=Java]
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

    public class DatabaseFactory implements IDatabaseProvider {

    private Configuration configuration;

    private final Logger logger;

    private final DatabaseConfigurationSection databaseConfigurationSection;

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
    @Nullable
    @Override
    public SessionFactory buildSessionFactory() {
  return this.configuration == null ? null : this.configuration.buildSessionFactory(); //build the sessionfactory
  }
  }
    [/code]

  The interface of the DatabaseProvider looks as follows:
    [code=Java]
    public interface IDatabaseProvider {

    void connect() throws Exception;

    /**
    * Include annotated classes.
    *
    * @throws Exception
    */
    void includeAnnotatedClasses() throws Exception;

    @Nullable
    SessionFactory buildSessionFactory();
  }
    [/code]

  Now we are going to create the BaseDao where all Dao's extend from:
    [code=Java]

    import de.alphaomegait.aohibernate.AOHibernate;
    import de.alphaomegait.aohibernate.database.DatabaseFactory;
    import org.hibernate.FlushMode;
    import org.hibernate.Session;
    import org.hibernate.SessionFactory;
    import org.hibernate.query.Query;
    import org.jetbrains.annotations.NotNull;
    import org.jetbrains.annotations.Nullable;

    import java.util.logging.Level;
    import java.util.logging.Logger;

    public abstract class BaseDao<T> {

    private Session session;
    private final Logger logger;
    private final AOHibernate aoHibernate;

    public BaseDao(
    final @NotNull AOHibernate aoHibernate
    ) {
    this.aoHibernate = aoHibernate;
    this.logger = this.aoHibernate.getLogger();
    }

    /**
    * Persists the given entity into the database.
    *
    * @param data the entity to be persisted
    */
    public void persistEntity(
    final T data
    ) {
    try (final Session session = this.getOrCreateSession()) {
    session.beginTransaction();
    session.persist(data);
    session.flush();
    session.getTransaction().commit();
    } catch (
    final Exception exception
    ) {
    this.logger.log(
    Level.SEVERE,
    "Failed to persist entity",
    exception
    );
    } finally {
    session.close();
    }
    }

    /**
    * Get or create a session.
    *
    * @return the session object
    */
    public Session getOrCreateSession() {
    final SessionFactory sessionFactory = this.getSessionFactory();
    if (sessionFactory == null)
    return null;

    if (this.session == null)
    this.session = sessionFactory.withOptions().flushMode(FlushMode.AUTO).openSession();

  return !this.session.isOpen() ? this.getSessionFactory().withOptions().flushMode(FlushMode.AUTO).openSession() : this.session;
  }

    public @Nullable Session getSession() {
    return this.session;
  }

    /**
    * Removes the specified entity from the database.
    *
    * @param data the entity to be removed
    */
    public void removeEntity(
    final T data
    ) {
    try (final Session session = this.getOrCreateSession()) {
    session.beginTransaction();
    session.remove(data);
    session.getTransaction().commit();
  } catch (final Exception exception) {
    if (
    session.getTransaction() != null &&
    session.getTransaction().isActive()
    ) session.getTransaction().rollback();
    this.logger.log(
    Level.SEVERE,
    "Failed to remove entity",
    exception
    );
  } finally {
    session.close();
  }
  }

    /**
    * Creates and returns a named query of type Query based on the provided query name.
    *
    * @param queryName the name of the query
    *
    * @return the created named query
    */
    protected Query<T> createNamedQuery(
    final @NotNull String queryName
    ) {
    return this.getOrCreateSession().createNamedQuery(
    queryName,
    this.getClazzType()
    );
  }

    protected abstract Class<T> getClazzType();

    /**
    * Retrieves the session factory.
    *
    * @return           the session factory, or null if it cannot be retrieved
    */
    private @Nullable SessionFactory getSessionFactory() {
    final DatabaseFactory databaseFactory = new DatabaseFactory(this.aoHibernate);
    return databaseFactory.buildSessionFactory();
  }
  }
[/code]

After that, we are ready to persist and delete data from the given database! Caution: Updating using the merge function can be a bit critical so it's better to create your update methods using Criteria or HQL functions. Since we are using in this example Converter to convert a UUID automatically in a string, it's for us possible to use the Criteria function, Depending on the query and the converter, Hibernate is not able to convert the data between the update. So you would have to use the query and do it manually so it gets converted by Hibernate.

[code=Java]
import de.alphaomegait.aohibernate.AOHibernate;
import de.alphaomegait.aohibernate.database.entities.AOPlayer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AOPlayerDao extends BaseDao<AOPlayer> {

    private final Logger logger;

    public AOPlayerDao(
    final @NotNull AOHibernate aoHibernate
    ) {
    super(aoHibernate);
    this.logger = aoHibernate.getLogger();
    }

    public Optional<AOPlayer> findByUUID(
    final @NotNull UUID uuid
    ) {
    Query<AOPlayer> query = this.createNamedQuery("AOPlayer.findByUUID");
    query.setParameter("playerUUID", uuid);
    return Optional.ofNullable(query.getSingleResultOrNull());
    }

    public List<AOPlayer> findAll() {
    Query<AOPlayer> query = this.createNamedQuery("AOPlayer.findAll");
    return query.getResultList();
    }

    public void updateCriteria(
    final @NotNull AOPlayer player,
    final @NotNull Long idToUpdate
    ) {
    try (Session session = this.getOrCreateSession()) {
    final CriteriaBuilder criteriaBuilder         = session.getCriteriaBuilder();
    final CriteriaUpdate<AOPlayer> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(AOPlayer.class);
    final Root<AOPlayer> root                     = criteriaUpdate.from(AOPlayer.class);

    criteriaUpdate.where(criteriaBuilder.equal(
    root.get("id"),
    idToUpdate
    ));
    criteriaUpdate.set(
    "playerUUID",
    player.getPlayerUUID()
    );
    criteriaUpdate.set(
    "playerName",
    player.getPlayerName()
    );

    //if (! player.getHomes().isEmpty())
    //criteriaUpdate.set("homes", player.getHomes());

    session.beginTransaction();
    session.createMutationQuery(criteriaUpdate).executeUpdate();
    session.getTransaction().commit();
    } catch (
    final Exception exception
    ) {
    this.logger.log(
    Level.SEVERE,
    "Failed to update entity",
    exception
    );
    }
    }

    public void updateHQL(
    final AOPlayer aoPlayer,
    final Long idToUpdate
    ) {
    try (final Session session = this.getOrCreateSession()) {
    final String hqlQuery =
    "UPDATE AOPlayer SET " +
    "playerUUID = :playerUUID, " +
    "playerName = :playerName " +
    "WHERE id = :id";

    session.beginTransaction();
    session.createMutationQuery(hqlQuery)
    .setParameter("playerUUID", aoPlayer.getPlayerUUID())
    .setParameter("playerName", aoPlayer.getPlayerName())
    .setParameter("id", idToUpdate)
    .executeUpdate();
    session.getTransaction().commit();
    } catch (
    final Exception exception
    ) {
    this.logger.log(
    Level.SEVERE,
    exception.getMessage(),
    exception
    );
    }
    }

    @Override
    protected Class<AOPlayer> getClazzType() {
    return AOPlayer.class;
    }
  }
    [/code]

  Last but not least we need of course as well the Entity class which could look like the following:
[code=Java]
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
    query = "SELECT p FROM AOPlayer p WHERE p.playerUUID = :playerUUID"
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
    [/code]

  The UUID Converter looks like following:
    [code=Java]
    package de.alphaomegait.aohibernate.database.converter;

    import jakarta.persistence.AttributeConverter;
    import org.jetbrains.annotations.NotNull;

    import java.util.UUID;

    public class UUIDConverter implements AttributeConverter<UUID, String> {

    public String convertToDatabaseColumn(
    final @NotNull UUID uuid
    ) {
    return uuid.toString();
    }

    public UUID convertToEntityAttribute(
    final @NotNull String uuid
    ) {
    return UUID.fromString(uuid);
    }
  }
    [/code]

Finally, we will create a test for our Database, I will simply create a Join Event where my Player gets inserted into the database and after the third join updated. It could look like the following:
    [code=Java]
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
[/code]

Simply the Main Class to register the databasefactory and the event:
[code=Java]
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
    [/code]

Last but not least the database-config.yml file from the AOHibernate.java class (It should be simply placed into the resource folder):
    [code]

  host: 'localhost'
  port: 3306
  databaseName: 'aohibernate'
  tableCreation: 'update'
  username: 'root'
  password: ''
  showSQL: false
  entityAnnotatedClassParentPath: 'database.entities'
[/code]

Don't miss the plugin.yml file and you are ready to go! I hope this helps you a bit more to understand the basics of hibernate and databases overall! If you have further questions or want to see how you fully reduce the size of your Jar File, let me know.

Soon there will be a discord Server as well for things like that including my newest project which has been working on for over 2 years.
My Discord is justin300100 in case you need further assistance.

Best Regards
AlphaOmega-IT
