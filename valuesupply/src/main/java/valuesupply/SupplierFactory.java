package valuesupply;

import java.util.function.Supplier;

public interface SupplierFactory {

    Supplier<Object> create(ValueSupplyItemDescriptor valueSupplyItemDescriptor) throws UnknownSupplierException;

}
