package valuesupply;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;


public class StandardValueTypeTest {

    @Test
    public void answersIfImmediateValueIsRequired() throws Exception {
        assertThat(StandardValueType.String.requiresImmediateValue()).isTrue();
        assertThat(StandardValueType.LocalDate.requiresImmediateValue()).isFalse();
    }
}
