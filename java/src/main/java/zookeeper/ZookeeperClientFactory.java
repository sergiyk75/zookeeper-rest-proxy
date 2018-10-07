package zookeeper;

/**
 * Zookeeper client factory
 */
public class ZookeeperClientFactory
{
    private String connectString;

    /**
     * Constructs zookeeper client factory instance
     * @param connectString zookeeper connection string. Example 'zoo1:2181,zoo2:2181'
     */
    public ZookeeperClientFactory(String connectString)
    {
        this.connectString = connectString;
    }

    /**
     * Creates zookeeper client
     * @return zookeeper client
     */
    public ZookeeperClient create()
    {
        return new ZookeeperClient(connectString);
    }
}
