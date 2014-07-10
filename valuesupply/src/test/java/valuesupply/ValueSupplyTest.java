package valuesupply;

import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import util.function.Consumer;

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

    @Mock
    private Consumer<Map<String, Object>> allConsumer;

    @Mock
    private Consumer<ValueSupplyItem> eachConsumer;

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
    public void suppliesAllOfSpecifiedCategory() {
        valueSupply.supplyAllOf(httpHeaderCategory, allConsumer);

        verify(allConsumer).accept(ImmutableMap.<String, Object>of(arrivalName, helloSupplier.get(),
                                                                   approachingName, onTheWaySupplier.get()));
    }

    @Test
    public void suppliesEachOfSpecifiedCategory() {
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verify(eachConsumer).accept(refEq(new ValueSupplyItem(arrivalName, helloSupplier)));
        verify(eachConsumer).accept(refEq(new ValueSupplyItem(approachingName, onTheWaySupplier)));
    }
}
