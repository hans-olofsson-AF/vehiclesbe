package se.iteration.hans.angular.vehicle.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import se.iteration.hans.angular.vehicle.data.VehicleRepository;
import se.iteration.hans.angular.vehicle.model.Vehicle;
import se.iteration.hans.angular.vehicle.service.VehicleRegistration;
import se.iteration.hans.angular.vehicle.vo.VehicleVO;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the
 * vehicle table.
 */
@Path("/vehicles")
@RequestScoped
public class VehicleResourceRESTService {
	@Inject
	private Logger log;

	@Inject
	private Validator validator;

	@Inject
	private VehicleRepository repository;

	@Inject
	VehicleRegistration registration;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAllVehicles() {
		log.info("listAllVehicles() called");
		return responseOk(repository.findAllOrderedByName());
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response lookupVehicleById(@PathParam("id") long id) {
		Vehicle vehicle = repository.findById(id);
		if (vehicle == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return responseOk(vehicle);
	}

	private Response responseOk(Object object) {
		return Response.status(Response.Status.OK).entity(object).build();
		//return responseBuilder(object, Response.Status.OK);
	}

	private Response responseBadRequest(Object object) {
		return Response.status(Response.Status.BAD_REQUEST).entity(object).build();
		//return responseBuilder(object, Response.Status.BAD_REQUEST);
	}

	private Response responseBuilder(Object object, Status status) {
		return Response.status(status).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").entity(object).build();
	}

	/**
	 * Creates a new vehicle from the values provided. Performs validation, and will
	 * return a JAX-RS response with either 200 ok, or with a map of fields, and
	 * related errors.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVehicle(Vehicle vehicle) {
		Response.ResponseBuilder builder = null;
		try {
			// Validates vehicle using bean validation
			validateVehicle(vehicle);

			registration.register(vehicle);

			// Create an "ok" response
			builder = Response.ok();
		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			builder = createViolationResponse(ce.getConstraintViolations());
		} catch (ValidationException e) {
			// Handle the unique constrain violation
			Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("name", "Name taken");
			builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
		} catch (Exception e) {
			// Handle generic exceptions
			Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("error", e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateVehicle(VehicleVO vehicleVO) {
		try {
			VehicleVO vehicleVOReturn = registration.updateVehicle(vehicleVO);
			if (vehicleVOReturn != null) {
				return responseOk(vehicleVO);
			} else {
				return responseBadRequest("update unsuccessful");
			}
		} catch (Exception e) {
			Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("error", e.getMessage());
			return responseBadRequest(responseObj);
		}
	}

	/**
	 * <p>
	 * Validates the given Vehicle variable and throws validation exceptions based
	 * on the type of error. If the error is standard bean validation errors then it
	 * will throw a ConstraintValidationException with the set of the constraints
	 * violated.
	 * </p>
	 * <p>
	 * If the error is caused because an existing member with the same email is
	 * registered it throws a regular validation exception so that it can be
	 * interpreted separately.
	 * </p>
	 * 
	 * @param vehicle
	 *            Vehicle to be validated
	 * @throws ConstraintViolationException
	 *             If Bean Validation errors exist
	 * @throws ValidationException
	 *             If member with the same email already exists
	 */
	private void validateVehicle(Vehicle vehicle) throws ConstraintViolationException, ValidationException {
		// Create a bean validator and check for issues.
		Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
		}

		// Check the uniqueness of the email address
		if (vehicleAlreadyExists(vehicle.getName())) {
			throw new ValidationException("Unique Name Violation");
		}
	}

	/**
	 * Creates a JAX-RS "Bad Request" response including a map of all violation
	 * fields, and their message. This can then be used by clients to show
	 * violations.
	 * 
	 * @param violations
	 *            A set of violations that needs to be reported
	 * @return JAX-RS response containing all violations
	 */
	private Response.ResponseBuilder createViolationResponse(Set<ConstraintViolation<?>> violations) {
		log.fine("Validation completed. violations found: " + violations.size());

		Map<String, String> responseObj = new HashMap<String, String>();

		for (ConstraintViolation<?> violation : violations) {
			responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
		}

		return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
	}

	/**
	 * Checks if a member with the same email address is already registered. This is
	 * the only way to easily capture the "@UniqueConstraint(columnNames = "email")"
	 * constraint from the Member class.
	 * 
	 * @param email
	 *            The email to check
	 * @return True if the email already exists, and false otherwise
	 */
	public boolean vehicleAlreadyExists(String name) {
		Vehicle vehicle = null;
		try {
			vehicle = repository.findByName(name);
		} catch (NoResultException e) {
			// ignore
		}
		return vehicle != null;
	}
}
