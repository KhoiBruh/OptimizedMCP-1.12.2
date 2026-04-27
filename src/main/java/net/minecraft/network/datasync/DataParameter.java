package net.minecraft.network.datasync;

public record DataParameter<T>(int id, DataSerializer<T> serializer) {

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (p_equals_1_ != null && getClass() == p_equals_1_.getClass()) {
			DataParameter<?> dataparameter = (DataParameter) p_equals_1_;
			return id == dataparameter.id;
		} else {
			return false;
		}
	}

	public int hashCode() {

		return id;
	}

}
