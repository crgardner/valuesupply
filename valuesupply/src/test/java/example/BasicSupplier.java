package example;

import java.util.function.Supplier;

public class BasicSupplier<T> implements Supplier<T> {
	
	private T value;

	public BasicSupplier(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

}
