package valuesupply;

import com.google.common.base.Supplier;

public class KnownValueSupplier implements Supplier<String> {
    private String value;
    
    public KnownValueSupplier(String value) {
        this.value = value;
    }
    
    @Override public String get() {
        return value;
    }
}