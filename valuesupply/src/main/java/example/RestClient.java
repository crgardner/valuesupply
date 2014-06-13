package example;

import valuesupply.ValueSupply;

public class RestClient {

    public String execute(String endpoint, String resource, ValueSupply valueSupply) {
        return new RequestBuilder(endpoint, resource, valueSupply).build().get(String.class);
    }
}
