package valuesupply;

import com.google.common.base.*;

public interface SupplierFactory {

    Optional<Supplier<Object>> createFrom(ValueSupplyItemDescriptor valueSupplyItemDescriptor);

}
