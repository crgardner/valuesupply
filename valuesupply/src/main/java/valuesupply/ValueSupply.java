package valuesupply;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Supplier;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ValueSupply {

    private final Table<ValueSupplyCategory, String, Supplier<Object>> suppliers = HashBasedTable.create();
    private final SupplierMapExpander expander = new SupplierMapExpander();

    public void add(ValueSupplyCategory category, String name,
            Supplier<Object> supplier) {
        suppliers.put(category, name, supplier);
    }

    public Map<ValueSupplyCategory, Map<String, Object>> getAllCategorizedExpandedSuppliers() {
        return findExpandedSuppliersOf(allCategories());
    }

    private ValueSupplyCategory[] allCategories() {
        return suppliers.rowKeySet().toArray(new ValueSupplyCategory[0]);
    }

    public Map<ValueSupplyCategory, Map<String, Object>> findExpandedSuppliersOf(
            ValueSupplyCategory... categories) {
        Map<ValueSupplyCategory, Map<String, Object>> expandedSuppliers = new HashMap<>();

        for (ValueSupplyCategory category : categories) {
            expandedSuppliers.put(category, expander.expand(suppliers.row(category)));
        }
        return expandedSuppliers;
    }
}
