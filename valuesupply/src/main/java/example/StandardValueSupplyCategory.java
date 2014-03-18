package example;

import valuesupply.ValueSupplyCategory;

public enum StandardValueSupplyCategory implements ValueSupplyCategory {
    URL_COMPONENT,
    HTTP_HEADER, MEDIA_TYPE;

    @Override
    public String getName() {
        return name();
    }
}
