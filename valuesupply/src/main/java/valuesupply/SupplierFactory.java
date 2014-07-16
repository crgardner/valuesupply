package valuesupply;

import com.google.common.base.Supplier;

public interface SupplierFactory {

    Supplier<Object> create(String type) throws UnknownSupplierException;

}
