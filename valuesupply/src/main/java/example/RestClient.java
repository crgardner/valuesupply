package example;

import static example.StandardValueSupplyCategory.*;

import java.util.*;
import java.util.Map.Entry;

import javax.ws.rs.client.*;
import javax.ws.rs.client.Invocation.Builder;

import valuesupply.*;

public class RestClient {

    public String execute(String endpoint, String resource,
            ValueSupply valueSupply) {
        WebTarget target = ClientBuilder.newClient().target(endpoint)
                .path(resource);
        Map<ValueSupplyCategory, Map<String, Object>> expandedSuppliers = valueSupply
                .getAllCategorizedExpandedSuppliers();
        target = prepareUrl(target, expandedSuppliers);
        Builder request = target.request();
        request = prepareMediaTypes(request, expandedSuppliers);
        request = prepareHeaders(request, expandedSuppliers);
        return request.get(String.class);
    }

    private Builder prepareHeaders(Builder request,
            Map<ValueSupplyCategory, Map<String, Object>> expandedSuppliers) {
        if (expandedSuppliers.containsKey(HTTP_HEADER)) {
            for (Entry<String, Object> entry : expandedSuppliers.get(HTTP_HEADER).entrySet()) {
                request = request.header(entry.getKey(), entry.getValue());
            }
        }
        return request;
    }

    private Builder prepareMediaTypes(Builder request,
            Map<ValueSupplyCategory, Map<String, Object>> expandedSuppliers) {
        if (expandedSuppliers.containsKey(MEDIA_TYPE)) {
            for (Entry<String, Object> entry : expandedSuppliers.get(MEDIA_TYPE).entrySet()) {
                request = request.accept(entry.getValue().toString());
            }
        }
        return request;
    }

    private WebTarget prepareUrl(WebTarget target,
            Map<ValueSupplyCategory, Map<String, Object>> expandedSuppliers) {
        if (expandedSuppliers.containsKey(URL_COMPONENT)) {
            target = target.resolveTemplatesFromEncoded(expandedSuppliers
                    .get(URL_COMPONENT));
        }
        return target;
    }
}
