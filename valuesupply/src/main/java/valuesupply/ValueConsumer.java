package valuesupply;

import java.util.Map;

import com.google.common.base.Supplier;

public interface ValueConsumer {

    void provide(Supplier<String> supplier);

    void provide(ValueSupplyCategory category, Map<String, Supplier<String>> suppliers);

}
