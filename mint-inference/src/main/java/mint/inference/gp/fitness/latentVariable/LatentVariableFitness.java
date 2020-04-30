package mint.inference.gp.fitness.latentVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.gp.CallableNodeExecutor;
import mint.inference.gp.fitness.Fitness;
import mint.inference.gp.fitness.InvalidDistanceException;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public abstract class LatentVariableFitness<T> extends Fitness {

	final MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evalSet;

	protected Node<VariableAssignment<T>> individual;
	protected boolean needHidden;

	public LatentVariableFitness(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<T>> individual) {
		this.evalSet = evals;
		this.individual = individual;
	}

	public Node<?> getIndividual() {
		return individual;
	}

	private double getOffBy(VariableTerminal<?> var, T value, List<VariableAssignment<?>> ctx,
			Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current)
			throws InterruptedException, InvalidDistanceException {
		var.setValue(value);
		List<VariableAssignment<?>> ctx1 = new ArrayList<VariableAssignment<?>>(ctx);
		ctx1.add(var.getTerminal().copy());
		T actual = new CallableNodeExecutor<>(individual.copy(), ctx1).call();
		return distance(actual, current.getValue().getValue());
	}

	private double calculateDistance(Entry<List<VariableAssignment<?>>, VariableAssignment<?>> target,
			Set<VariableTerminal<?>> latent) throws InterruptedException {
		individual.reset();

		List<VariableAssignment<?>> ctx = makeCtx(target);
		CallableNodeExecutor<T> executor = new CallableNodeExecutor<>(individual, ctx);
		double minDistance = Double.POSITIVE_INFINITY;

		try {
			if (latent.isEmpty()) {
				minDistance = distance(executor.call(), target.getValue().getValue());
				individual.reset();
			}

			for (VariableTerminal<?> var : latent) {
				for (Object value : var.getTerminal().getValues()) {
					double offBy = getOffBy(var, (T) value, ctx, target);
					if (offBy < minDistance)
						minDistance = offBy;
				}
			}
		} catch (ClassCastException e) {
			return Double.POSITIVE_INFINITY;
		} catch (InvalidDistanceException e) {
			return Double.POSITIVE_INFINITY;
		} catch (NullPointerException e) {
			return Double.POSITIVE_INFINITY;
		} catch (java.lang.IndexOutOfBoundsException e) {
			System.out.println("OOB for " + individual);
			System.exit(1);
			return Double.POSITIVE_INFINITY;
		}
		return minDistance;
	}

	@Override
	public Double call() throws InterruptedException {
		if (individual == null) {
			return Double.POSITIVE_INFINITY;
		}

		double mistakes = 0D;
		List<Double> distances = new ArrayList<Double>();

		Set<VariableTerminal<?>> latent = individual.latentVars();

		Set<String> totalVars = totalUsedVars();
		totalVars.removeAll(individual.varsInTree().stream().map(s -> s.getName()).collect(Collectors.toSet()));

		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entries()) {
			double minDistance = calculateDistance(current, latent);
			distances.add(minDistance);
			if (minDistance > 0D) {
				mistakes++;
			}
		}

		double fitness = mistakes + rmsd(distances);

		// If we've used all of the available inputs then don't penalise use of latent
		// variables
		if (totalVars.isEmpty())
			return fitness;

		// If we've not used all of the available inputs, penalise by the number of
		// latent variables used
		return fitness + latent.size();
	}

	private Set<String> totalUsedVars() {
		Set<String> totalUsedVars = new HashSet<String>();
		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entries()) {
			for (VariableAssignment<?> vName : current.getKey()) {
				totalUsedVars.add(vName.getName());
			}
		}
		return totalUsedVars;
	}

	public List<VariableAssignment<?>> makeCtx(Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current) {
		List<VariableAssignment<?>> ctx = new ArrayList<VariableAssignment<?>>();
		for (VariableAssignment<?> v : current.getKey()) {
			ctx.add(v);
		}
		return ctx;
	}

	protected abstract double distance(T actual, Object expected) throws InvalidDistanceException;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof LatentVariableFitness))
			return false;

		LatentVariableFitness<?> singleOutputFitness = (LatentVariableFitness<?>) o;

		if (!individual.equals(singleOutputFitness.individual))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return individual.hashCode();
	}

	public boolean correct() throws InterruptedException {
		Set<VariableTerminal<?>> undef = individual.latentVars();

		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entries()) {
			double minDistance = calculateDistance(current, undef);
			if (minDistance > 0D) {
				return false;
			}
		}

		return true;
	}

	// We want to break ties first by expressions which use all the input variables
	// and then by size

	@Override
	public List<Double> breakTies() {
		Set<String> totalVars = totalUsedVars();
		totalVars.removeAll(individual.varsInTree().stream().map(s -> s.getName()).collect(Collectors.toSet()));

		return Arrays.asList((double) totalVars.size(), (double) individual.size());
	}
}