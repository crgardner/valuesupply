package valuesupply;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ValueSupplyTest {
    private ValueSupplyCategory httpHeaderCategory;
    private ValueSupplyCategory urlComponentCategory;

    private ValueSupply valueSupply;

    private String helloName;
    private String enRouteName;
    private String goodbyeName;

    private Supplier<Object> helloSupplier;
    private Supplier<Object> enRouteSupplier;
    private Supplier<Object> goodbyeSupplier;

    @Mock
    private java.util.function.Consumer<Map<String, Object>> allConsumer;

    @Mock
    private java.util.function.Consumer<ValueSupplyItem> eachConsumer;

    @Mock
    private SupplierFactory supplierFactory;

    @Before
    public void setUp() {
        valueSupply = new ValueSupply(supplierFactory);
        helloName = "hello";
        enRouteName = "enRoute";
        goodbyeName = "goodbye";

        httpHeaderCategory = new BasicValueSupplyCategory("http-header");
        urlComponentCategory = new BasicValueSupplyCategory("url-component");

        helloSupplier = Suppliers.<Object> ofInstance("hello");
        enRouteSupplier = Suppliers.<Object> ofInstance("onTheWay");
        goodbyeSupplier = Suppliers.<Object> ofInstance("goodbye");
    }

    @Test
    public void suppliesAllOfSpecifiedCategory() throws Exception {
        havingASupplierFactoryWithAllKnownSuppliers();

        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, helloName));
        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, enRouteName));

        valueSupply.supplyAllOf(httpHeaderCategory, allConsumer);

        verify(allConsumer).accept(aMapOf(helloName, helloSupplier.get(),
                                          enRouteName, enRouteSupplier.get()));
    }

    @Test
    public void suppliesEachOfSpecifiedCategory() throws Exception {
        havingASupplierFactoryWithAllKnownSuppliers();

        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, helloName));
        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, enRouteName));

        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
        verifyConsumerAcceptsValueSupplyItemOf(enRouteName, enRouteSupplier);
    }

    @Test
    public void addsToPendingWhenDescriptorIndicatesResolutionIsRequired() throws Exception {
        valueSupply.addItemBasedOn(aValueSupplyItemDescriptor(httpHeaderCategory, helloName, StandardValueType.String));

        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verify(eachConsumer, never()).accept(any(ValueSupplyItem.class));
    }

    @Test(expected=UnknownSupplierException.class)
    public void rejectsAttemptsToAddUnresolvableSupplier() throws Exception {
        when(supplierFactory.create(any(ValueSupplyItemDescriptor.class))).thenThrow(new UnknownSupplierException());

        valueSupply.addItemBasedOn(aValueSupplyItemDescriptor(urlComponentCategory, "asOfDate", StandardValueType.LocalDate));
    }

    @Test
    public void offersResolutionOfSinglePendingSupplier() throws Exception {
        valueSupply.addItemBasedOn(aValueSupplyItemDescriptor(httpHeaderCategory, helloName, StandardValueType.String));

        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.of(helloSupplier);
            }
        });

        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
    }

    @Test
    public void ensuresResolvedItemsAreNoLongerPending() throws Exception {
        Function<String, Optional<Supplier<Object>>> supplierResolver = spy(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.of(helloSupplier);
            }
        });

        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(httpHeaderCategory, helloName, StandardValueType.String));

        valueSupply.resolvePending(supplierResolver);
        valueSupply.resolvePending(supplierResolver);

        verify(supplierResolver, times(1)).apply(helloName);
    }

    @Test
    public void offersResolutionOfMultiplePendingSuppliers() throws Exception {
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(httpHeaderCategory, helloName, StandardValueType.String));
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(httpHeaderCategory, goodbyeName, StandardValueType.String));

        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.of((itemName == helloName) ? helloSupplier : goodbyeSupplier);
            }
        });

        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
        verifyConsumerAcceptsValueSupplyItemOf(goodbyeName, goodbyeSupplier);
    }

    @Test
    public void ignoresUnresolvedPendingSuppliers() throws Exception {
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(httpHeaderCategory, helloName, StandardValueType.String));

        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.absent();
            }
        });

        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verify(eachConsumer, never()).accept(any(ValueSupplyItem.class));
    }

    private Map<String, Object> aMapOf(String key1, Object value1, String key2, Object value2) {
        return ImmutableMap.<String, Object>of(key1, value1, key2, value2);
    }

    private void verifyConsumerAcceptsValueSupplyItemOf(String name, Supplier<Object> supplier) {
        verify(eachConsumer).accept(refEq(new ValueSupplyItem(name, supplier)));
    }

    private void havingASupplierFactoryWithAllKnownSuppliers() throws UnknownSupplierException {
        when(supplierFactory.create(any(ValueSupplyItemDescriptor.class))).thenReturn(helloSupplier).thenReturn(enRouteSupplier);
    }

    private ValueSupplyItemDescriptor aValueSupplyItemDescriptor(
            ValueSupplyCategory valueSupplyCategory, String name, ValueType valueType) {
        return new ValueSupplyItemDescriptor(valueSupplyCategory, name, valueType);
    }

    private ValueSupplyItemDescriptor aResolvableValueSupplyDescriptor(
            ValueSupplyCategory valueSupplyCategory, String name) {
        return new ValueSupplyItemDescriptor(valueSupplyCategory, name, StandardValueType.String, "placeholder");
    }
}
