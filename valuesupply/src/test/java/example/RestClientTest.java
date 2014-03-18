package example;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.*;

import valuesupply.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class RestClientTest {
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089));
    
    private ValueSupply valueSupply;
    private String endpoint;
    private String expandedResource;
    private String resource;
    private RestClient restClient;
    private String response;

    @Test
    public void preparesCallBasedOnValueSuppliers() throws Exception {
        endpoint = "http://localhost:8089";
        resource = "/v1/providers/{companyName}/exports";
        expandedResource = "/v1/providers/ACME/exports";
        
        stubFor(get(urlEqualTo(expandedResource))
                .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("[{'export': 'cigars'}, {'export': 'scotch'}]")));
        
        valueSupply = new ValueSupply();
        
        valueSupply.add(StandardValueSupplyCategory.URL_COMPONENT, "companyName", new KnownValueSupplier("ACME"));
        valueSupply.add(StandardValueSupplyCategory.MEDIA_TYPE, "Content-Type", new KnownValueSupplier("application/json"));
        valueSupply.add(StandardValueSupplyCategory.HTTP_HEADER, "userName", new KnownValueSupplier("clintEastwood"));
        
        restClient = new RestClient();
        
        response = restClient.execute(endpoint, resource, valueSupply);
        
        assertThat(response).isEqualTo("[{'export': 'cigars'}, {'export': 'scotch'}]");
    }
}
