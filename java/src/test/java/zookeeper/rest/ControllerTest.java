package zookeeper.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ControllerTest
{
    private Controller controller;
    private Service service;
    private Request request;
    private Response response;

    @BeforeEach
    void setUp()
    {
        request = Mockito.mock(Request.class);
        response = Mockito.mock(Response.class);
        service = Mockito.mock(Service.class);
        controller = new Controller(service);
    }

    @Test
    void greeting()
    {
        String result = controller.greeting(request, response);

        assertNotNull(result);
    }

    @Test
    void tree() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.when(service.getNodeChildTree("/one/two"))
               .thenReturn(ImmutableMap.of("three", ImmutableMap.of("four", Collections.emptyMap(), "five", Collections.emptyMap())));

        String result = controller.tree(request, response);

        assertEquals("{\"Status\":\"OK\",\"Path\":\"/one/two\",\"Children\":{\"three\":{\"four\":{},\"five\":{}}}}", result);
    }

    @Test
    void treeFailed() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.when(service.getNodeChildTree("/one/two"))
               .thenThrow(new Exception("blah"));

        String result = controller.tree(request, response);

        assertEquals("{\"Status\":\"ERROR\",\"Path\":\"/one/two\",\"Error\":\"blah\"}", result);
    }

    @Test
    void list() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two", "three"});
        Mockito.when(service.getNodeChildren("/one/two/three"))
               .thenReturn(ImmutableList.of("five", "four", "six"));

        String result = controller.list(request, response);

        assertEquals("{\"Status\":\"OK\",\"Path\":\"/one/two/three\",\"Children\":[\"five\",\"four\",\"six\"]}", result);
    }

    @Test
    void listFailed() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.when(service.getNodeChildren("/one/two"))
               .thenThrow(new Exception("blah"));

        String result = controller.list(request, response);

        assertEquals("{\"Status\":\"ERROR\",\"Path\":\"/one/two\",\"Error\":\"blah\"}", result);
    }

    @Test
    void get() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.when(service.getNodeData("/one/two"))
               .thenReturn("blah".getBytes());

        String result = controller.get(request, response);

        assertEquals("{\"Status\":\"OK\",\"Path\":\"/one/two\",\"Data\":\"YmxhaA\\u003d\\u003d\"}", result);
    }

    @Test
    void getFailed() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.when(service.getNodeData("/one/two"))
               .thenThrow(new Exception("blah"));

        String result = controller.get(request, response);

        assertEquals("{\"Status\":\"ERROR\",\"Path\":\"/one/two\",\"Error\":\"blah\"}", result);
    }

    @org.junit.jupiter.api.Test
    void set() throws Exception
    {
        byte[] data = "blah".getBytes();
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.when(request.bodyAsBytes()).thenReturn(data);

        String result = controller.set(request, response);

        assertEquals("{\"Status\":\"OK\",\"Path\":\"/one/two\"}", result);
        Mockito.verify(service).setNodeData("/one/two", data);
    }

    @Test
    void setFailed() throws Exception
    {
        byte[] data = "blah".getBytes();
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.when(request.bodyAsBytes()).thenReturn(data);
        Mockito.doThrow(new Exception("blah")).when(service).setNodeData("/one/two", data);

        String result = controller.set(request, response);

        assertEquals("{\"Status\":\"ERROR\",\"Path\":\"/one/two\",\"Error\":\"blah\"}", result);
    }

    @Test
    void delete() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});

        String result = controller.delete(request, response);

        assertEquals("{\"Status\":\"OK\",\"Path\":\"/one/two\"}", result);
        Mockito.verify(service).deleteNode("/one/two");
    }

    @Test
    void deleteFailed() throws Exception
    {
        Mockito.when(request.splat()).thenReturn(new String[]{"one", "two"});
        Mockito.doThrow(new Exception("blah")).when(service).deleteNode("/one/two");

        String result = controller.delete(request, response);

        assertEquals("{\"Status\":\"ERROR\",\"Path\":\"/one/two\",\"Error\":\"blah\"}", result);
    }
}