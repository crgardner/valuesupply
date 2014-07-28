package valuesupply;

import java.util.*;

import util.function.Consumer;

import com.google.common.base.*;
import com.google.common.collect.*;

public class ValueSupply {
    private final Table<ValueSupplyCategory, String, ValueSupplyItem> suppliers = HashBasedTable.create();
    private final Set<ValueSupplyItemDescriptor> pendingSuppliers = new HashSet<>();
    private final SupplierFactory knownSupplierFactory;

    public ValueSupply(SupplierFactory knownSupplierFactory) {
        this.knownSupplierFactory = knownSupplierFactory;
    }

    public void addItemBasedOn(ValueSupplyItemDescriptor descriptor)
            throws UnknownSupplierException {
        if (descriptor.isResolutionRequired()) {
            pendingSuppliers.add(descriptor);
            return;
        }

        Optional<Supplier<Object>> optionalSupplier = knownSupplierFactory.createFrom(descriptor);

        if (!optionalSupplier.isPresent()) {
            throw new UnknownSupplierException();
        }

        add(descriptor, optionalSupplier.get());
    }

    private void add(ValueSupplyItemDescriptor descriptor, Supplier<Object> supplier) {
        suppliers.put(descriptor.getValueSupplyCategory(), descriptor.getName(), new ValueSupplyItem(descriptor.getName(), supplier));
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

    public void resolvePending(SupplierFactory runTimeSupplierFactory) {
        for (Iterator<ValueSupplyItemDescriptor> iterator = pendingSuppliers.iterator(); iterator.hasNext();) {
            ValueSupplyItemDescriptor pending = iterator.next();
            Optional<Supplier<Object>> supplierCandidate = runTimeSupplierFactory.createFrom(pending);

            if (supplierCandidate.isPresent()) {
                add(pending, supplierCandidate.get());
                iterator.remove();
            }
        }
    }
}
