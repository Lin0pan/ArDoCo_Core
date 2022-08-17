import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncRestAPI implements WebAPI<Future<JSONObject>, JSONObject>{
    private static final Logger logger = LoggerFactory.getLogger(AsyncRestAPI.class);
    private final String url;
    private final int port;

    private final ExecutorService executor;

    public AsyncRestAPI(String url, int port) {
        this.url = url;
        this.port = port;
        this.executor= Executors.newSingleThreadExecutor();
    }

    private JSONObject parseJsonString(String jsonString){
        Object ob = null;
        try {
            ob = new JSONParser().parse(jsonString);
        } catch (ParseException e) {
            logger.error("Failed to parse json string" + e.getMessage(), e);
        }
        return  (JSONObject) ob;
    }

    @Override
    public Future<JSONObject> sendApiRequest(String endpoint, JSONObject requestData) {
        return executor.submit(() -> {
            HttpURLConnection connection;
            BufferedReader reader;
            String line;
            StringBuilder responseContent = new StringBuilder();

            String ep = endpoint;
            if(!endpoint.startsWith("/")){
                ep = "/"+endpoint;
            }

            try{
                URL u = new URL(this.url + ":" + port + ep);
                connection = (HttpURLConnection) u.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = requestData.toJSONString();

                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int status = connection.getResponseCode();

                if (status >= 300) {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }
                else {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }

                connection.disconnect();
                return parseJsonString(responseContent.toString());

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return new JSONObject();
        });
    }

    @Override
    public Future<JSONObject> sendApiRequest(String endpoint) {
        return executor.submit(() -> {
            HttpURLConnection connection;
            BufferedReader reader;
            String line;
            StringBuilder responseContent = new StringBuilder();

            String ep = endpoint;
            if (!endpoint.startsWith("/")) {
                ep = "/" + endpoint;
            }

            try {
                URL u = new URL(this.url + ":" + port + ep);
                connection = (HttpURLConnection) u.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();

                if (status >= 300) {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }

                connection.disconnect();
                return parseJsonString(responseContent.toString());

            } catch (IOException e) {
                logger.error("Failed to request status: " + e.getMessage(), e);
            }
            return new JSONObject();

        });
    }
}
