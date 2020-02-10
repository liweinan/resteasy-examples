package org.jboss.resteasy.examples.springmvc;

import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.resteasy.annotations.ClientURI;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.examples.springundertow.Contact;
import org.jboss.resteasy.examples.springundertow.ContactsResource;
import org.jboss.resteasy.plugins.server.undertow.spring.UndertowJaxrsSpringServer;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ContactsTest {

    @Path(ContactsResource.CONTACTS_URL)
    public interface ContactProxy {
        @Path("data")
        @POST
        @Consumes(MediaType.APPLICATION_XML)
        Response createContact(Contact contact);

        @GET
        @Produces(MediaType.APPLICATION_XML)
        Contact getContact(@ClientURI String uri);

        @GET
        String getString(@ClientURI String uri);
    }

    private static UndertowJaxrsSpringServer server;


    private static ContactProxy proxy;
    public static final String host = "http://localhost:8081/";
    private static Client client;

    @BeforeClass
    public static void setup() {

        // ----- SERVER ------
        server = new UndertowJaxrsSpringServer();
        server.start();

        DeploymentInfo deployment = server.undertowDeployment("classpath:resteasy-spring-mvc-servlet.xml", null);
        deployment.setDeploymentName(ContactsTest.class.getName());
        deployment.setContextPath("/");
        deployment.setClassLoader(ContactsTest.class.getClassLoader());
        server.deploy(deployment);

        // ----- CLIENT ------
        client = ClientBuilder.newClient();
        WebTarget target = client.target(host);
        ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
        proxy = rtarget.proxy(ContactProxy.class);
    }

    @AfterClass
    public static void end() {
        server.stop();
        client.close();
    }

    @Test
    public void readHTML() {
        {
            // post data
            Response response = proxy.createContact(new Contact("Solomon", "Duskis"));
            Assert.assertEquals(response.getStatus(), 201);
            String duskisUri = (String) response.getMetadata().getFirst(
                    HttpHeaderNames.LOCATION);
            System.out.println(duskisUri);
            Assert.assertTrue(duskisUri.endsWith(ContactsResource.CONTACTS_URL
                    + "/data/Duskis"));
            response.close();
            Assert
                    .assertEquals("Solomon", proxy.getContact(duskisUri).getFirstName());
            response = proxy.createContact(new Contact("Bill", "Burkie"));
            response.close();
            String result = proxy.getString(host + ContactsResource.CONTACTS_URL
                    + "/data");
            Assert.assertEquals(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><contacts><contact><firstName>Solomon</firstName><lastName>Duskis</lastName></contact><contact><firstName>Bill</firstName><lastName>Burkie</lastName></contact></contacts>",
                    result);
        }

        {
            // read data
            String result = proxy.getString(host + ContactsResource.CONTACTS_URL
                    + "/data");
            Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><contacts><contact><firstName>Solomon</firstName><lastName>Duskis</lastName></contact><contact><firstName>Bill</firstName><lastName>Burkie</lastName></contact></contacts>",
                    result);
        }
    }
}
