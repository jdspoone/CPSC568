package casa.interfaces;

public interface Transformation extends Comparable<Transformation>{

	public abstract Describable transform(Describable e);

	public abstract Describable revTransform(Describable e);

	public abstract String transform(String e);

	public abstract String revTransform(String e);

	public abstract String from();

	public abstract String to();

	public boolean isApplicable(Describable e);

}