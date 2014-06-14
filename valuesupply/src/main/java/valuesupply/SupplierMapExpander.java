package valuesupply;

import java.util.*;
import java.util.function.Supplier;

public class SupplierMapExpander {
    public Map<String, Object> expand(Map<String, Supplier<Object>> suppliers) {
        Map<String, Object> expanded = new HashMap<>();
        
        suppliers.forEach((name, supplier) -> {
            expanded.put(name, supplier.get());
        });
        
        return expanded;
                
    }
}
