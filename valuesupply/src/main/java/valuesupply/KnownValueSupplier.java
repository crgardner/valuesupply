package valuesupply;

import com.google.common.base.Supplier;

public class KnownValueSupplier implements Supplier<Object> {
    private Object value;

    public KnownValueSupplier(Object value) {
        this.value = value;
    }

    @Override public Object get() {
        return value;
    }
}