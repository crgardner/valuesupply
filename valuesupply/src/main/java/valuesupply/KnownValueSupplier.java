package valuesupply;

import java.util.function.Supplier;


public class KnownValueSupplier implements Supplier<Object> {
    private Object value;

    public KnownValueSupplier(Object value) {
        this.value = value;
    }

    @Override public Object get() {
        return value;
    }
}