package valuesupply;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import util.function.Consumer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class ValueSupply {
    private final Table<ValueSupplyCategory, String, ValueSupplyItem> suppliers = HashBasedTable.create();
    private final Map<String, ValueSupplyCategory> pendingSuppliers = new HashMap<>();
    private final SupplierFactory supplierFactory;

    public ValueSupply(SupplierFactory supplierFactory) {
        this.supplierFactory = supplierFactory;
    }

    public void add(ValueSupplyCategory category, String name, Supplier<Object> supplier) {
        suppliers.put(category, name, new ValueSupplyItem(name, supplier));
    }

    public void addItemBasedOn(ValueSupplyItemDescriptor descriptor) throws UnknownSupplierException {
        if (descriptor.isResolutionRequired()) {
            pend(descriptor.getValueSupplyCategory(), descriptor.getName());
            return;
        }

        Supplier<Object> supplier = supplierFactory.create(descriptor);

        add(descriptor.getValueSupplyCategory(), descriptor.getName(), supplier);

    }

    private void pend(ValueSupplyCategory pendingResolutionCategory, String name) {
        pendingSuppliers.put(name, pendingResolutionCategory);
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

    public void resolvePending(Function<String, Optional<Supplier<Object>>> supplierResolver) {
        for (Iterator<Entry<String, ValueSupplyCategory>> iterator = pendingSuppliers.entrySet().iterator(); iterator.hasNext();) {
            Entry<String, ValueSupplyCategory> pending = iterator.next();
            Optional<Supplier<Object>> supplierCandidate = supplierResolver.apply(pending.getKey());

            if (supplierCandidate.isPresent()) {
                add(pending.getValue(), pending.getKey(), supplierCandidate.get());
                iterator.remove();
            }
        }

    }

    public void resolvePending(AltSupplierFactory supplierFactory) {
        for (Iterator<Entry<String, ValueSupplyCategory>> iterator = pendingSuppliers.entrySet().iterator(); iterator.hasNext();) {
            Entry<String, ValueSupplyCategory> pending = iterator.next();
            Optional<Supplier<Object>> supplierCandidate = supplierFactory.create(pending.getKey(), StandardValueType.String, "");

            if (supplierCandidate.isPresent()) {
                add(pending.getValue(), pending.getKey(), supplierCandidate.get());
                iterator.remove();
            }
        }


    }
}
