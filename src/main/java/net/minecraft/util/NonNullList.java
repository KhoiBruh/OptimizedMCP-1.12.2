package net.minecraft.util;

import java.util.*;

public class NonNullList<E> extends AbstractList<E> {

	private final List<E> delegate;
	private final E defaultElement;

	protected NonNullList() {
		this(new ArrayList<>(), null);
	}

	protected NonNullList(List<E> delegateIn, E listType) {
		delegate = delegateIn;
		defaultElement = listType;
	}

	public static <E> NonNullList<E> create() {

		return new NonNullList<>();
	}

	public static <E> NonNullList<E> withSize(int size, E fill) {
		
		Objects.requireNonNull(fill);
		Object[] aobject = new Object[size];
		Arrays.fill(aobject, fill);
		return new NonNullList<>(Arrays.asList((E[]) aobject), fill);
	}

	@SafeVarargs
	public static <E> NonNullList<E> from(E defaultElementIn, E... elements) {

		return new NonNullList<>(Arrays.asList(elements), defaultElementIn);
	}
	public E get(int index) {
		return delegate.get(index);
	}

	public E set(int index, E element) {
		Objects.requireNonNull(element);
		return delegate.set(index, element);
	}

	public void add(int index, E element) {
		Objects.requireNonNull(element);
		delegate.add(index, element);
	}

	public E remove(int index) {
		return delegate.remove(index);
	}

	public int size() {
		return delegate.size();
	}

	public void clear() {
		if (defaultElement == null) {
			super.clear();
		} else {
			Collections.fill(this, defaultElement);
		}
	}

}
