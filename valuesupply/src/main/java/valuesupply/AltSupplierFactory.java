package valuesupply;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public interface AltSupplierFactory {
    Optional<Supplier<Object>> create(String name, ValueType valueType, String potentialDefaultValue);
}
