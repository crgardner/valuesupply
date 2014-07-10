package example;

import static valuesupply.StandardValueSupplyCategory.HTTP_HEADER;
import static valuesupply.StandardValueSupplyCategory.MEDIA_TYPE;
import static valuesupply.StandardValueSupplyCategory.URL_COMPONENT;

import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import util.function.Consumer;
import valuesupply.ValueSupply;
import valuesupply.ValueSupplyItem;

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
        valueSupply.supplyEachOf(HTTP_HEADER, new Consumer<ValueSupplyItem>() {

            @Override
            public void accept(ValueSupplyItem httpHeader) {
                request.header(httpHeader.getName(), httpHeader.getValue());
            }
        });
    }

    private void prepareMediaTypes() {
        valueSupply.supplyEachOf(MEDIA_TYPE, new Consumer<ValueSupplyItem>() {

            @Override
            public void accept(ValueSupplyItem mediaType) {
                request.accept(mediaType.getValue().toString());
            }
        });
    }

}