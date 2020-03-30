package mint.tracedata.types;

/**
 *
 * A boolean variable that is, for Machine Learning reasons, stored as a
 * 2-valued integer.
 *
 * Created by neilwalkinshaw on 01/09/2014.
 */
public class IntegerBooleanVariableAssignment extends IntegerVariableAssignment {

	public IntegerBooleanVariableAssignment(String name, Boolean value) {
		super(name);
		if (value)
			setToValue(1l);
		else
			setToValue(0l);
		setMax(1l);
		setMin(0l);
	}

	public IntegerBooleanVariableAssignment(String name, long value) {
		super(name);
		assert (value >= 0 && value <= 1);
		this.value = value;
		setMax(1l);
		setMin(0l);
	}

	public IntegerBooleanVariableAssignment(String name) {
		super(name);
		value = 0l;
		setMax(1l);
		setMin(0l);
	}

	@Override
	public void setStringValue(String s) {
		if (s.equals("true"))
			setToValue(1l);
		else
			setToValue(0l);
	}

	@Override
	public VariableAssignment<Long> copy() {

		IntegerBooleanVariableAssignment copied = new IntegerBooleanVariableAssignment(name, value);
		copied.setParameter(isParameter());
		return copied;
	}

	@Override
	public VariableAssignment<?> createNew(String name, String value) {
		IntegerBooleanVariableAssignment iva = new IntegerBooleanVariableAssignment(name);
		iva.setParameter(isParameter());
		if (value == null)
			setNull(true);
		else if (value.trim().equals("*"))
			setNull(true);
		else
			iva.setStringValue(value);
		iva.setMax(1l);
		iva.setMin(0l);
		return iva;
	}

}
