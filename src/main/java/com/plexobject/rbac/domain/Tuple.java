package com.plexobject.rbac.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class is used to represent Tuple of values. This class is immutable and
 * non-thread safe
 * 
 * 
 */
public class Tuple {
	final Object[] objects;

	public Tuple(Object... objects) {
		if (null == objects) {
			throw new NullPointerException("no objects");
		}

		this.objects = objects;
	}

	public int size() {
		return objects.length;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(int i) {
		return (T) objects[i];
	}

	@SuppressWarnings("unchecked")
	public <T> T first() {
		return (T) get(0);
	}

	@SuppressWarnings("unchecked")
	public <T> T second() {
		return (T) get(1);
	}

	@SuppressWarnings("unchecked")
	public <T> T third() {
		return (T) get(2);
	}

	@SuppressWarnings("unchecked")
	public <T> T last() {
		return (T) get(objects.length - 1);
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Tuple)) {
			return false;
		}
		Tuple rhs = (Tuple) object;
		if (objects.length != rhs.objects.length) {
			return false;
		}
		EqualsBuilder eqBuilder = new EqualsBuilder();
		for (int i = 0; i < objects.length; i++) {
			eqBuilder.append(objects[i], rhs.objects[i]);
		}
		return eqBuilder.isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder hashBuilder = new HashCodeBuilder(786529047, 1924536713);
		for (int i = 0; i < objects.length; i++) {
			hashBuilder.append(objects[i]);
		}
		return hashBuilder.toHashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder strBuilder = new ToStringBuilder(this);
		for (int i = 0; i < objects.length; i++) {
			strBuilder.append(objects[i]);
		}
		return strBuilder.toString();
	}
}
