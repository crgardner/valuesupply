package valuesupply;

import java.util.Map;

import com.google.common.base.*;
import com.google.common.collect.Maps;

public class SupplierMapExpander {

    private final static Function<Supplier<Object>, Object> expandValueFunction = new Function<Supplier<Object>, Object>() {
        @Override public Object apply(Supplier<Object> input) {
            return input.get();
        }
    };

    public Map<String, Object> expand(Map<String, Supplier<Object>> suppliers) {
        return Maps.transformValues(suppliers, expandValueFunction);
    }
}
