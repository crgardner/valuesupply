package valuesupply;

import java.util.HashMap;
import java.util.Map;

import util.function.Consumer;

import com.google.common.base.Supplier;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class ValueSupply {
    private final Table<ValueSupplyCategory, String, ValueSupplyItem> suppliers = HashBasedTable.create();
    private final Map<ValueSupplyCategory, String> pendingSuppliers = new HashMap<>();

    public void add(ValueSupplyCategory category, String name,
            Supplier<Object> supplier) {
        suppliers.put(category, name, new ValueSupplyItem(name, supplier));
    }

    public void supplyAllOf(ValueSupplyCategory category, Consumer<Map<String, Object>> allConsumer) {
        allConsumer.accept(altitemsWith(category));
    }

    private Map<String, Object> altitemsWith(ValueSupplyCategory category) {
        Map<String, ValueSupplyItem> row = suppliers.row(category);

        return Maps.transformEntries(row, new Maps.EntryTransformer<String, ValueSupplyItem, Object>() {

            @Override
            public Object transformEntry(String key, ValueSupplyItem value) {
                return value.valueAsString();
            }
        });
    }

    public void supplyEachOf(ValueSupplyCategory category, Consumer<ValueSupplyItem> eachConsumer) {
        for (ValueSupplyItem valueSupplyItem : suppliers.row(category).values()) {
            eachConsumer.accept(valueSupplyItem);
        }
    }

    public void pend(ValueSupplyCategory pendingResolutionCategory, String name) {
        pendingSuppliers.put(pendingResolutionCategory, name);
    }

    public void resolvePending(ValueSupplyCategory category, String name,
            Supplier<Object> resolvedSupplier) {
        add(category, pendingSuppliers.get(category), resolvedSupplier);
    }
}
