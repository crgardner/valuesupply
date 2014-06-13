package example;

import static valuesupply.StandardValueSupplyCategory.HTTP_HEADER;
import static valuesupply.StandardValueSupplyCategory.MEDIA_TYPE;
import static valuesupply.StandardValueSupplyCategory.URL_COMPONENT;

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;

import util.function.Consumer;
import valuesupply.ValueSupply;

class RequestBuilder {
    private final ValueSupply valueSupply;
    private Builder request;

    RequestBuilder(String endpoint, String resource, ValueSupply valueSupply) {
        this.valueSupply = valueSupply;
        prepareUrl(ClientBuilder.newClient().target(endpoint).path(resource));
    }

    private void prepareUrl(final WebTarget webTarget) {
        valueSupply.supplyAllOf(URL_COMPONENT, new Consumer<Map<String, Object>>() {

            @Override
            public void accept(Map<String, Object> urlComponents) {
                request = webTarget.resolveTemplatesFromEncoded(urlComponents).request();
            }
        });
    }

    Builder build() {
        prepareHeaders();
        prepareMediaTypes();
        return request;
    }

    private void prepareHeaders() {
        valueSupply.supplyEachOf(HTTP_HEADER, new Consumer<Map.Entry<String, Object>>() {

            @Override
            public void accept(Entry<String, Object> httpHeader) {
                request.header(httpHeader.getKey(), httpHeader.getValue());
            }
        });
    }

    private void prepareMediaTypes() {
        valueSupply.supplyEachOf(MEDIA_TYPE, new Consumer<Map.Entry<String, Object>>() {

            @Override
            public void accept(Entry<String, Object> mediaType) {
                request.accept(mediaType.getValue().toString());
            }
        });
    }

}