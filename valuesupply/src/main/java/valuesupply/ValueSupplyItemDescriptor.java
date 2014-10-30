package valuesupply;

import java.util.Objects;

public class ValueSupplyItemDescriptor {

    private String name;
    private ValueType valueType;
    private String stringValue;
    private ValueSupplyCategory valueSupplyCategory;

    public ValueSupplyItemDescriptor(ValueSupplyCategory valueSupplyCategory, String name,
            ValueType valueType) {
        this(valueSupplyCategory, name, valueType, null);
    }

    public ValueSupplyItemDescriptor(ValueSupplyCategory valueSupplyCategory, String name,
            ValueType valueType, String value) {
        this.name = name;
        this.valueType = valueType;
        this.stringValue = value;
        this.valueSupplyCategory = valueSupplyCategory;
    }

    public String name() {
        return name;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public String getStringValue() {
        return stringValue;
    }

    public ValueSupplyCategory category() {
        return valueSupplyCategory;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ValueSupplyItemDescriptor other = (ValueSupplyItemDescriptor) obj;

        return Objects.equals(valueSupplyCategory, other.valueSupplyCategory)
                && Objects.equals(name, other.name) && Objects.equals(valueType, other.valueType)
                && Objects.equals(stringValue, other.stringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, valueType, stringValue);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                                             .add("valueSupplyCategory", valueSupplyCategory)
                                             .add("name", name)
                                             .add("valueType", valueType)
                                             .add("stringValue", stringValue)
                                             .toString();
    }

    public boolean isResolutionRequired() {
        return valueType.requiresImmediateValue() && stringValue == null;
    }
}
