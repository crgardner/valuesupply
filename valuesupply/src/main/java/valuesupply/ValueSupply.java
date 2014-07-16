package valuesupply;

import java.util.HashMap;
import java.util.Map;

import util.function.Consumer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class ValueSupply {
    private final Table<ValueSupplyCategory, String, ValueSupplyItem> suppliers = HashBasedTable.create();
    private final Map<ValueSupplyCategory, String> pendingSuppliers = new HashMap<>();
    private final SupplierFactory supplierFactory;

    public ValueSupply(SupplierFactory supplierFactory) {
        this.supplierFactory = supplierFactory;
    }

    public void add(ValueSupplyCategory category, String name,
            Supplier<Object> supplier) {
        suppliers.put(category, name, new ValueSupplyItem(name, supplier));
    }

    public void supplyAllOf(ValueSupplyCategory category, Consumer<Map<String, Object>> allConsumer) {
        allConsumer.accept(itemsWith(category));
    }

    private Map<String, Object> itemsWith(ValueSupplyCategory category) {
        Map<String, ValueSupplyItem> categoryItemsByName = suppliers.row(category);

        return Maps.transformValues(categoryItemsByName, new Function<ValueSupplyItem, Object>() {

            @Override
            public Object apply(ValueSupplyItem item) {
                return item.valueAsString();
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

    public void add(ValueSupplyCategory category, String nameWithTypeIndicator) throws UnknownSupplierException {
        Supplier<Object> supplier = supplierFactory.create(resolveSupplierTypeName(nameWithTypeIndicator));

        add(category, nameWithTypeIndicator, supplier);
    }

    private String resolveSupplierTypeName(String nameWithTypeIndicator) {
        String[] components = nameWithTypeIndicator.split(":");
        Preconditions.checkArgument(components.length == 2);
        return components[1];
    }
}
