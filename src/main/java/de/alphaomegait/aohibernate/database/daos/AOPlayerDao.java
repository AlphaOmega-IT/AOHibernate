package de.alphaomegait.aohibernate.database.daos;

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
		query.setParameter("uuid", uuid);
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
				"uuid",
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