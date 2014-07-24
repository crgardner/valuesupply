package example;

import static valuesupply.StandardValueSupplyCategory.HTTP_HEADER;
import static valuesupply.StandardValueSupplyCategory.URL_COMPONENT;

import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import util.function.Consumer;
import valuesupply.ValueSupply;
import valuesupply.ValueSupplyItem;

class RequestBuilder {
    private final String endpoint;
    private final String resource;
    private final ValueSupply valueSupply;
    private Builder request;

    RequestBuilder(String endpoint, String resource, ValueSupply valueSupply) {
        this.endpoint = endpoint;
        this.resource = resource;
        this.valueSupply = valueSupply;
    }

    Builder build() {
        prepareUrlFrom();
        prepareHeaders();
        return request;
    }

    private void prepareUrlFrom() {
        valueSupply.supplyAllOf(URL_COMPONENT, new Consumer<Map<String, Object>>() {

            @Override
            public void accept(Map<String, Object> urlComponents) {
                request = webTarget().resolveTemplatesFromEncoded(urlComponents).request();
            }
        });
    }

    private WebTarget webTarget() {
        return ClientBuilder.newClient().target(endpoint).path(resource);
    }

    private void prepareHeaders() {
        valueSupply.supplyEachOf(HTTP_HEADER, new Consumer<ValueSupplyItem>() {

            @Override
            public void accept(ValueSupplyItem httpHeader) {
                request.header(httpHeader.name(), httpHeader.valueAsString());
            }
        });
    }
}