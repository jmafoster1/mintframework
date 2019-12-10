package mint.tracedata.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class IntegerVariableAssignment extends NumberVariableAssignment<Integer> {
	// Cannot use min and Integer.MAX_VAlUE because calculating their difference
	// causes an arithmetic overflow
	private final static int min = -1000;
	private final static int max = 1000;

	private static List<Integer> values = new ArrayList<Integer>();

	private final static Logger LOGGER = Logger.getLogger(IntegerVariableAssignment.class.getName());

	protected static Map<String, Integer> constMap = new HashMap<String, Integer>();

	private static int getMinVal(int given) {
		if (constMap.isEmpty())
			return given;
		else
			return 0;
	}

	private static int getMaxVal(int given) {
		if (constMap.isEmpty())
			return given;
		else
			return constMap.size();
	}

	public static Map<String, Integer> getConstMap() {
		return constMap;
	}

	public IntegerVariableAssignment(String name, Integer value) {
		super(name, value, getMinVal(min), getMaxVal(max));
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Integer value, boolean add) {
		super(name, value, getMinVal(min), getMaxVal(max));
		if (add)
			addValue(value);
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Integer value, Integer min, Integer max) {
		super(name, value, getMinVal(min), getMaxVal(max));
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Integer min, Integer max) {
		super(name, min, max);
	}

	public IntegerVariableAssignment(String name) {
		super(name, getMinVal(min), getMaxVal(max));
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Collection<Integer> from) {
		super(name, min, max, from);
	}

	@Override
	public void setStringValue(String s) {
		if (s.trim().equals("nonsensical") || s.trim().equals("null") || s.trim().isEmpty()) {
			this.setNull(true);
			return;
		} else {
			try {
				Double doubVal = Double.valueOf(s);
				setToValue(doubVal.intValue());
			} catch (NumberFormatException nfe) {
				LOGGER.warn("Variable " + name + " string " + s + " is not an integer. Setting to const.");
				if (constMap.containsKey(s))
					setToValue(constMap.get(s));
				else {
					constMap.put(s, constMap.size());
					setToValue(constMap.get(s));
				}
			}
		}

	}

	@Override
	public String printableStringOfValue() {
		if (value == null)
			return "NA";
		else
			return Integer.toString(value);
	}

	@Override
	public String typeString() {
		return ":I";
	}

	@Override
	public VariableAssignment<?> createNew(String name, String value) {
		IntegerVariableAssignment iva = new IntegerVariableAssignment(name);
		iva.setParameter(isParameter());
		if (value == null)
			setNull(true);
		else if (value.trim().equals("*"))
			setNull(true);
		else if (value.trim().equals("E")) // special error value...
			iva.setValue(min);
		else
			iva.setStringValue(value);
		iva.setMax(max);
		iva.setMin(min);
		// assert max>0;
		return iva;
	}

	@Override
	public VariableAssignment<Integer> copy() {
		IntegerVariableAssignment copied = new IntegerVariableAssignment(name, value, min, max);
		copied.setParameter(isParameter());
		if (isRestricted()) {
			copied.setRange(from);
		}
		if (!isNull()) {
			copied.setValue(value);
		}
		copied.setNull(isNull());
		return copied;

	}

	@Override
	protected Integer generateRandom() {
		if (!values.isEmpty())
			return values.get(rand.nextInt(values.size()));
		System.out.println("max:" + max + " min: " + min);
		Integer retVal = min + rand.nextInt((max - min));
		return retVal;
	}

	@Override
	public boolean withinLimits() {
		if (getValue() > max || getValue() < min)
			return false;
		return super.withinLimits();
	}

	@Override
	protected void setToValue(Integer value) {
		super.setToValue(value);
		if (enforcing) {
			if (value > max)
				this.value = max;
			else if (value < min)
				this.value = min;
		}
	}

	@Override
	public List<Integer> getValues() {
		return values;
	}

	@Override
	public void addValue(Integer v) {
		if (!values.contains(v))
			values.add(v);
	}

	public static List<Integer> values() {
		return values;
	}

	@Override
	public void fuzz() {
		if (VariableAssignment.rand.nextDouble() > 0.5)
			this.value++;
		else
			this.value--;
	}

}
