package mint.inference.gp.fitness.latentVariable;

import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.gp.fitness.InvalidDistanceException;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public class IntegerFitness extends LatentVariableFitness<Long> {

	public IntegerFitness(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<Long>> individual) {
		super(evals, individual);
	}

	@Override
	protected double distance(Long actual, Object expected) throws InvalidDistanceException {
		if (expected instanceof Long) {
			Long exp = (Long) expected;
			return Math.abs(actual.longValue() - exp.longValue());
		} else if (expected instanceof Double) {
			Double exp = (Double) expected;
			return Math.abs(actual.doubleValue() - exp.doubleValue());
		} else if (expected instanceof Integer) {
			Integer exp = (Integer) expected;
			return Math.abs(actual.intValue() - exp.intValue());
		} else {
			return Double.POSITIVE_INFINITY;
		}
	}
}
