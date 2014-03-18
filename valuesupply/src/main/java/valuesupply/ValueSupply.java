package valuesupply;

import java.util.Map;

import com.google.common.base.Supplier;
import com.google.common.collect.*;

public class ValueSupply {

    private final Table<ValueSupplyCategory, String, Supplier<Object>> suppliers = HashBasedTable.create();

    public void add(ValueSupplyCategory category, String name,
            Supplier<Object> supplier) {
        suppliers.put(category, name, supplier);
    }

    public void provideEachByCategoryTo(ValueConsumer consumer) {
        for (ValueSupplyCategory category : suppliers.rowKeySet()) {
            consumer.accept(category, suppliers.row(category));
        }
    }

	public Map<ValueSupplyCategory, Map<String, Object>> getAllCategorizedExpandedSuppliers() {
		ExpandedSupplierCapturingConsumer consumer = new ExpandedSupplierCapturingConsumer();
		provideEachByCategoryTo(consumer);
		return consumer.getExpandedSuppliers();
	}

	public Map<ValueSupplyCategory, Map<String, Object>> findExpandedSuppliersOf(ValueSupplyCategory category) {
		Map<String, Supplier<Object>> rows = suppliers.row(category);
		ExpandedSupplierCapturingConsumer consumer = new ExpandedSupplierCapturingConsumer();
		consumer.accept(category, rows);
		return consumer.getExpandedSuppliers();
	}

}
