package valuesupply;

import java.util.Map;

import com.google.common.base.Supplier;

public interface ValueConsumer {

    void accept(ValueSupplyCategory category, Map<String, Supplier<String>> suppliers);

}
