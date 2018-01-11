package se.iteration.hans.angular.vehicle.service;

import se.iteration.hans.angular.vehicle.model.Vehicle;
import se.iteration.hans.angular.vehicle.vo.VehicleVO;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class VehicleRegistration {
	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<Vehicle> vehicleEventSrc;

	public void register(Vehicle vehicle) throws Exception {
		log.info("Registering " + vehicle.getName());
		em.persist(vehicle);
		vehicleEventSrc.fire(vehicle);
	}
	

	public VehicleVO updateVehicle(VehicleVO vehicleVO) {
		Vehicle vehicle = em.find(Vehicle.class, vehicleVO.getId());
		if (vehicle != null) {
			vehicle.setName(vehicleVO.getName());
			vehicle.setType(vehicleVO.getType());
			vehicle.setMass(vehicleVO.getMass());
			em.merge(vehicle);
			vehicleEventSrc.fire(vehicle);
			return vehicleVO;
		} else {
			return null;
		}
	}
}
