package valuesupply;

public enum StandardValueType implements ValueType {
    LocalDate,
    String(true)
    ;

    private final boolean requiresImmediateValue;

    private StandardValueType() {
        this(false);
    }

    private StandardValueType(boolean requiresImmediateValue) {
        this.requiresImmediateValue = requiresImmediateValue;
    }

    @Override
    public boolean requiresImmediateValue() {
        return requiresImmediateValue;
    }
}
