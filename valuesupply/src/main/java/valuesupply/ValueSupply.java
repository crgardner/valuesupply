package valuesupply;

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
            consumer.provide(category, suppliers.row(category));
        }
    }

}
