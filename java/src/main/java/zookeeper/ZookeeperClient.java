package zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.List;

/**
 * Zookeeper client wrapper over curator API
 */
public class ZookeeperClient implements AutoCloseable
{
    /**
     * Zookeeper client factory
     */
    public static class Factory
    {
        private String connectString;

        /**
         * Constructs zookeeper client factory instance
         *
         * @param connectString zookeeper connection string. Example 'zoo1:2181,zoo2:2181'
         */
        public Factory(String connectString)
        {
            this.connectString = connectString;
        }

        /**
         * Creates zookeeper client
         *
         * @return zookeeper client
         */
        public ZookeeperClient create()
        {
            return new ZookeeperClient(connectString);
        }
    }

    private static final int CONNECTION_TIMEOUT_MS = 3000;
    private static final int SESSION_TIMEOUT_MS = 10000;
    private static final int RETRY_TIMES = 1;
    private static final int RETRY_INTERVAL_MS = 1000;

    private final CuratorFramework curator;

    /**
     * Constructs zookeeper client instance
     *
     * @param connectString zookeeper connection string. Example 'zoo1:2181,zoo2:2181'
     */
    private ZookeeperClient(String connectString)
    {
        curator = CuratorFrameworkFactory.newClient(
                connectString,
                SESSION_TIMEOUT_MS,
                CONNECTION_TIMEOUT_MS,
                new RetryNTimes(RETRY_TIMES, RETRY_INTERVAL_MS));
        curator.start();
    }

    /**
     * Gets data for specified zookeeper node
     *
     * @param path zookeeper node path
     * @return zookeeper node data
     * @throws Exception throws KeeperErrorCode exception
     */
    public byte[] get(String path) throws Exception
    {
        return curator.getData()
                      .forPath(path);
    }

    /**
     * Sets data for specified zookeeper node
     * Parent nodes are auto created as needed
     *
     * @param path  zookeeper node path
     * @param bytes zookeeper node data
     * @throws Exception throws KeeperErrorCode exception
     */
    public void set(String path, byte[] bytes) throws Exception
    {
        curator.create()
               .orSetData()
               .creatingParentsIfNeeded()
               .forPath(path, bytes);
    }

    /**
     * Deletes specifed nodes and its children tree
     *
     * @param path zookeeper node path
     * @throws Exception throws KeeperErrorCode exception
     */
    public void delete(String path) throws Exception
    {
        curator.delete()
               .deletingChildrenIfNeeded()
               .forPath(path);
    }

    /**
     * Lists child nodes for specifed zookeeper node
     *
     * @param path zookeeper node path
     * @return child nodes
     * @throws Exception throws KeeperErrorCode exception
     */
    public List<String> list(String path) throws Exception
    {
        return curator.getChildren()
                      .forPath(path);
    }

    /**
     * Closes active zookeeper connection
     */
    public void close()
    {
        curator.close();
    }
}
