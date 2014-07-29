package valuesupply;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import util.function.Consumer;

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ValueSupplyTest {
    private ValueSupplyCategory httpHeaderCategory;
    private ValueSupplyCategory urlComponentCategory;

    private ValueSupply valueSupply;
    private ValueSupplyItemDescriptor helloDescriptor;

    private String helloName;
    private String enRouteName;
    private String goodbyeName;

    private Supplier<Object> helloSupplier;
    private Supplier<Object> enRouteSupplier;
    private Supplier<Object> goodbyeSupplier;

    @Mock
    private Consumer<Map<String, Object>> allConsumer;

    @Mock
    private Consumer<ValueSupplyItem> eachConsumer;

    @Mock
    private SupplierFactory knownSupplierFactory;

    @Mock
    private SupplierFactory runtimeSupplierFactory;

    @Before
    public void setUp() {
        valueSupply = new ValueSupply(knownSupplierFactory);
        helloName = "hello";
        enRouteName = "enRoute";
        goodbyeName = "goodbye";

        httpHeaderCategory = new BasicValueSupplyCategory("http-header");
        urlComponentCategory = new BasicValueSupplyCategory("url-component");

        helloSupplier = Suppliers.<Object> ofInstance("hello");
        enRouteSupplier = Suppliers.<Object> ofInstance("onTheWay");
        goodbyeSupplier = Suppliers.<Object> ofInstance("goodbye");

        helloDescriptor = aValueSupplyItemDescriptor(httpHeaderCategory, helloName, StandardValueType.String);
    }

    @Test
    public void suppliesAllOfSpecifiedCategory() throws Exception {
        havingASupplierFactoryWithAllKnownSuppliers();

        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, helloName));
        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, enRouteName));
        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(urlComponentCategory, goodbyeName));
        valueSupply.supplyAllOf(httpHeaderCategory, allConsumer);

        verify(allConsumer).accept(aMapOf(helloName, helloSupplier.get(),
                                          enRouteName, enRouteSupplier.get()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sendsEmptyMapForSupplyAllRequestForUnknownCategory() throws Exception {
        valueSupply.supplyAllOf(httpHeaderCategory, allConsumer);

        verify(allConsumer).accept(Collections.EMPTY_MAP);
    }

    @Test
    public void suppliesEachOfSpecifiedCategory() throws Exception {
        havingASupplierFactoryWithAllKnownSuppliers();

        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, helloName));
        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(httpHeaderCategory, enRouteName));
        valueSupply.addItemBasedOn(aResolvableValueSupplyDescriptor(urlComponentCategory, goodbyeName));
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
        verifyConsumerAcceptsValueSupplyItemOf(enRouteName, enRouteSupplier);
    }

    @Test
    public void ignoresSupplyEachRequestForUnknownCategory() throws Exception {
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyZeroInteractions(eachConsumer);
    }

    @Test
    public void addsToPendingWhenDescriptorIndicatesResolutionIsRequired() throws Exception {
        valueSupply.addItemBasedOn(helloDescriptor);
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyZeroInteractions(eachConsumer);
    }

    @Test(expected=UnknownSupplierException.class)
    public void rejectsAttemptsToAddUnresolvableSupplier() throws Exception {
        when(knownSupplierFactory.createFrom(any(ValueSupplyItemDescriptor.class))).thenReturn(Optional.<Supplier<Object>>absent());

        valueSupply.addItemBasedOn(aValueSupplyItemDescriptor(urlComponentCategory, "asOfDate", StandardValueType.LocalDate));
    }

    @Test
    public void offersResolutionOfSinglePendingSupplier() throws Exception {
        when(runtimeSupplierFactory.createFrom(helloDescriptor)).thenReturn(Optional.of(helloSupplier));

        valueSupply.addItemBasedOn(helloDescriptor);
        valueSupply.resolvePendingItems(runtimeSupplierFactory);
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
    }

    @Test
    public void ensuresResolvedItemsAreNoLongerPending() throws Exception {
        when(runtimeSupplierFactory.createFrom(helloDescriptor)).thenReturn(Optional.of(helloSupplier));

        valueSupply.addItemBasedOn(helloDescriptor);
        valueSupply.resolvePendingItems(runtimeSupplierFactory);
        valueSupply.resolvePendingItems(runtimeSupplierFactory);

        verify(runtimeSupplierFactory, times(1)).createFrom(helloDescriptor);
    }

    @Test
    public void offersResolutionOfMultiplePendingSuppliers() throws Exception {
        valueSupply.addItemBasedOn(helloDescriptor);
        valueSupply.addItemBasedOn(aValueSupplyItemDescriptor(httpHeaderCategory, goodbyeName, StandardValueType.String));

        valueSupply.resolvePendingItems(new SupplierFactory() {

            @Override
            public Optional<Supplier<Object>> createFrom(ValueSupplyItemDescriptor descriptor) {
                return Optional.of((descriptor.getName() == helloName) ? helloSupplier : goodbyeSupplier);
            }
        });

        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
        verifyConsumerAcceptsValueSupplyItemOf(goodbyeName, goodbyeSupplier);
    }

    @Test
    public void ignoresUnresolvedPendingSuppliers() throws Exception {
        when(runtimeSupplierFactory.createFrom(helloDescriptor)).thenReturn(Optional.<Supplier<Object>>absent());

        valueSupply.addItemBasedOn(helloDescriptor);
        valueSupply.resolvePendingItems(runtimeSupplierFactory);
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyZeroInteractions(eachConsumer);
    }

    private Map<String, Object> aMapOf(String key1, Object value1, String key2, Object value2) {
        return ImmutableMap.<String, Object>of(key1, value1, key2, value2);
    }

    private void verifyConsumerAcceptsValueSupplyItemOf(String name, Supplier<Object> supplier) {
        verify(eachConsumer).accept(refEq(new ValueSupplyItem(name, supplier)));
    }

    private void havingASupplierFactoryWithAllKnownSuppliers() throws UnknownSupplierException {
        when(knownSupplierFactory.createFrom(any(ValueSupplyItemDescriptor.class))).thenReturn(Optional.of(helloSupplier)).thenReturn(Optional.of(enRouteSupplier));
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
