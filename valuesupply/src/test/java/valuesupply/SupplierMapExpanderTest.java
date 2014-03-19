package valuesupply;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class SupplierMapExpanderTest {

    private Map<String, Supplier<Object>> suppliers;
    private Map<String, Object> expandedSuppliers;
    private SupplierMapExpander expander;

    @Mock
    private Supplier<Object> firstSupplier;

    @Mock
    private Supplier<Object> secondSupplier;

    @Before
    public void setUp() {
        expander = new SupplierMapExpander();
    }

    @Test
    public void expandsAllMappedSuppliers() {
        havingsuppliersMappedWithKeys("first", "second");
        expectingSuppliersToProvideValues("hello", "goodbye");

        expandedSuppliers = expander.expand(suppliers);

        assertThat(expandedSuppliers).containsEntry("first", "hello")
                                     .containsEntry("second", "goodbye");
    }

    private void havingsuppliersMappedWithKeys(String firstKey, String secondKey) {
        suppliers = new ImmutableMap.Builder<String, Supplier<Object>>()
                .put(firstKey, firstSupplier).put(secondKey, secondSupplier).build();
    }

    private void expectingSuppliersToProvideValues(String firstValue, String secondValue) {
        when(firstSupplier.get()).thenReturn(firstValue);
        when(secondSupplier.get()).thenReturn(secondValue);
    }
}
