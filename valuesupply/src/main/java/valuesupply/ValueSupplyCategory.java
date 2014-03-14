package valuesupply;

public class ValueSupplyCategory {

    private String name;

    public ValueSupplyCategory(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("{name=%s}", name);
    }

}
