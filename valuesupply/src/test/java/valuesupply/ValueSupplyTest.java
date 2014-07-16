package valuesupply;

import static org.mockito.Mockito.*;

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
    private ValueSupplyCategory httpHeaderCategory;
    private ValueSupplyCategory urlComponentCategory;
    private ValueSupplyCategory pendingResolutionCategory;

    private ValueSupply valueSupply;
    private String arrivalName;
    private String departureName;
    private String approachingName;

    private Supplier<Object> helloSupplier;
    private Supplier<Object> onTheWaySupplier;
    private Supplier<Object> goodbyeSupplier;

    @Mock
    private Supplier<Object> resolvedSupplier;

    @Mock
    private Supplier<Object> toBeReplacedSupplier;

    @Mock
    private Consumer<Map<String, Object>> allConsumer;

    @Mock
    private Consumer<ValueSupplyItem> eachConsumer;

    @Mock
    private SupplierFactory supplierFactory;

    @Before
    public void setUp() {
        valueSupply = new ValueSupply(supplierFactory);
        arrivalName = "arrival";
        departureName = "departure";
        approachingName = "approaching";

        httpHeaderCategory = new BasicValueSupplyCategory("http-header");
        urlComponentCategory = new BasicValueSupplyCategory("url-component");
        pendingResolutionCategory = new BasicValueSupplyCategory("toBeResolved");

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

    @Test
    public void resolvesSupplierBasedOnTypedParameter() throws Exception {
        when(supplierFactory.create("LocalDate")).thenReturn(resolvedSupplier);

        valueSupply.add(urlComponentCategory, "asOfDate:LocalDate");
        valueSupply.supplyEachOf(urlComponentCategory, eachConsumer);

        verify(eachConsumer).accept(refEq(new ValueSupplyItem("asOfDate:LocalDate", resolvedSupplier)));
    }

    @Test(expected=UnknownSupplierException.class)
    public void rejectsAttemptsToAddUnresolvableSupplier() throws Exception {
        when(supplierFactory.create("LocalDate")).thenThrow(new UnknownSupplierException());

        valueSupply.add(urlComponentCategory, "asOfDate:LocalDate");
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsAttemptsToAddUntypedName() throws Exception {
        valueSupply.add(urlComponentCategory, "asOfDate");
    }

    @Test
    public void allowsForPendingSuppliers() {
        valueSupply.pend(pendingResolutionCategory, "pending");
        valueSupply.resolvePending(pendingResolutionCategory, "pending", resolvedSupplier);
        valueSupply.supplyEachOf(pendingResolutionCategory, eachConsumer);

        verify(eachConsumer).accept(refEq(new ValueSupplyItem("pending", resolvedSupplier)));
    }

    @Test
    public void replacesPreviousPendingSupplier() {
        valueSupply.pend(pendingResolutionCategory, "pending");
        valueSupply.resolvePending(pendingResolutionCategory, "pending", toBeReplacedSupplier);
        valueSupply.resolvePending(pendingResolutionCategory, "pending", resolvedSupplier);

        valueSupply.supplyEachOf(pendingResolutionCategory, eachConsumer);

        verify(eachConsumer).accept(refEq(new ValueSupplyItem("pending", resolvedSupplier)));
        verify(eachConsumer, never()).accept(refEq(new ValueSupplyItem("pending", toBeReplacedSupplier)));
    }
}
