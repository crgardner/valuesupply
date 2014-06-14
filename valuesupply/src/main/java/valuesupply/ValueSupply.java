package valuesupply;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import static java.util.stream.Collectors.*;
import static java.util.Arrays.*;

import com.google.common.collect.*;

public class ValueSupply {

    private final Table<ValueSupplyCategory, String, Supplier<Object>> suppliers = HashBasedTable.create();
    private final SupplierMapExpander expander = new SupplierMapExpander();

    public void add(ValueSupplyCategory category, String name,
            Supplier<Object> supplier) {
        suppliers.put(category, name, supplier);
    }

    public Map<ValueSupplyCategory, Map<String, Object>> getAllCategorizedExpandedSuppliers() {
        return findExpandedSuppliersOf(allCategories());
    }

    private ValueSupplyCategory[] allCategories() {
        return suppliers.rowKeySet().toArray(new ValueSupplyCategory[0]);
    }

    public Map<ValueSupplyCategory, Map<String, Object>> findExpandedSuppliersOf(ValueSupplyCategory... categories) {
        return asList(categories).stream()
                                 .collect(toMap(category -> category,
                                                category -> expander.expand(suppliers.row(category))));
    }

    public void supplyAllOf(ValueSupplyCategory category, Consumer<Map<String, Object>> allConsumer) {
        allConsumer.accept(findExpandedSuppliersOf(category).get(category));
    }

    public void supplyEachOf(ValueSupplyCategory category, Consumer<Entry<String, Object>> eachConsumer) {
        findExpandedSuppliersOf(category).get(category).entrySet().stream().forEach(entry -> eachConsumer.accept(entry));
    }
}
