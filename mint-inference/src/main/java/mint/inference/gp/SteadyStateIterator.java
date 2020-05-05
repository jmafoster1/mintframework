package mint.inference.gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.emory.mathcs.backport.java.util.Collections;
import mint.inference.evo.AbstractEvo;
import mint.inference.evo.AbstractIterator;
import mint.inference.evo.Chromosome;
import mint.inference.gp.tree.Node;

/**
 * Responsible for creating the offspring in an iteration by applying mutation
 * and crossover. Also retains a small number (3) elite offspring from the
 * previous generation.
 *
 * Created by neilwalkinshaw on 06/03/15.
 */
public class SteadyStateIterator extends AbstractIterator {
	protected Generator gen;
	protected int maxDepth;

	private final static Logger LOGGER = Logger.getLogger(SteadyStateIterator.class.getName());

	public SteadyStateIterator(List<Chromosome> elite, List<Chromosome> population, int mu, double mutation,
			Generator g, int maxD, Random r) {
		super(elite, population, mu, mutation, r);
		this.gen = g;
		this.maxDepth = maxD;
	}

	@Override
	protected Chromosome mutate(Chromosome root) {
		Node<?> toMutate = (Node<?>) root;

		return toMutate.mutate(gen, rand.nextInt(maxDepth - toMutate.depth())).simp();
	}

	@Override
	protected Chromosome crossOver(Chromosome parentA, Chromosome parentB) {
		Node<?> crossOverA = null;
		Node<?> crossOverB = null;
		Node<?> aCopy = (Node<?>) parentA.copy();
		Node<?> bCopy = (Node<?>) parentB.copy();
		try {
			crossOverA = selectCrossOverPoint(aCopy, null);
			crossOverB = selectCrossOverPoint(bCopy, crossOverA);
			if (crossOverB == null || crossOverA == null) {
				// LOGGER.debug("null crossover for children of "+aCopy);
				return (parentA);
			}
			// LOGGER.debug("crossed with "+aCopy);
			aCopy = aCopy.swap(crossOverA, crossOverB);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			LOGGER.debug(crossOverA + ", " + crossOverB);
		}
		return aCopy.simp();
	}

	/**
	 * Select a crossover point in tree that will be transplanted into target.
	 * Although both are Node types, tree is treated as a whole subtree from that
	 * node, whereas target is a specific node that is the target of a cross-over.
	 * 
	 * @param tree
	 * @param target
	 * @return
	 */
	protected Node<?> selectCrossOverPoint(Node<?> tree, Node<?> target) {
		List<Node<?>> nt = tree.getAllNodesAsList().stream().filter(x -> x.getReturnType() == tree.getReturnType())
				.collect(Collectors.toList());
		nt.remove(tree);
//		int depth = maxDepth;
//		if (target != null)
//			depth = maxDepth - target.depth();
		Node<?> picked = null;
		double which = rand.nextDouble();
		if (nt.isEmpty())
			return null;
		picked = pickRandomBiasEarly(nt, which);
		return picked;
	}

	/**
	 * Pick random element from a list, but prefer earlier elements (these
	 * correspond to elements higher up the tree).
	 * 
	 * @param coll
	 * @param which
	 * @return
	 */
	protected Node<?> pickRandomBiasEarly(List<Node<?>> coll, double which) {
		double collSize = coll.size();
		double[] probs = calculateProbs(collSize);
		double sum = 0;
		Node<?> retVal = null;
		for (int i = 0; i < coll.size(); i++) {
			double current = probs[i];
			sum += current;
			if (which <= sum) {
				retVal = coll.get(i);
				break;
			}
		}
		return retVal;
	}

	/**
	 * Calculate probabilities to bias towards earlier elements.
	 *
	 * This should favour the selection of nodes that are higher up in the tree
	 * (assumign tree is in breadth-first order).
	 * 
	 * @param collSize
	 * @return
	 */
	private double[] calculateProbs(double collSize) {
		double inc = collSize / 5;
		double[] probs = new double[(int) collSize];
		int counter = 0;
		double sum = 0;
		for (int i = (int) collSize; i > 0; i--) {
			probs[counter] = i * inc;
			sum += (i * inc);
			counter++;
		}
		for (int i = 0; i < probs.length; i++) {
			probs[i] = probs[i] / sum;
		}
		return probs;
	}

	@Override
	public List<Chromosome> iterate(AbstractEvo gp) {
		// System.out.println("Building new population");
		List<Chromosome> newPopulation = new ArrayList<Chromosome>(population);
		sel = gp.getSelection(population);

		for (int c = 0; c < mu; c++) {
			List<Chromosome> parents = sel.select(gp.getGPConf(), 2);
			Chromosome child = crossOver(parents.get(0), parents.get(1));
			if (rand.nextDouble() > mutation)
				child = mutate(child);
			population.add(child);
		}

		// System.out.println("Simplifying new population");
		newPopulation = newPopulation.stream().map(c -> c.simp()).collect(Collectors.toList());
		// System.out.println("Simplified new population");

		// System.out.println("Removing duplicates");
		newPopulation = gp.removeDuplicates(newPopulation);
		// System.out.println("Removed duplicates");

		// System.out.println("Evaluating new population");
		gp.evaluatePopulation(newPopulation);
		// System.out.println("Evaluated new population");

		// Sort by fitness and drop the worst if there's a surplus after removing
		// duplicates
		Collections.sort(newPopulation);
		if (newPopulation.size() > gp.getGPConf().getPopulationSize())
			newPopulation = newPopulation.subList(0, gp.getGPConf().getPopulationSize());

		// Add in new random individuals if there's a deficit after removing duplicates
		int remainder = gp.getGPConf().getPopulationSize() - newPopulation.size();

		if (remainder > 0) {
			// System.out.println("Adding in new individuals");
			List<Chromosome> rest = new ArrayList<Chromosome>(gp.generatePopulation(remainder, newPopulation));
			// System.out.println("Added in new individuals");
			gp.evaluatePopulation(rest);
			newPopulation.addAll(rest);
		}

		// System.out.println("Finished Iteration");
		return newPopulation;
	}

}
