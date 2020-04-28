package mint.tracedata.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringVariableAssignment extends VariableAssignment<String> {

	private static List<String> values = new ArrayList<String>();

	public StringVariableAssignment(String name, String value) {
		super(name, value);
	}

	public StringVariableAssignment(String name, String value, boolean add) {
		super(name, value);
		if (add)
			addValue(value);
	}

	public StringVariableAssignment(String name) {
		super(name);
	}

	public StringVariableAssignment(String name, Collection<String> from) {
		super(name, from);
	}

	@Override
	public void setStringValue(String s) {
		setNull(false);
		setToValue(String.valueOf(s));
	}

	@Override
	public String printableStringOfValue() {
		return value;
	}

	@Override
	public String typeString() {
		return ":S";
	}

	@Override
	public VariableAssignment<?> createNew(String name, String value) {
		StringVariableAssignment sva = new StringVariableAssignment(name);
		sva.setParameter(isParameter());
		if (value == null)
			setNull(true);
		else if (value.trim().equals("*"))
			setNull(true);
		else
			sva.setStringValue(value);
		return sva;
	}

	@Override
	public VariableAssignment<String> copy() {
		StringVariableAssignment copied = new StringVariableAssignment(name, value);
		copied.setParameter(isParameter());
		copied.setRange(from);
		assert (copied.isNull() == isNull());
		return copied;
	}

	@Override
	protected String generateRandom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getValues() {
		return values;
	}

	@Override
	public void addValue(String v) {
		if (!values.contains(v))
			values.add(v);
	}

	@Override
	public String toString() {
		if (value == null)
			return name;
		return name + "=" + value;
	}

	@Override
	// Either chop the first letter off or add a letter onto the end or replace with
	// a new value from the set of values
	public void fuzz() {
		switch (VariableAssignment.rand.nextInt(3)) {
		case 0:
			if (this.value.length() > 0)
				this.value = this.value.substring(1);
			else
				this.value += (char) (VariableAssignment.rand.nextInt(26) + 'a');
			break;
		case 1:
			if (values.isEmpty())
				break;
			this.value += (char) (VariableAssignment.rand.nextInt(26) + 'a');
			break;
		case 2:
			if (values.isEmpty())
				break;
			this.value = values.get(VariableAssignment.rand.nextInt(values.size()));
			break;
		}
	}

	public static void clearValues() {
		values.clear();
	}
}
