package se.iteration.hans.angular.vehicle.test;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import se.iteration.hans.angular.vehicle.model.Vehicle;
import se.iteration.hans.angular.vehicle.service.VehicleRegistration;
import se.iteration.hans.angular.vehicle.util.Resources;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class VehicleRegistrationTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Vehicle.class, VehicleRegistration.class, Resources.class)
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                // Deploy our test datasource
                .addAsWebInfResource("test-ds.xml");
    }

    @Inject
    VehicleRegistration vehicleRegistration;

    @Inject
    Logger log;

    @Test
    public void testRegister() throws Exception {
        Vehicle newVehicle = new Vehicle();
        newVehicle.setName("Roadster 2");
        newVehicle.setType("Sportscar");
        newVehicle.setMass(1);
        vehicleRegistration.register(newVehicle);
        assertNotNull(newVehicle.getId());
        log.info(newVehicle.getName() + " was persisted with id " + newVehicle.getId());
    }

}
