package valuesupply;

import com.google.common.base.Supplier;

public class ValueSupplyItem {

    private final String name;
    private final Supplier<Object> supplier;

    public ValueSupplyItem(String name, Supplier<Object> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return supplier.get();
    }

}
