package mint.inference.gp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.log4j.Logger;

import edu.emory.mathcs.backport.java.util.Collections;
import mint.Configuration;
import mint.inference.evo.AbstractIterator;
import mint.inference.evo.Chromosome;
import mint.inference.evo.GPConfiguration;
import mint.inference.evo.Selection;
import mint.inference.evo.TournamentSelection;
import mint.inference.gp.fitness.latentVariable.BooleanFitness;
import mint.inference.gp.fitness.latentVariable.IntegerFitness;
import mint.inference.gp.fitness.latentVariable.LatentVariableFitness;
import mint.inference.gp.fitness.latentVariable.StringFitness;
import mint.inference.gp.selection.LatentVariableTournament;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.ListVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public class LatentVariableGP extends GP<VariableAssignment<?>> {

	private final static Logger LOGGER = Logger.getLogger(GP.class.getName());

	protected TournamentSelection selection = null;
	private Node<?> fittest = null;

	public LatentVariableGP(Generator gen, MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			GPConfiguration gpConf) {
		super(gpConf);
		this.evals = evals;
		this.gen = gen;
		distances = new HashMap<Node<?>, List<Double>>();
	}

	@Override
	public Selection getSelection(List<Chromosome> currentPop) {
		selection = new LatentVariableTournament(evals, currentPop, getGPConf().getDepth(), gen.rand);
		return selection;
	}

	@Override
	protected Datatype getType() {
		VariableAssignment<?> var = evals.values().iterator().next();
		if (var instanceof StringVariableAssignment)
			return Datatype.STRING;
		else if (var instanceof DoubleVariableAssignment)
			return Datatype.DOUBLE;
		else if (var instanceof IntegerVariableAssignment)
			return Datatype.INTEGER;
		else if (var instanceof BooleanVariableAssignment)
			return Datatype.BOOLEAN;
		else if (var instanceof ListVariableAssignment)
			return Datatype.LIST;
		else
			throw new IllegalStateException("Cannot determine the type of " + var.getClass().getName());
	}

	@Override
	protected AbstractIterator getIterator(List<Chromosome> population) {
		if (selection != null) {
			List<Chromosome> elites = selection.getElite();
			return new SteadyStateIterator(elites, population, getGPConf().getCrossOver(), getGPConf().getMutation(),
					gen, getGPConf().getDepth(), new Random(Configuration.getInstance().SEED));
		}
		return new SteadyStateIterator(new ArrayList<Chromosome>(), population, getGPConf().getCrossOver(),
				getGPConf().getMutation(), gen, getGPConf().getDepth(), new Random(Configuration.getInstance().SEED));
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isCorrect(Chromosome c) {
		try {
			if (((Node<?>) c).getReturnType() == Datatype.STRING)
				return new StringFitness(evals, (Node<VariableAssignment<String>>) c).correct();
			else if (((Node<?>) c).getReturnType() == Datatype.INTEGER)
				return new IntegerFitness(evals, (Node<VariableAssignment<Long>>) c).correct();
			else if (((Node<?>) c).getReturnType() == Datatype.BOOLEAN) {
				return new BooleanFitness(evals, (Node<VariableAssignment<Boolean>>) c).correct();
			}
			// System.out.println(c.getClass());
			throw new IllegalArgumentException(
					"Could not calculate correctness for node of type " + ((Node<?>) c).getReturnType());
		} catch (Exception e) {
			return false;
		}
	}

	public String popInfo() {
		List<Chromosome> orderedByFitness = new ArrayList<Chromosome>(population);
		Collections.sort(orderedByFitness);

		List<String> popString = new ArrayList<String>();

		for (Chromosome c : orderedByFitness) {
			popString.add(c + ": " + c.getFitness());
		}

		return popString.toString();
	}

	private LatentVariableFitness<?> getFitnessFunction(Chromosome c) {
		Node<?> node = (Node<?>) c;
		if (node.getReturnType() == Datatype.STRING)
			return new StringFitness(evals, (Node<VariableAssignment<String>>) c);
		else if (node.getReturnType() == Datatype.INTEGER)
			return new IntegerFitness(evals, (Node<VariableAssignment<Long>>) c);
		else {
			assert (node.getReturnType() == Datatype.BOOLEAN);
			return new BooleanFitness(evals, (Node<VariableAssignment<Boolean>>) c);
		}
	}

	private Node<?> chooseBest(List<Chromosome> pop) {
		Node<?> best;
		if (fittest == null)
			best = (Node<?>) pop.get(0);
		else
			best = fittest;
		for (Chromosome c : pop) {
			Node<?> node = (Node<?>) c;
			LatentVariableFitness<?> fit = getFitnessFunction(c);

			if (node.getFitness() < best.getFitness())
				best = node;
			else if (node.getFitness().equals(best.getFitness()) && node != best) {
				LatentVariableFitness<?> bestFit = getFitnessFunction(best);
				List<Double> newTieBreak = fit.breakTies();
				List<Double> bestTieBreak = bestFit.breakTies();

				for (int i = 0; i < Math.min(newTieBreak.size(), bestTieBreak.size()); i++) {
					if (newTieBreak.get(i) > bestTieBreak.get(i)) {
						break;
					}
					if (newTieBreak.get(i) < bestTieBreak.get(i)) {
						best = (Node<?>) c;
						break;
					}
				}
			}
		}
//		// System.out.println(" Challenger: " + best + " Challenger Fitness: " + best.getFitness());
		return best;
	}

	@Override
	public void evaluatePopulation(List<Chromosome> pop) {
		for (Chromosome c : pop) {
			Node<?> node = (Node<?>) c;
			if (node.getFitness() == null) {
				try {
					LatentVariableFitness<?> fit = getFitnessFunction(c);
					double fitness = fit.call();
					node.setFitness(fitness);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}

	@Override
	public Chromosome evolve(int lim) {
		assert (lim > 0);
		// System.out.println("Generating population");
		population = generatePopulation(getGPConf().getPopulationSize() - seeds.size());

		population.addAll(seeds);

		evaluatePopulation(population);

		fittest = chooseBest(population);

		LOGGER.debug("GP iteration: 0" + " - best individual: " + fittest + " fitness: " + fittest.getFitness()
				+ " New population: " + popInfo());

		AbstractIterator it = getIterator(population);
		for (int i = 1; i <= lim; i++) {
//			List<Chromosome> optimal = population.stream().filter(x -> x.getFitness() == 0).collect(Collectors.toList());
//			System.out.println(popInfo());

			if (fittest.getFitness() <= 0D)
				return fittest;

			population = it.iterate(this);

			fittest = chooseBest(population);

			LOGGER.debug("GP iteration: " + i + " - best individual: " + fittest + " fitness: " + fittest.getFitness()
					+ " New population: " + popInfo());

			it = getIterator(population);
		}

		return fittest;
	}

}
