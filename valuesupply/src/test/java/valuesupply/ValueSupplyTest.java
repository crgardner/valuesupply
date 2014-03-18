package valuesupply;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ValueSupplyTest {

    private BasicValueSupplyCategory httpHeaderCategory;
    private BasicValueSupplyCategory urlComponentCategory;
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
	private Map<ValueSupplyCategory, Map<String, Object>> allSuppliers;

    @Before
    public void setUp() {
        valueSupply = new ValueSupply();
        arrivalName = "arrival";
        departureName = "departure";
        approachingName = "approaching";

        httpHeaderCategory = new BasicValueSupplyCategory("http-header");
        urlComponentCategory = new BasicValueSupplyCategory("url-component");
    }

    @Test
    public void providesCategorizedValueSuppliersToConsumers() {
        valueSupply.add(httpHeaderCategory, arrivalName, helloSupplier);
        valueSupply.add(httpHeaderCategory, approachingName, onTheWaySupplier);
        valueSupply.add(urlComponentCategory, departureName, goodbyeSupplier);

        valueSupply.provideEachByCategoryTo(consumer);

        verify(consumer).accept(httpHeaderCategory, ImmutableMap.of(arrivalName, helloSupplier,
                                                                    approachingName, onTheWaySupplier));
        verify(consumer).accept(urlComponentCategory, ImmutableMap.of(departureName, goodbyeSupplier));
    }

    @Test
    public void returnsAllCategorizedExpandedSuppliers() {
    	when(helloSupplier.get()).thenReturn("hello");
    	when(onTheWaySupplier.get()).thenReturn("onTheWay");
    	when(goodbyeSupplier.get()).thenReturn("goodbye");

        valueSupply.add(httpHeaderCategory, arrivalName, helloSupplier);
        valueSupply.add(httpHeaderCategory, approachingName, onTheWaySupplier);
        valueSupply.add(urlComponentCategory, departureName, goodbyeSupplier);


        allSuppliers = valueSupply.getAllCategorizedExpandedSuppliers();

        Map<String, Object> items = new HashMap<>();
        items.put(arrivalName, "hello");
        items.put(approachingName, "onTheWay");

        assertThat(allSuppliers).containsEntry(httpHeaderCategory, items);
        assertThat(allSuppliers).containsEntry(urlComponentCategory, new ImmutableMap.Builder<String, Object>().put(departureName, "goodbye").build());
    }

}
