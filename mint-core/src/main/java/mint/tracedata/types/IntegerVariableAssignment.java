package mint.tracedata.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class IntegerVariableAssignment extends NumberVariableAssignment<Long> {
	// Cannot use min and Integer.MAX_VAlUE because calculating their difference
	// causes an arithmetic overflow
	private final static long min = -1000;
	private final static long max = 1000;

	private static List<Long> values = new ArrayList<Long>();

	private final static Logger LOGGER = Logger.getLogger(IntegerVariableAssignment.class.getName());

	protected static Map<String, Long> constMap = new HashMap<String, Long>();

	private static long getMinVal(long given) {
		if (constMap.isEmpty())
			return given;
		else
			return 0;
	}

	private static long getMaxVal(long given) {
		if (constMap.isEmpty())
			return given;
		else
			return constMap.size();
	}

	public static Map<String, Long> getConstMap() {
		return constMap;
	}

	public IntegerVariableAssignment(String name, Long value) {
		super(name, value, getMinVal(min), getMaxVal(max));
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Long value, boolean add) {
		super(name, value, getMinVal(min), getMaxVal(max));
		if (add)
			addValue(value);
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Long value, Long min, Long max) {
		super(name, value, getMinVal(min), getMaxVal(max));
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Long min, Long max) {
		super(name, min, max);
	}

	public IntegerVariableAssignment(String name, Integer lowerLimit, Integer upperLimit) {
		super(name, min, max);
	}

	public IntegerVariableAssignment(String name) {
		super(name, getMinVal(min), getMaxVal(max));
		assert max > 0;
	}

	public IntegerVariableAssignment(String name, Collection<Long> from) {
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
				setToValue(doubVal.longValue());
			} catch (NumberFormatException nfe) {
				LOGGER.warn("Variable " + name + " string " + s + " is not an integer. Setting to const.");
				if (constMap.containsKey(s))
					setToValue(constMap.get(s));
				else {
					constMap.put(s, (long) constMap.size());
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
			return Long.toString(value);
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
	public VariableAssignment<Long> copy() {
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
	protected Long generateRandom() {
		System.out.println("generateRandom");
		if (!values.isEmpty())
			return values.get(rand.nextInt(values.size()));
		System.out.println("max:" + max + " min: " + min);
		Long retVal = min + rand.nextInt((int) (max - min));
		return retVal;
	}

	@Override
	public boolean withinLimits() {
		if (getValue() > max || getValue() < min)
			return false;
		return super.withinLimits();
	}

	@Override
	protected void setToValue(Long value) {
		super.setToValue(value);
		if (enforcing) {
			if (value > max)
				this.value = max;
			else if (value < min)
				this.value = min;
		}
	}

	@Override
	public List<Long> getValues() {
		return values;
	}

	@Override
	public void addValue(Long v) {
		if (!values.contains(v))
			values.add(v);
	}

	public static List<Long> values() {
		return values;
	}

	@Override
	public void fuzz() {
		switch (VariableAssignment.rand.nextInt(3)) {
		case 0:
			this.value++;
			break;
		case 1:
			this.value--;
			break;
		case 2:
			this.value = values.get(VariableAssignment.rand.nextInt(values.size()));
			break;
		}
	}

}
