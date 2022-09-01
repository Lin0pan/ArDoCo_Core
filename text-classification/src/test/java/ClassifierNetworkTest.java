import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import records.ClassificationResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassifierNetworkTest {

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetworkAsync.class);

    private AsyncRestAPI mockedRestApi;
    private TextClassifier classifier;

    @BeforeEach
    private void init(){
        this.mockedRestApi = Mockito.mock(AsyncRestAPI.class);
        this.classifier = new ClassifierNetworkAsync(mockedRestApi);
    }

    private Future<JSONObject> futureFromJSONObject(JSONObject obj){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> obj);
    }

    private void mockApiStatusResponse(Boolean classifierReady){
        JSONObject jsonStatusResponse = null;
        try {
            if(classifierReady) {
                jsonStatusResponse = (JSONObject) new JSONParser().parse("{\"status\":\"ready\"}");
            } else {
                jsonStatusResponse = (JSONObject) new JSONParser().parse("{\"status\":\"notready\"}");
            }

            //Future<JSONObject> futureJsonStatusResponse = new Future<JSONObject>(jsonStatusResponse);
            when(mockedRestApi.sendApiRequest("/status")).thenReturn(futureFromJSONObject(jsonStatusResponse));

        } catch (ParseException e) {
            logger.error("Failed to parse json: " + e.getMessage(), e);
        }
    }

    private void mockApiClassificationResponse(JSONObject classificationResponse){
        when(mockedRestApi.sendApiRequest("/classify", classificationResponse)).thenReturn(futureFromJSONObject(classificationResponse));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getClassifierStatus_ifStatusContainsReady_returnReady(boolean ready){

        mockApiStatusResponse(ready);
        Assertions.assertEquals(classifier.getClassifierStatus().ready(), ready);
    }

    @Test
    void classifyPhrases_jsonResponse_parseResponse(){

        Map<Integer, String> testRequest = new HashMap<>() {{
            put(1, "test-phrase-1");
            put(5, "test-phrase-5");
            put(7, "test-phrase-7");
        }};

        mockApiClassificationResponse(new JSONObject(testRequest));

        ClassificationResponse response = classifier.classifyPhrases(new JSONObject(testRequest));

        Assertions.assertEquals(response.classifications().size(), testRequest.size());
    }
}
