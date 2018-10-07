package zookeeper.rest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zookeeper service. It uses curator API
 */
class Service
{
    private static final int CONNECTION_TIMEOUT_MS = 3000;
    private static final int SESSION_TIMEOUT_MS = 10000;
    private static final int RETRY_TIMES = 1;
    private static final int RETRY_INTERVAL_MS = 1000;

    private final String connectString;

    /**
     * Constructs zookeeper service instance
     *
     * @param connectString zookeeper connection string. Example 'zoo1:2181,zoo2:2181'
     */
    Service(String connectString)
    {
        this.connectString = connectString;
    }

    private CuratorFramework createClient()
    {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                connectString,
                SESSION_TIMEOUT_MS,
                CONNECTION_TIMEOUT_MS,
                new RetryNTimes(RETRY_TIMES, RETRY_INTERVAL_MS));
        client.start();
        return client;
    }

    /**
     * Gets data for specified zookeeper node
     *
     * @param path zookeeper node path
     * @return zookeeper node data
     * @throws Exception errors
     */
    byte[] getNodeData(String path) throws Exception
    {
        try (CuratorFramework client = createClient())
        {
            return client.getData().forPath(path);
        }
    }

    /**
     * Sets data for specified zookeeper node
     * Parent nodes are auto created as needed
     *
     * @param path  zookeeper node path
     * @param bytes zookeeper node data
     * @throws Exception errors
     */
    void setNodeData(String path, byte[] bytes) throws Exception
    {
        try (CuratorFramework client = createClient())
        {
            client.create()
                  .orSetData()
                  .creatingParentsIfNeeded()
                  .forPath(path, bytes);
        }
    }

    /**
     * Deletes specified nodes and its children tree
     *
     * @param path zookeeper node path
     * @throws Exception errors
     */
    void deleteNode(String path) throws Exception
    {
        try (CuratorFramework client = createClient())
        {
            client.delete()
                  .deletingChildrenIfNeeded()
                  .forPath(path);
        }
    }

    /**
     * Lists direct child nodes for specified zookeeper node
     *
     * @param path zookeeper node path
     * @return child nodes
     * @throws Exception errors
     */
    List<String> getNodeChildren(String path) throws Exception
    {
        try (CuratorFramework client = createClient())
        {
            return client.getChildren().forPath(path);
        }
    }

    /**
     * Lists child nodes recursively for specified zookeeper node
     *
     * @param path zookeeper node path
     * @return child nodes
     * @throws Exception errors
     */
    Map getNodeChildTree(String path) throws Exception
    {
        try (CuratorFramework client = createClient())
        {
            return getChildrenRecursively(client, path);
        }
    }

    private Map getChildrenRecursively(CuratorFramework client, String path) throws Exception
    {
        Map<String, Object> map = new HashMap<>();
        String basePath = path + (path.equals("/") ? "" : "/");
        for (String node : client.getChildren().forPath(path))
        {
            Map children = getChildrenRecursively(client, basePath + node);
            map.put(node, children);
        }
        return map;
    }

}
