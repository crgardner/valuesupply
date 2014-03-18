package valuesupply;

public class BasicValueSupplyCategory implements ValueSupplyCategory {

    private String name;

    public BasicValueSupplyCategory(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("{name=%s}", name);
    }

}
