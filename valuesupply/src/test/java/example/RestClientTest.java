package example;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import valuesupply.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Optional;
import com.google.common.base.Suppliers;

@RunWith(MockitoJUnitRunner.class)
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

    @Mock
    private SupplierFactory supplierFactory;

    private ValueSupplyItemDescriptor contentTypeDescriptor;

    private ValueSupplyItemDescriptor userNameDescriptor;

    private ValueSupplyItemDescriptor companyNameDescriptor;

    @Before
    public void setUp() throws Exception {
        prepareRequestAspects();
        prepareValueSupplyItemDescriptors();
        prepareValueSupply();
        prepareRestClient();
    }


    private void prepareRequestAspects() {
        endpoint = "http://localhost:8089";
        resource = "/v1/providers/{companyName}/exports";
        userNameHeaderValue = "clintEastwood";
        companyNameComponentValue = "ACME";
    }

    private void prepareValueSupplyItemDescriptors() {
        contentTypeDescriptor = new ValueSupplyItemDescriptor(StandardValueSupplyCategory.HTTP_HEADER, "Accept", StandardValueType.String, MediaType.APPLICATION_JSON);
        userNameDescriptor = new ValueSupplyItemDescriptor(StandardValueSupplyCategory.HTTP_HEADER, "userName", StandardValueType.String, userNameHeaderValue);
        companyNameDescriptor = new ValueSupplyItemDescriptor(StandardValueSupplyCategory.URL_COMPONENT, "companyName", StandardValueType.String, companyNameComponentValue);

    }
    private void prepareValueSupply() throws Exception {
        valueSupply = new ValueSupply(supplierFactory);

        when(supplierFactory.createFrom(contentTypeDescriptor)).thenReturn(Optional.of(Suppliers.<Object>ofInstance((MediaType.APPLICATION_JSON))));
        when(supplierFactory.createFrom(userNameDescriptor)).thenReturn(Optional.of(Suppliers.<Object>ofInstance(userNameHeaderValue)));
        when(supplierFactory.createFrom(companyNameDescriptor)).thenReturn(Optional.of(Suppliers.<Object>ofInstance(companyNameComponentValue)));

        valueSupply.addItemBasedOn(companyNameDescriptor);
        valueSupply.addItemBasedOn(contentTypeDescriptor);
        valueSupply.addItemBasedOn(userNameDescriptor);
    }

    private void prepareRestClient() {
        restClient = new RestClient();
    }

    @Test
    public void preparesCallBasedOnValueSuppliers() throws Exception {

        expandedResource = "/v1/providers/ACME/exports";
        response = "[{'export': 'cigars'}, {'export': 'scotch'}]";

        stubFor(get(urlEqualTo(expandedResource))
                .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON))
                .withHeader("userName", equalTo(userNameHeaderValue))
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON)
                                       .withBody(response)));

        actualResponse = restClient.execute(endpoint, resource, valueSupply);

        assertThat(actualResponse).isEqualTo(response);
    }
}
