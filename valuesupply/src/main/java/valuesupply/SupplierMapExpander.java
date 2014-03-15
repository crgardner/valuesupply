package valuesupply;

import java.util.Map;

import com.google.common.base.*;
import com.google.common.collect.Maps;

public class SupplierMapExpander {

    private final static Function<Supplier<String>, String> expandValueFunction = new Function<Supplier<String>, String>() {
        @Override public String apply(Supplier<String> input) {
            return input.get();
        }
    };

    public Map<String, String> transform(Map<String, Supplier<String>> suppliers) {
        return Maps.transformValues(suppliers, expandValueFunction);
    }
}
