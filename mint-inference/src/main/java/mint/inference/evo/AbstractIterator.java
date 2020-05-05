package mint.inference.evo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Responsible for creating the offspring in an iteration by applying mutation
 * and crossover. Also retains a small number (3) elite offspring from the
 * previous generation.
 *
 * Created by neilwalkinshaw on 06/03/15.
 */
public abstract class AbstractIterator {

	protected List<Chromosome> population;
	protected List<Chromosome> elite;
	protected Random rand;
	public int mu;
	public double mutation;
	protected Selection sel;

	public AbstractIterator(List<Chromosome> elites, List<Chromosome> population, int crossOver, double mutation,
			Random r) {
		rand = r;
		this.population = population;
		this.mutation = mutation;
		this.mu = crossOver;
		this.elite = new ArrayList<Chromosome>();
		this.elite.addAll(elites);
	}

	public List<Chromosome> iterate(AbstractEvo gp) {
		Collections.shuffle(population, rand);
		List<Chromosome> newPopulation = new ArrayList<>();
		for (Chromosome el : elite) {
			newPopulation.add(el.copy());
		}
		int numberCrossover = (population.size() - elite.size()) * mu;
		int numberMutation = (int) ((population.size() - elite.size()) * mutation);
		for (int crossOvers = 0; crossOvers < numberCrossover; crossOvers++) {
			sel = gp.getSelection(population);
			List<Chromosome> parents = sel.select(gp.getGPConf(), 2);
			newPopulation.add(crossOver(parents.get(0), parents.get(1)));
		}
		for (int mutations = 0; mutations < numberMutation; mutations++) {
			newPopulation.add(mutate(population.get(rand.nextInt(population.size()))));
		}

		int remainder = gp.getGPConf().getPopulationSize() - newPopulation.size();
		if (remainder > 0) {
			newPopulation.addAll(gp.generatePopulation(remainder));
		}

		Collections.shuffle(newPopulation, rand);
		return newPopulation;
	}

	protected abstract Chromosome mutate(Chromosome toMutate);

	protected abstract Chromosome crossOver(Chromosome parentA, Chromosome parentB);

	public Selection getLatestSelection() {
		return sel;
	}

}
