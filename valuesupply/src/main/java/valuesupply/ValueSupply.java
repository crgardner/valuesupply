package valuesupply;

import java.util.HashMap;
import java.util.Map;

import util.function.Consumer;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ValueSupply {
    private final Multimap<ValueSupplyCategory, ValueSupplyItem> suppliers = HashMultimap.create();

    public void add(ValueSupplyCategory category, String name,
            Supplier<Object> supplier) {
        suppliers.put(category, new ValueSupplyItem(name, supplier));
    }

    public void supplyAllOf(ValueSupplyCategory category, Consumer<Map<String, Object>> allConsumer) {
        allConsumer.accept(itemsWith(category));
    }

    private Map<String, Object> itemsWith(ValueSupplyCategory category) {
        Map<String, Object> itemsByName = new HashMap<>();

        for (ValueSupplyItem valueSupplyItem : suppliers.get(category)) {
            itemsByName.put(valueSupplyItem.name(), valueSupplyItem.valueAsString());
        }
        return itemsByName;
    }

    public void supplyEachOf(ValueSupplyCategory category, Consumer<ValueSupplyItem> eachConsumer) {
        for (ValueSupplyItem valueSupplyItem : suppliers.get(category)) {
            eachConsumer.accept(valueSupplyItem);
        }
    }
}
