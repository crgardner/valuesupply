package valuesupply;

import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import util.function.Consumer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ValueSupplyTest {
    private ValueSupplyCategory httpHeaderCategory;
    private ValueSupplyCategory urlComponentCategory;
    private ValueSupplyCategory pendingResolutionCategory;

    private ValueSupply valueSupply;

    private String helloName;
    private String goodbyeName;
    private String enRouteName;
    private String pendingName;

    private Supplier<Object> helloSupplier;
    private Supplier<Object> enRouteSupplier;
    private Supplier<Object> goodbyeSupplier;
    private Supplier<Object> resolvedSupplier;
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
        helloName = "hello";
        goodbyeName = "goodbye";
        enRouteName = "enRoute";
        pendingName = "pending";

        httpHeaderCategory = new BasicValueSupplyCategory("http-header");
        urlComponentCategory = new BasicValueSupplyCategory("url-component");
        pendingResolutionCategory = new BasicValueSupplyCategory("toBeResolved");

        helloSupplier = Suppliers.<Object> ofInstance("hello");
        enRouteSupplier = Suppliers.<Object> ofInstance("onTheWay");
        goodbyeSupplier = Suppliers.<Object> ofInstance("goodbye");
        resolvedSupplier = Suppliers.<Object> ofInstance("resolved");
        toBeReplacedSupplier = Suppliers.<Object> ofInstance("toBeReplaced");

        valueSupply.add(httpHeaderCategory, helloName, helloSupplier);
        valueSupply.add(httpHeaderCategory, enRouteName, enRouteSupplier);
        valueSupply.add(urlComponentCategory, goodbyeName, goodbyeSupplier);
    }

    @Test
    public void suppliesAllOfSpecifiedCategory() {
        valueSupply.supplyAllOf(httpHeaderCategory, allConsumer);

        verify(allConsumer).accept(aMapOf(helloName, helloSupplier.get(),
                                          enRouteName, enRouteSupplier.get()));
    }

    @Test
    public void suppliesEachOfSpecifiedCategory() {
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
        verifyConsumerAcceptsValueSupplyItemOf(enRouteName, enRouteSupplier);
    }

    @Test
    public void addsNewItemBasedOnDescriptor() throws Exception {
        when(supplierFactory.create(StandardValueType.LocalDate)).thenReturn(resolvedSupplier);

        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(urlComponentCategory,
                                                                 "asOfDate", StandardValueType.LocalDate));
        valueSupply.supplyEachOf(urlComponentCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf("asOfDate", resolvedSupplier);
    }

    @Test
    public void addsToPendingWhenDescriptorIndicatesResolutionIsRequired() throws Exception {
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(pendingResolutionCategory,
                                                                 pendingName, StandardValueType.String));
        valueSupply.supplyEachOf(pendingResolutionCategory, eachConsumer);

        verify(eachConsumer, never()).accept(any(ValueSupplyItem.class));
    }

    @Test(expected=UnknownSupplierException.class)
    public void rejectsAttemptsToAddUnresolvableSupplier() throws Exception {
        when(supplierFactory.create(StandardValueType.LocalDate)).thenThrow(new UnknownSupplierException());

        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(urlComponentCategory,
                                                                 "asOfDate", StandardValueType.LocalDate));
    }

    @Test
    public void offersResolutionOfSinglePendingSupplier() throws Exception {
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(pendingResolutionCategory, pendingName, StandardValueType.String));
        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.of(resolvedSupplier);
            }
        });

        valueSupply.supplyEachOf(pendingResolutionCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(pendingName, resolvedSupplier);
    }

    @Test
    public void offersResolutionOfMultiplePendingSuppliers() throws Exception {
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(pendingResolutionCategory, pendingName, StandardValueType.String));
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(pendingResolutionCategory, "anotherPending", StandardValueType.String));

        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.of((itemName == pendingName) ? helloSupplier : goodbyeSupplier);
            }
        });

        valueSupply.supplyEachOf(pendingResolutionCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(pendingName, helloSupplier);
        verifyConsumerAcceptsValueSupplyItemOf("anotherPending", goodbyeSupplier);
    }

    @Test
    public void ignoresUnresolvedPendingSuppliers() throws Exception {
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(pendingResolutionCategory, pendingName, StandardValueType.String));
        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.absent();
            }
        });

        valueSupply.supplyEachOf(pendingResolutionCategory, eachConsumer);

        verify(eachConsumer, never()).accept(any(ValueSupplyItem.class));
    }

    @Test
    public void ignoresReplacingPreviousPendingSupplier() throws Exception {
        valueSupply.addItemBasedOn(new ValueSupplyItemDescriptor(pendingResolutionCategory, pendingName, StandardValueType.String));
        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.of(toBeReplacedSupplier);
            }
        });

        valueSupply.resolvePending(new Function<String, Optional<Supplier<Object>>>() {

            @Override
            public Optional<Supplier<Object>> apply(String itemName) {
                return Optional.of(resolvedSupplier);
            }
        });

        valueSupply.supplyEachOf(pendingResolutionCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(pendingName, toBeReplacedSupplier);
        verify(eachConsumer, never()).accept(refEq(new ValueSupplyItem(pendingName, resolvedSupplier)));
    }

    private Map<String, Object> aMapOf(String key1, Object value1, String key2, Object value2) {
        return ImmutableMap.<String, Object>of(key1, value1, key2, value2);
    }

    private void verifyConsumerAcceptsValueSupplyItemOf(String name, Supplier<Object> supplier) {
        verify(eachConsumer).accept(refEq(new ValueSupplyItem(name, supplier)));
    }
}
