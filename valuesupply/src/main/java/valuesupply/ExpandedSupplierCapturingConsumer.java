package valuesupply;

import java.util.*;

import com.google.common.base.Supplier;

public class ExpandedSupplierCapturingConsumer implements ValueConsumer {
    private Map<ValueSupplyCategory, Map<String, Object>> expandedSuppliers = new HashMap<>();
    public Map<ValueSupplyCategory, Map<String, Object>> getExpandedSuppliers() {
        return expandedSuppliers;
    }

    private SupplierMapExpander expander = new SupplierMapExpander();

    @Override
    public void accept(ValueSupplyCategory category, Map<String, Supplier<Object>> suppliers) {
        expandedSuppliers.put(category, expander.expand(suppliers));
    }
}