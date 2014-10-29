package example;

import static valuesupply.StandardValueSupplyCategory.HTTP_HEADER;
import static valuesupply.StandardValueSupplyCategory.URL_COMPONENT;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import valuesupply.ValueSupply;

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
		prepareUrl();
		prepareHeaders();
		return request;
	}

	private void prepareUrl() {
		valueSupply.supplyAllOf(URL_COMPONENT, (urlComponents) -> {
			request = webTarget().resolveTemplatesFromEncoded(urlComponents) .request();
		});
	}

	private WebTarget webTarget() {
		return ClientBuilder.newClient().target(endpoint).path(resource);
	}

	private void prepareHeaders() {
		valueSupply.supplyEachOf(HTTP_HEADER, (httpHeader) -> {
			request.header(httpHeader.name(), httpHeader.valueAsString());
		});
	}
}