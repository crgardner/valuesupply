package valuesupply;

import java.util.HashMap;
import java.util.Map;

import util.function.Consumer;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ValueSupply {
    private final Multimap<ValueSupplyCategory, ValueSupplyItem> experimentalSuppliers = HashMultimap.create();

    public void add(ValueSupplyCategory category, String name,
            Supplier<Object> supplier) {
        experimentalSuppliers.put(category, new ValueSupplyItem(name, supplier));
    }

    public void supplyAllOf(ValueSupplyCategory category, Consumer<Map<String, Object>> allConsumer) {
        Map<String, Object> itemsByName = new HashMap<>();

        for (ValueSupplyItem valueSupplyItem : experimentalSuppliers.get(category)) {
            itemsByName.put(valueSupplyItem.getName(), valueSupplyItem.getValue());
        }
        allConsumer.accept(itemsByName);
    }

    public void supplyEachOf(ValueSupplyCategory category,
            Consumer<ValueSupplyItem> eachConsumer) {
        for (ValueSupplyItem valueSupplyItem : experimentalSuppliers.get(category)) {
            eachConsumer.accept(valueSupplyItem);
        }
    }
}
