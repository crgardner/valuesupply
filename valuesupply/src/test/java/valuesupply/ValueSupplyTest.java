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

        verify(allConsumer).accept(
                ImmutableMap.<String, Object> of(helloName, helloSupplier.get(), enRouteName,
                        enRouteSupplier.get()));
    }

    @Test
    public void suppliesEachOfSpecifiedCategory() {
        valueSupply.supplyEachOf(httpHeaderCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf(helloName, helloSupplier);
        verifyConsumerAcceptsValueSupplyItemOf(enRouteName, enRouteSupplier);
    }

    @Test
    public void resolvesSupplierBasedOnTypedParameter() throws Exception {
        when(supplierFactory.create("LocalDate")).thenReturn(resolvedSupplier);

        valueSupply.add(urlComponentCategory, "asOfDate:LocalDate");
        valueSupply.supplyEachOf(urlComponentCategory, eachConsumer);

        verifyConsumerAcceptsValueSupplyItemOf("asOfDate:LocalDate", resolvedSupplier);
    }

    @Test(expected = UnknownSupplierException.class)
    public void rejectsAttemptsToAddUnresolvableSupplier() throws Exception {
        when(supplierFactory.create("LocalDate")).thenThrow(new UnknownSupplierException());

        valueSupply.add(urlComponentCategory, "asOfDate:LocalDate");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAttemptsToAddUntypedName() throws Exception {
        valueSupply.add(urlComponentCategory, "asOfDate");
    }

    @Test
    public void offersResolutionOfPendingSuppliers() {
        valueSupply.pend(pendingResolutionCategory, pendingName);
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
    public void ignoresUnresolvedPendingSuppliers() {
        valueSupply.pend(pendingResolutionCategory, pendingName);
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
    public void ignoresReplacingPreviousPendingSupplier() {
        valueSupply.pend(pendingResolutionCategory, pendingName);
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


    private void verifyConsumerAcceptsValueSupplyItemOf(String name, Supplier<Object> supplier) {
        verify(eachConsumer).accept(refEq(new ValueSupplyItem(name, supplier)));
    }
}
