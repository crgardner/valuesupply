package example;

import static valuesupply.StandardValueSupplyCategory.*;

import javax.ws.rs.client.*;
import javax.ws.rs.client.Invocation.Builder;

import valuesupply.ValueSupply;

class RequestBuilder {
    private final ValueSupply valueSupply;
    private Builder request;

    RequestBuilder(String endpoint, String resource, ValueSupply valueSupply) {
        this.valueSupply = valueSupply;
        prepareUrl(ClientBuilder.newClient().target(endpoint).path(resource));
    }

    private void prepareUrl(WebTarget webTarget) {
        valueSupply.supplyAllOf(URL_COMPONENT, (urlComponents) -> {
            request = webTarget.resolveTemplatesFromEncoded(urlComponents).request();
        });
    }

    Builder build() {
        prepareHeaders();
        prepareMediaTypes();
        return request;
    }

    private void prepareHeaders() {
        valueSupply.supplyEachOf(HTTP_HEADER, (httpHeader) -> {
            request.header(httpHeader.getKey(), httpHeader.getValue());
        });
    }

    private void prepareMediaTypes() {
        valueSupply.supplyEachOf(MEDIA_TYPE, (mediaType) -> {
            request.accept(mediaType.getValue().toString());
        });
    }

}