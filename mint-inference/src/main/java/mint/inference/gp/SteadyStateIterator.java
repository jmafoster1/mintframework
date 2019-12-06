package mint.inference.gp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;
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

	public SteadyStateIterator(List<Chromosome> elite, List<Chromosome> population, double crossOver, double mutation,
			Generator g, int maxD, Random r) {
		super(elite, population, crossOver, mutation, r);
		this.gen = g;
		this.maxDepth = maxD;
	}

	@Override
	protected Chromosome mutate(Chromosome root) {
		root = root.copy();
		List<Node<?>> nt = new ArrayList<Node<?>>();

		addAllChildren((Node<?>) root, nt, null/* , maxDepth */);
		removeUnviableNodes(nt);
		if (nt.isEmpty())
			return root;
		Node<?> toMutate = pickRandomBiasEarly(nt, rand.nextDouble());

		toMutate.mutate(gen, rand.nextInt(maxDepth - toMutate.depth()));
		return root.simp();
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
			crossOverA.swapWith(crossOverB);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			LOGGER.debug(crossOverA + ", " + crossOverB);
		}
		return aCopy.simp();
	}

	protected void removeUnviableNodes(List<Node<?>> nt) {
		Collection<Node<?>> toRemove = new HashSet<Node<?>>();
		for (Node<?> n : nt) {
			if (n.depth() >= maxDepth)
				toRemove.add(n);
		}
		nt.removeAll(toRemove);
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
		List<Node<?>> nt = new ArrayList<Node<?>>();
//		int depth = maxDepth;
//		if (target != null)
//			depth = maxDepth - target.depth();
		addAllChildren(tree, nt, target/* , depth */); // only add nodes that are same type as target
		Node<?> picked = null;
		removeUnviableNodes(nt);
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

	/**
	 * Add children to to nt that could feasibly be candidates for crossover. For
	 * this their subtree depth must be < childMaxDepth, and they must be of the
	 * same type as target.
	 *
	 * @param tree
	 * @param nt
	 * @param target
	 */
	protected void addAllChildren(Node<?> tree, List<Node<?>> nt, Node<?> target) {
		Stack<Node<?>> worklist = new Stack<Node<?>>();
		for (Node<?> child : tree.getChildren()) {
			worklist.push(child);
			if (target != null) {
				if (!target.getReturnType().equals(child.getReturnType()))
					continue;
			}
			nt.add(child);
			Collections.shuffle(nt, rand);
		}
		while (!worklist.isEmpty()) {
			List<Node<?>> toAdd = new ArrayList<Node<?>>();
			List<Node<?>> forThisDepth = new ArrayList<Node<?>>();
			Node<?> c = worklist.pop();
			for (Node<?> child : c.getChildren()) {
				toAdd.add(child);
				if (target != null) {
					if (!target.getReturnType().equals(child.getReturnType()))
						continue;
				}
				forThisDepth.add(child);
			}
			Collections.shuffle(forThisDepth, rand);
			nt.addAll(forThisDepth);
			worklist.addAll(toAdd);
		}
	}

	@Override
	public List<Chromosome> iterate(AbstractEvo gp) {
		List<Chromosome> newPopulation = new ArrayList<Chromosome>(population);
		sel = gp.getSelection(population);

		int numberCrossover = (int) ((population.size() - elite.size()) * crossOver);
		int numberMutation = (int) ((population.size() - elite.size()) * mutation);
		for (int crossOvers = 0; crossOvers < numberCrossover; crossOvers++) {
			List<Chromosome> parents = sel.select(gp.getGPConf(), 2);
			newPopulation.add(crossOver(parents.get(0), parents.get(1)));
		}
		for (int mutations = 0; mutations < numberMutation; mutations++) {
			newPopulation.add(mutate(population.get(rand.nextInt(population.size()))));
		}

		gp.evaluatePopulation(newPopulation);

		newPopulation = newPopulation.stream().map(c -> c.simp()).collect(Collectors.toList());

		newPopulation = gp.removeDuplicates(newPopulation);
		Collections.sort(newPopulation);

		if (newPopulation.size() > gp.getGPConf().getPopulationSize())
			newPopulation = newPopulation.subList(0, gp.getGPConf().getPopulationSize());

		int remainder = gp.getGPConf().getPopulationSize() - newPopulation.size();
		if (remainder > 0) {
			newPopulation.addAll(gp.generatePopulation(remainder));
		}

		Collections.shuffle(newPopulation, rand);
		return newPopulation;
	}

}
