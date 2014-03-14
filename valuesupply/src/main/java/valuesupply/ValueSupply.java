package valuesupply;

import java.util.Map;

import com.google.common.base.Supplier;
import com.google.common.collect.*;

public class ValueSupply {

    private Table<ValueSupplyCategory,String, Supplier<String>> suppliers = HashBasedTable.create();

    public void add(ValueSupplyCategory category, String name,
            Supplier<String> supplier) {
        suppliers.put(category, name, supplier);
    }

    public void provideAllTo(ValueConsumer consumer) {
        for (ValueSupplyCategory category : suppliers.rowKeySet()) {
            Map<String, Supplier<String>> row = suppliers.row(category);
            consumer.provide(category, row);
        }
    }

}
