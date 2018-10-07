package zookeeper.rest;

import com.google.gson.Gson;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Zookeeper Service response
 */
class ServiceResponse
{
    /**
     * Creates OK ServiceResponse instance
     * @param path zookeeper node path
     * @return OK ServiceResponse instance
     */
    static ServiceResponse Ok(String path)
    {
        return new ServiceResponse("OK", path);
    }

    /**
     * Creates ERROR ServiceResponse instance
     * @param path zookeeper node path
     * @param error error message
     * @return ERROR ServiceResponse instance
     */
    static ServiceResponse Error(String path, String error)
    {
        ServiceResponse response = new ServiceResponse("ERROR", path);
        response.map.put("Error", error);
        return response;
    }

    private Map<String, Object> map = new HashMap<>();

    private ServiceResponse(String status, String path)
    {
        map.put("Status", status);
        map.put("Path", path);
    }

    /**
     * Sets zookeeper node data
     * @param data zokeeper node data
     * @return ServiceResponse instance
     */
    ServiceResponse data(byte[] data)
    {
        String encodedData = Base64.getEncoder().encodeToString(data);
        map.put("Data", encodedData);
        return this;
    }

    /**
     * Sets zookeeper node children
     * @param children zokeeper node child list or child tree
     * @return ServiceResponse instance
     */
    ServiceResponse children(Object children)
    {
        map.put("Children", children);
        return this;
    }

    /**
     * Serializes service response into json string
     * @return json string
     */
    String toJson()
    {
        return new Gson().toJson(map);
    }
}
