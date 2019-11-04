package drofff.merger;

public abstract class Mergeable implements Cloneable {

	@Override
	public Object clone() throws CloneNotSupportedException {
		History.save(this);
		return super.clone();
	}

	public abstract String getId();
}
