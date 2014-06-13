package valuesupply;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import util.function.Consumer;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
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

    @Mock
    private Consumer<Map<String, Object>> allConsumer;

    @Mock
    private Consumer<Map.Entry<String, Object>> eachConsumer;

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

    @Test
    public void suppliesAllOfSpecifiedCategory() {
        valueSupply.supplyAllOf(httpHeaderCategory, allConsumer);

        verify(allConsumer).accept(ImmutableMap.<String, Object>of(arrivalName, helloSupplier.get(),
                                                                   approachingName, onTheWaySupplier.get()));
    }

    @Test
    public void suppliesEachOfSpecifiedCategory() {
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        ImmutableList<Entry<String, Object>> of = ImmutableMap.<String, Object>of(arrivalName, helloSupplier.get(),
                approachingName, onTheWaySupplier.get()).entrySet().asList();

        verify(eachConsumer).accept(of.get(0));
        verify(eachConsumer).accept(of.get(1));
    }
}
