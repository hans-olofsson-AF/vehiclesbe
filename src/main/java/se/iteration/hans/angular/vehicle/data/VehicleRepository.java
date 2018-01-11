package se.iteration.hans.angular.vehicle.data;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import se.iteration.hans.angular.vehicle.model.Vehicle;
import se.iteration.hans.angular.vehicle.vo.VehicleVO;

@ApplicationScoped
public class VehicleRepository {
	@Inject
	private EntityManager em;

	public Vehicle findById(Long id) {
		return em.find(Vehicle.class, id);
	}

	public Vehicle findByName(String name) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Vehicle> criteria = cb.createQuery(Vehicle.class);
		Root<Vehicle> vehicle = criteria.from(Vehicle.class);
		criteria.select(vehicle).where(cb.equal(vehicle.get("name"), name));
		return em.createQuery(criteria).getSingleResult();
	}

	public List<Vehicle> findAllOrderedByName() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Vehicle> criteria = cb.createQuery(Vehicle.class);
		Root<Vehicle> vehicle = criteria.from(Vehicle.class);
		criteria.select(vehicle).orderBy(cb.asc(vehicle.get("name")));
		return em.createQuery(criteria).getResultList();
	}
}
