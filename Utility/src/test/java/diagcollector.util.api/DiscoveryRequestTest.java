package diagcollector.util.api;

import com.google.gson.Gson;
import diagcollector.diagutil.api.DiscoveryRequest;
import org.junit.Assert;
import org.junit.Test;

public class DiscoveryRequestTest {
    @Test
    public void testDiscoveryAll() {
        String checkString = "all";
        String json = "{\n" +
                "\"text\":\"" + checkString + "\"\n" +
                "}";
        Gson gson = new Gson();
        DiscoveryRequest discoveryRequest = gson.fromJson(json, DiscoveryRequest.class);
        Assert.assertNotNull(discoveryRequest);
        Assert.assertEquals(checkString, discoveryRequest.getText());
    }
}
