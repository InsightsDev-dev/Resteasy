package org.jboss.resteasy.test.spring.deployment;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.test.spring.deployment.resource.SpringLocatingLocatingResource;
import org.jboss.resteasy.test.spring.deployment.resource.SpringLocatingSimpleResource;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtilSpring;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * @tpSubChapter Spring
 * @tpChapter Integration tests - dependencies included in deployment
 * @tpSince RESTEasy 3.0.16
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SpringLocatingResourceDependenciesInDeploymentTest {

    static ResteasyClient client;

    @Deployment
    private static Archive<?> deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, SpringLocatingResourceDependenciesInDeploymentTest.class.getSimpleName() + ".war")
                .addAsWebInfResource(SpringLocatingResourceDependenciesInDeploymentTest.class.getPackage(), "web.xml", "web.xml");
        archive.addAsWebInfResource(SpringLocatingResourceDependenciesInDeploymentTest.class.getPackage(),
                "springLocatingResource/applicationContext.xml", "applicationContext.xml");
        archive.addClass(SpringLocatingLocatingResource.class);
        archive.addClass(SpringLocatingSimpleResource.class);
        TestUtilSpring.addSpringLibraries(archive);
        return archive;
    }

    @Before
    public void init() {
        client = new ResteasyClientBuilder().build();
    }

    @After
    public void after() throws Exception {
        client.close();
    }

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, SpringLocatingResourceDependenciesInDeploymentTest.class.getSimpleName());
    }

    /**
     * @tpTestDetails Test resource bean defined in xml spring settings
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testNoDefaultsResource() throws Exception {

        {
            WebTarget target = client.target(generateURL("/basic"));
            Response response = target.request().get();
            Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assert.assertEquals("Unexpected response from the server", "basic", response.readEntity(String.class));
        }
        {
            WebTarget target = client.target(generateURL("/basic"));
            Response response = target.request().put(Entity.text("basic"));
            Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());

        }
        {
            WebTarget target = client.target(generateURL("/queryParam"));
            Response response = target.queryParam("param", "hello world").request().get();
            Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assert.assertEquals("Unexpected response from the server", "hello world", response.readEntity(String.class));
        }
        {
            WebTarget target = client.target(generateURL("/uriParam/1234"));
            Response response = target.request().get();
            Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assert.assertEquals("1234", response.readEntity(String.class));
        }

    }

    /**
     * @tpTestDetails Test resource bean defined in xml spring settings, resource calls another resource also
     * defined as resource bean
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testLocatingResource() throws Exception {

        {
            WebTarget target = client.target(generateURL("/locating/basic"));
            Response response = target.request().get();
            Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assert.assertEquals("Unexpected response from the server", "basic", response.readEntity(String.class));
        }
        {
            WebTarget target = client.target(generateURL("/locating/basic"));
            Response response = target.request().put(Entity.text("basic"));
            Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());
        }
        {
            WebTarget target = client.target(generateURL("/locating/queryParam"));
            Response response = target.queryParam("param", "hello world").request().get();
            Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assert.assertEquals("Unexpected response from the server", "hello world", response.readEntity(String.class));
        }
        {
            WebTarget target = client.target(generateURL("/locating/uriParam/1234"));
            Response response = target.request().get();
            Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            Assert.assertEquals("1234", response.readEntity(String.class));
        }
    }
}
