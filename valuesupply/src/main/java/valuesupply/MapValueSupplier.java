package valuesupply;

import java.util.Map;

import com.google.common.base.Supplier;

public class MapValueSupplier implements Supplier<Object> {

    private Map<String, Object> keyValuePairs;
    private String key;

    public MapValueSupplier(String key, Map<String, Object> keyValuePairs) {
        this.key = key;
        this.keyValuePairs = keyValuePairs;
    }

    @Override
    public Object get() {
        return keyValuePairs.get(key);
    }

}
