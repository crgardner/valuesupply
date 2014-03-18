package example;

import static example.StandardValueSupplyCategory.*;

import java.util.*;
import java.util.Map.Entry;

import javax.ws.rs.client.*;
import javax.ws.rs.client.Invocation.Builder;

import valuesupply.*;

public class RestClient {

    public String execute(String endpoint, String resource, ValueSupply valueSupply) {
        WebTarget target = ClientBuilder.newClient().target(endpoint).path(resource);

        ExpandedSupplierCapturingConsumer consumer = new ExpandedSupplierCapturingConsumer();
        valueSupply.provideAllTo(consumer);

        Map<ValueSupplyCategory, Map<String, Object>> expandedSuppliers = consumer.getExpandedSuppliers();
        
        if (expandedSuppliers.containsKey(URL_COMPONENT)) {
            target = target.resolveTemplatesFromEncoded(expandedSuppliers.get(URL_COMPONENT));
        }

        Builder request = target.request();
        if (expandedSuppliers.containsKey(MEDIA_TYPE)) {
            for (Entry<String, Object> entry : expandedSuppliers.get(MEDIA_TYPE).entrySet()) {
                request = request.accept(entry.getValue().toString());
            }
        }

        if (expandedSuppliers.containsKey(HTTP_HEADER)) {
            for (Entry<String, Object> entry : expandedSuppliers.get(HTTP_HEADER).entrySet()) {
                request = request.header(entry.getKey(), entry.getValue());
            }
        }

        return request.get(String.class);
    }
}
