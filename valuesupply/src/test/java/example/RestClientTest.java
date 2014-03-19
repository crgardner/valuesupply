package example;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.MediaType;

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
    private String actualResponse;
    private String response;
    private String userNameHeaderValue;
    private String companyNameComponentValue;

    @Before
    public void setUp() {
        prepareRequestAspects();
        prepareValueSupply();
        prepareRestClient();
    }

    private void prepareRequestAspects() {
        endpoint = "http://localhost:8089";
        resource = "/v1/providers/{companyName}/exports";
        expandedResource = "/v1/providers/ACME/exports";
        response = "[{'export': 'cigars'}, {'export': 'scotch'}]";
        userNameHeaderValue = "clintEastwood";
        companyNameComponentValue = "ACME";
    }

    private void prepareValueSupply() {
        valueSupply = new ValueSupply();

        valueSupply.add(StandardValueSupplyCategory.MEDIA_TYPE, "Content-Type", new KnownValueSupplier(MediaType.APPLICATION_JSON));
        valueSupply.add(StandardValueSupplyCategory.HTTP_HEADER, "userName", new KnownValueSupplier(userNameHeaderValue));
        valueSupply.add(StandardValueSupplyCategory.URL_COMPONENT, "companyName", new KnownValueSupplier(companyNameComponentValue));
    }

    private void prepareRestClient() {
        restClient = new RestClient();
    }

    @Test
    public void preparesCallBasedOnValueSuppliers() throws Exception {
        stubFor(get(urlEqualTo(expandedResource))
                .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON))
                .withHeader("userName", equalTo(userNameHeaderValue))
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON)
                                       .withBody(response)));

        actualResponse = restClient.execute(endpoint, resource, valueSupply);

        assertThat(actualResponse).isEqualTo(response);
    }
}
