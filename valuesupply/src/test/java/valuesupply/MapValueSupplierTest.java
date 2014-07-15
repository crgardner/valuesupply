package valuesupply;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

public class MapValueSupplierTest {

    private Map<String, Object> keyValuePairs;
    private Supplier<Object> supplier;
    private Object value;

    @Test
    public void suppliesValueBasedOnInherentKey() {
        keyValuePairs = ImmutableMap.<String, Object>of("keyOne", "valueOne",
                                                        "keyTwo", "valueTwo",
                                                        "keyThree", "valueThree");

        supplier = new MapValueSupplier("keyTwo", keyValuePairs);
        value = supplier.get();

        assertThat(value).isEqualTo("valueTwo");
    }

}
