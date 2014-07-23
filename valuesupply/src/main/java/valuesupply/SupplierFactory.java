package valuesupply;

import com.google.common.base.Supplier;

public interface SupplierFactory {

    Supplier<Object> create(ValueSupplyItemDescriptor valueSupplyItemDescriptor) throws UnknownSupplierException;

}
