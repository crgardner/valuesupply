package valuesupply;

import static org.assertj.core.api.Assertions.assertThat;
import static valuesupply.StandardValueSupplyCategory.HTTP_HEADER;

import org.junit.Test;

public class ValueSupplyItemDescriptorTest {
    private ValueSupplyItemDescriptor descriptor;
    private String item = "item";


    @Test
    public void isCreatableWithNameAndTypeOnly() {
        descriptor = new ValueSupplyItemDescriptor(HTTP_HEADER, item, StandardValueType.LocalDate, null);

        assertThat(descriptor).isEqualTo(new ValueSupplyItemDescriptor(HTTP_HEADER, item, StandardValueType.LocalDate, null));
        assertThat(descriptor.category()).isEqualTo(HTTP_HEADER);
        assertThat(descriptor.name()).isEqualTo(item);
        assertThat(descriptor.stringValue()).isNull();
        assertThat(descriptor.valueType()).isEqualTo(StandardValueType.LocalDate);
    }

    @Test
    public void isCreatableWithNameTypeAndConstantValue() throws Exception {
        descriptor = new ValueSupplyItemDescriptor(HTTP_HEADER, item, StandardValueType.String, "ABC");

        assertThat(descriptor).isEqualTo(new ValueSupplyItemDescriptor(HTTP_HEADER, item, StandardValueType.String, "ABC"));
        assertThat(descriptor.category()).isEqualTo(HTTP_HEADER);
        assertThat(descriptor.name()).isEqualTo(item);
        assertThat(descriptor.stringValue()).isEqualTo("ABC");
        assertThat(descriptor.valueType()).isEqualTo(StandardValueType.String);
    }

    @Test
    public void answersResolutionIsRequired() {
        descriptor = new ValueSupplyItemDescriptor(HTTP_HEADER, item, StandardValueType.String);

        assertThat(descriptor.isResolutionRequired()).isTrue();
    }

    @Test
    public void answersResolutionIsNotRequired() {
        descriptor = new ValueSupplyItemDescriptor(HTTP_HEADER, item, StandardValueType.String, "ABC");

        assertThat(descriptor.isResolutionRequired()).isFalse();
    }

}
