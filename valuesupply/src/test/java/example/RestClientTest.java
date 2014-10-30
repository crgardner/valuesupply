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
        
        when(supplierFactory.create(contentTypeDescriptor)).thenReturn(new BasicSupplier<Object>(MediaType.APPLICATION_JSON));
        when(supplierFactory.create(userNameDescriptor)).thenReturn(new BasicSupplier<Object>(userNameHeaderValue));
        when(supplierFactory.create(companyNameDescriptor)).thenReturn(new BasicSupplier<Object>(companyNameComponentValue));

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
