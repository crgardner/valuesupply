package valuesupply;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import util.function.Consumer;

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

    public void supplyAllOf(ValueSupplyCategory category, Consumer<Map<String, Object>> allConsumer) {
        allConsumer.accept(findExpandedSuppliersOf(category).get(category));
    }

    public void supplyEachOf(ValueSupplyCategory category, Consumer<Entry<String, Object>> eachConsumer) {
        for (Entry<String, Object> entry : findExpandedSuppliersOf(category).get(category).entrySet()) {
            eachConsumer.accept(entry);
        }
    }
}
