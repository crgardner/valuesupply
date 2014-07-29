package valuesupply;

import java.util.*;

import util.function.Consumer;

import com.google.common.base.*;
import com.google.common.collect.*;

public class ValueSupply {
    private final Table<ValueSupplyCategory, String, ValueSupplyItem> resolvedSupplyItems = HashBasedTable.create();
    private final Set<ValueSupplyItemDescriptor> pendingSupplyItems = new HashSet<>();
    private final SupplierFactory knownSupplierFactory;

    public ValueSupply(SupplierFactory knownSupplierFactory) {
        this.knownSupplierFactory = knownSupplierFactory;
    }

    public void addItemBasedOn(ValueSupplyItemDescriptor descriptor) throws UnknownSupplierException {
        if (descriptor.isResolutionRequired()) {
            pendingSupplyItems.add(descriptor);
            return;
        }

        doAdd(descriptor);
    }

    private void doAdd(ValueSupplyItemDescriptor descriptor) throws UnknownSupplierException {
        Optional<Supplier<Object>> supplierCandidate = knownSupplierFactory.createFrom(descriptor);

        verifyAvailability(supplierCandidate);
        addToResolvedIfPossible(descriptor, supplierCandidate);
    }

    private void verifyAvailability(Optional<Supplier<Object>> supplierCandidate) throws UnknownSupplierException {
        if (!supplierCandidate.isPresent()) {
            throw new UnknownSupplierException();
        }
    }

    private void addToResolvedIfPossible(ValueSupplyItemDescriptor descriptor, Optional<Supplier<Object>> supplierCandidate) {
        if (supplierCandidate.isPresent()) {
            resolvedSupplyItems.put(descriptor.getValueSupplyCategory(), descriptor.getName(), new ValueSupplyItem(descriptor.getName(), supplierCandidate.get()));
        }
    }

    public void supplyAllOf(ValueSupplyCategory category, Consumer<Map<String, Object>> allConsumer) {
        allConsumer.accept(itemsWith(category));
    }

    private Map<String, Object> itemsWith(ValueSupplyCategory category) {
        Map<String, ValueSupplyItem> categoryItemsByName = resolvedSupplyItems.row(category);

        return Maps.transformValues(categoryItemsByName, new Function<ValueSupplyItem, Object>() {

            @Override
            public Object apply(ValueSupplyItem item) {
                return item.valueAsString();
            }
        });
    }

    public void supplyEachOf(ValueSupplyCategory category, Consumer<ValueSupplyItem> eachConsumer) {
        for (ValueSupplyItem valueSupplyItem : resolvedSupplyItems.row(category).values()) {
            eachConsumer.accept(valueSupplyItem);
        }
    }

    public void resolvePendingItems(SupplierFactory runtimeSupplierFactory) {
        for (Iterator<ValueSupplyItemDescriptor> iterator = pendingSupplyItems.iterator(); iterator.hasNext();) {
            resolvePendingItem(runtimeSupplierFactory, iterator);
        }
    }

    private void resolvePendingItem(SupplierFactory runtimeSupplierFactory, Iterator<ValueSupplyItemDescriptor> iterator) {
        ValueSupplyItemDescriptor pending = iterator.next();

        Optional<Supplier<Object>> supplierCandidate = runtimeSupplierFactory.createFrom(pending);

        addToResolvedIfPossible(pending, supplierCandidate);
        removeFromPendingIfNecessary(iterator, supplierCandidate);
    }

    private void removeFromPendingIfNecessary(Iterator<ValueSupplyItemDescriptor> iterator, Optional<Supplier<Object>> supplierCandidate) {
        if (supplierCandidate.isPresent()) {
            iterator.remove();
        }
    }
}
