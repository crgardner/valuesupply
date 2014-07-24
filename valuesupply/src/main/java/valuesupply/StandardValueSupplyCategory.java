package valuesupply;


public enum StandardValueSupplyCategory implements ValueSupplyCategory {
    URL_COMPONENT,
    HTTP_HEADER;

    @Override
    public String getName() {
        return name();
    }
}
