package zookeeper.rest;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

/**
 * Zookeeper REST Proxy service bootstrap
 */
public class Bootstrap
{
    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args)
    {
        Options options = new Options()
                .addOption(Option.builder().longOpt("zookeeper").hasArg().desc("Zookeeper servers").build())
                .addOption(Option.builder().longOpt("listen").hasArg().desc("Address to listen").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            logger.error("Failed to parse arguments", e);
            System.exit(1);
        }

        String[] listen = cmd.getOptionValue("listen", "127.0.0.1:8889").split(":");
        ipAddress(listen[0]);
        port(Integer.parseInt(listen[1]));

        exception(Exception.class, (exception, request, response) ->
        {
            logger.error("Internal error", exception);
            response.status(500);
            response.body("<html><body><h1>500 Internal Error</h1></body></html>");
        });

        String connectString = cmd.getOptionValue("zookeeper", "127.0.0.1:2181");
        Controller service = new Controller(new Service(connectString));

        configureRoutes(service);
    }

    private static void configureRoutes(Controller service)
    {
        get("/", service::greeting);
        get("/v1/tree/*", service::tree);
        get("/v1/list/*", service::list);
        get("/v1/get/*", service::get);
        put("/v1/set/*", service::set);
        delete("/v1/delete/*", service::delete);
    }
}
