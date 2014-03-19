package valuesupply;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private Supplier<Object> helloSupplier;
    private Supplier<Object> onTheWaySupplier;
    private Supplier<Object> goodbyeSupplier;
    private Map<ValueSupplyCategory, Map<String, Object>> allSuppliers;

    @Before
    public void setUp() {
        valueSupply = new ValueSupply();
        arrivalName = "arrival";
        departureName = "departure";
        approachingName = "approaching";

        httpHeaderCategory = new BasicValueSupplyCategory("http-header");
        urlComponentCategory = new BasicValueSupplyCategory("url-component");

        helloSupplier = new KnownValueSupplier("hello");
        onTheWaySupplier = new KnownValueSupplier("onTheWay");
        goodbyeSupplier = new KnownValueSupplier("goodbye");

        valueSupply.add(httpHeaderCategory, arrivalName, helloSupplier);
        valueSupply.add(httpHeaderCategory, approachingName, onTheWaySupplier);
        valueSupply.add(urlComponentCategory, departureName, goodbyeSupplier);
    }

    @Test
    public void returnsAllCategorizedExpandedSuppliers() {

        allSuppliers = valueSupply.getAllCategorizedExpandedSuppliers();

        Map<String, Object> items = new HashMap<>();
        items.put(arrivalName, "hello");
        items.put(approachingName, "onTheWay");

        assertThat(allSuppliers).containsEntry(httpHeaderCategory, items);
        assertThat(allSuppliers).containsEntry(
                urlComponentCategory,
                new ImmutableMap.Builder<String, Object>().put(departureName,
                        "goodbye").build());
    }

    @Test
    public void returnsRequestedCategoryOfSupplier() throws Exception {
        Map<ValueSupplyCategory, Map<String, Object>> matching = valueSupply
                .findExpandedSuppliersOf(httpHeaderCategory);

        assertThat(matching.get(httpHeaderCategory)).containsEntry(arrivalName,
                helloSupplier.get()).containsEntry(approachingName,
                onTheWaySupplier.get());
    }
}
