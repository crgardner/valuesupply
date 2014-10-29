package valuesupply;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.HashBasedTable;
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
        Map<String, ValueSupplyItem> itemsByName = suppliers.row(category);
        
        return itemsByName.entrySet().stream()
        				  .collect(toMap(Entry::getKey, entry -> entry.getValue().valueAsString()));
    }

    public void supplyEachOf(ValueSupplyCategory category, Consumer<ValueSupplyItem> eachConsumer) {
    	suppliers.row(category).values().forEach(item -> eachConsumer.accept(item));
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
}
