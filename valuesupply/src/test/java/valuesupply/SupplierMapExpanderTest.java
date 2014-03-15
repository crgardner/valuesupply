package valuesupply;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class SupplierMapExpanderTest {
    
    private Map<String, Supplier<String>> suppliers;
    private Map<String, String> expandedSuppliers;
    private SupplierMapExpander expander;

    @Mock
    private Supplier<String> firstSupplier;
    
    @Mock
    private Supplier<String> secondSupplier;

    @Before
    public void setUp() {
        suppliers = ImmutableMap.of("first", firstSupplier, "second", secondSupplier);
        expander = new SupplierMapExpander();
    }
    
    @Test
    public void expandsAllMappedSuppliers() {
        expectingSuppliersToProvideValues();
        
        expandedSuppliers = expander.expand(suppliers);
        
        assertThat(expandedSuppliers).containsEntry("first", "hello")
                                     .containsEntry("second", "goodbye");
    }

    private void expectingSuppliersToProvideValues() {
        when(firstSupplier.get()).thenReturn("hello");
        when(secondSupplier.get()).thenReturn("goodbye");
    }

}
