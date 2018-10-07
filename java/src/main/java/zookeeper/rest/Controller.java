package zookeeper.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

import java.util.*;

/**
 * Zookeeper REST Proxy service controller
 */
class Controller
{
    private static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final Service service;

    /**
     * Constructs Service instance
     *
     * @param service zookeeper service api
     */
    Controller(Service service)
    {
        this.service = service;
    }

    /**
     * Returns greeting html document
     *
     * @param request  http request
     * @param response http responce
     * @return greeting html document
     */
    @SuppressWarnings("unused")
    String greeting(Request request, Response response)
    {
        response.type("text/html");
        try
        {
            ClassLoader classLoader = getClass().getClassLoader();

            Properties properties = new Properties();
            properties.load(classLoader.getResourceAsStream("project.properties"));

            String html = IOUtils.toString(Objects.requireNonNull(classLoader.getResourceAsStream("greeting.html")));
            return String.format(html, properties.getProperty("version"));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to produce greeting document", e);
        }
    }

    /**
     * Gets zookeeper node child tree
     *
     * @param request  http request
     * @param response http responce
     * @return service response
     */
    String tree(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Listing node tree");

        Map children;
        try
        {
            children = service.getNodeChildTree(path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to list node tree: {}", e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Ok(path)
                              .children(children)
                              .toJson();
    }

    /**
     * Gets zookeeper node children
     *
     * @param request  http request
     * @param response http responce
     * @return service response
     */
    String list(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Listing children for {}", path);

        List<String> children;
        try
        {
            children = service.getNodeChildren(path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to list children for {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Ok(path)
                              .children(children)
                              .toJson();
    }

    /**
     * Returns zookeeper node data
     *
     * @param request  http request
     * @param response http responce
     * @return service response
     */
    String get(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Getting data for {}", path);

        byte[] data;
        try
        {
            data = service.getNodeData(path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to delete {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Ok(path)
                              .data(data)
                              .toJson();
    }

    /**
     * Sets zookeeper node child tree
     *
     * @param request  http request
     * @param response http responce
     * @return service response
     */
    String set(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Setting data for {}", path);

        byte[] data = request.bodyAsBytes();
        try
        {
            service.setNodeData(path, data);
        }
        catch (Exception e)
        {
            logger.debug("Failed to set data for {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Ok(path)
                              .toJson();
    }

    /**
     * Deletes zookeeper node child tree
     *
     * @param request  http request
     * @param response http responce
     * @return service response
     */
    String delete(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Deleting {}", path);

        try
        {
            service.deleteNode(path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to delete {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Ok(path)
                              .toJson();
    }

    private static String getPath(Request request)
    {
        return "/" + String.join("/", request.splat());
    }
}
