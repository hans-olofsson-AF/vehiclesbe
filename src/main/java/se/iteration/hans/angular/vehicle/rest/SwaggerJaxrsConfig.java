package se.iteration.hans.angular.vehicle.rest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.BeanConfig;

/**
 * Servlet som initierar Swagger. Logiken hade även kunnat läggas i
 * JaxRsActivator men har lagts i en egen klass så att man vid bygge till
 * produktion med profilen "noswagger" (-Pnoswagger) lätt kan exkludera klassen
 * från WAR:en.
 * 
 * @author Hans Olofsson hans.olofsson@iteration.se
 *
 *         Skapad 8 jan 2018
 *
 */

@WebServlet(name = "SwaggerJaxrsConfig", loadOnStartup = 1)
public class SwaggerJaxrsConfig extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String BASE_PATH = "/vehiclesbe/rest";
	private static final String VERSION = "1.0.0";
	private static final String HTTP_SCHEME = "http";
	private static final String HTTPS_SCHEME = "https";
	private static final String HOST_AND_PORT_PROPERTY = "frontingHostAndPort";
	private static final String DEFAULT_HOST_AND_PORT = "localhost:8080";
	private static final String SWAGGER_RESOURCE_PACKAGE = "se.iteration.hans.angular.vehicle.rest";
	private static final String TITLE = "Swagger";

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		String hostAndPort = System.getProperty(HOST_AND_PORT_PROPERTY);
		if (hostAndPort == null) {
			hostAndPort = DEFAULT_HOST_AND_PORT;
		}
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setTitle(TITLE);
		beanConfig.setVersion(VERSION);
		beanConfig.setSchemes(new String[] { HTTP_SCHEME, HTTPS_SCHEME });
		beanConfig.setHost(hostAndPort);
		beanConfig.setBasePath(BASE_PATH);
		beanConfig.setResourcePackage(SWAGGER_RESOURCE_PACKAGE);
		beanConfig.setScan(true);
	}
}
