package valuesupply;

import static org.mockito.Mockito.verify;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ValueSupplyTest {

    private ValueSupplyCategory httpHeaderCategory;
    private ValueSupplyCategory urlComponentCategory;
    private ValueSupply valueSupply;
    private String arrivalName;
    private String departureName;
    private String approachingName;

    @Mock
    private ValueConsumer consumer;

    @Mock
    private Supplier<String> helloSupplier;

    @Mock
    private Supplier<String> onTheWaySupplier;

    @Mock
    private Supplier<String> goodbyeSupplier;

    @Before
    public void setUp() {
        valueSupply = new ValueSupply();
        arrivalName = "arrival";
        departureName = "departure";
        approachingName = "approaching";

        httpHeaderCategory = new ValueSupplyCategory("http-header");
        urlComponentCategory = new ValueSupplyCategory("url-component");
    }

    @Test
    public void providesCategorizedValueSuppliersToConsumers() {
        valueSupply.add(httpHeaderCategory, arrivalName, helloSupplier);
        valueSupply.add(httpHeaderCategory, approachingName, onTheWaySupplier);
        valueSupply.add(urlComponentCategory, departureName, goodbyeSupplier);

        valueSupply.provideAllTo(consumer);

        verify(consumer).accept(httpHeaderCategory, ImmutableMap.of(arrivalName, helloSupplier,
                                                                    approachingName, onTheWaySupplier));
        verify(consumer).accept(urlComponentCategory, ImmutableMap.of(departureName, goodbyeSupplier));
    }

}
