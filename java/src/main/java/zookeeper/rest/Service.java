package zookeeper.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;
import zookeeper.ZookeeperClient;

import java.util.*;

/**
 * Zookeeper REST Proxy service
 */
class Service
{
    private static Logger logger = LoggerFactory.getLogger(Service.class);

    private ZookeeperClient.Factory zkClientFactory;

    /**
     * Constructs Service instance
     *
     * @param zkClientFactory zookeeper client factory
     */
    Service(ZookeeperClient.Factory zkClientFactory)
    {
        this.zkClientFactory = zkClientFactory;
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
    Object tree(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Listing node tree");

        Map children;
        try (ZookeeperClient zkClient = zkClientFactory.create())
        {
            children = getChildrenRecursively(zkClient, path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to list node tree: {}", e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Success(path)
                              .children(children)
                              .toJson();
    }

    private Map getChildrenRecursively(ZookeeperClient zkClient, String path) throws Exception
    {
        Map<String, Object> map = new HashMap<>();
        String basePath = path + (path.equals("/") ? "" : "/");
        for (String node : zkClient.list(path))
        {
            Map children = getChildrenRecursively(zkClient, basePath + node);
            map.put(node, children);
        }
        return map;
    }

    /**
     * Gets zookeeper node children
     *
     * @param request  http request
     * @param response http responce
     * @return service response
     */
    Object list(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Listing children for {}", path);

        List<String> children;
        try (ZookeeperClient zkClient = zkClientFactory.create())
        {
            children = zkClient.list(path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to list children for {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Success(path)
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
    Object get(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Getting data for {}", path);

        byte[] data;
        try (ZookeeperClient zkClient = zkClientFactory.create())
        {
            data = zkClient.get(path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to delete {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Success(path)
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
    Object set(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Setting data for {}", path);

        byte[] data = request.bodyAsBytes();
        try (ZookeeperClient zkClient = zkClientFactory.create())
        {
            zkClient.set(path, data);
        }
        catch (Exception e)
        {
            logger.debug("Failed to set data for {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Success(path)
                              .toJson();
    }

    /**
     * Deletes zookeeper node child tree
     *
     * @param request  http request
     * @param response http responce
     * @return service response
     */
    Object delete(Request request, Response response)
    {
        response.type("application/json");
        String path = getPath(request);
        logger.debug("Deleting {}", path);

        try (ZookeeperClient zkClient = zkClientFactory.create())
        {
            zkClient.delete(path);
        }
        catch (Exception e)
        {
            logger.debug("Failed to delete {}: {}", path, e);
            return ServiceResponse.Error(path, e.getMessage())
                                  .toJson();
        }

        return ServiceResponse.Success(path)
                              .toJson();
    }

    private static String getPath(Request request)
    {
        return "/" + String.join("/", request.splat());
    }
}
