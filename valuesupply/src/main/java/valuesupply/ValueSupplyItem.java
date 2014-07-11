package valuesupply;

import com.google.common.base.Supplier;

public class ValueSupplyItem {

    private final String name;
    private final Supplier<Object> supplier;

    public ValueSupplyItem(String name, Supplier<Object> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String name() {
        return name;
    }

    public Object value() {
        return supplier.get();
    }

    public String valueAsString() {
        return value().toString();
    }

}
