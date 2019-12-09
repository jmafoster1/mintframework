package mint.inference.gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.evo.AbstractEvo;
import mint.inference.evo.Chromosome;
import mint.inference.evo.GPConfiguration;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/2018.
 */
public abstract class GP<T> extends AbstractEvo {

	protected Generator gen;
	protected boolean mem_dist = false;
	protected MultiValuedMap<List<VariableAssignment<?>>, T> evals;
	protected Map<Node<?>, List<Double>> distances;

	/**
	 * Takes as input a random program generator, a training set (a map from a list
	 * of input parameters to an output parameter) and a configuration.
	 *
	 * @param gpConf
	 */
	public GP(GPConfiguration gpConf) {
		super(gpConf);
	}

	public MultiValuedMap<List<VariableAssignment<?>>, T> getEvals() {
		return evals;
	}

	@Override
	public List<Chromosome> generatePopulation(int i) {
		List<Chromosome> population = null;
		population = gen.generatePopulation(i, getGPConf().getDepth(), getType());
		population.addAll(seeds);
		return population;
	}

	@Override
	public List<Chromosome> generatePopulation(int i, List<Chromosome> existing) {
		List<Chromosome> population = null;
		population = gen.generatePopulation(i, getGPConf().getDepth(), getType(), existing);
		population.addAll(seeds);
		return population;
	}

	protected abstract Datatype getType();

	public Map<Node<?>, List<Double>> getDistances() {
		return distances;
	}

	public abstract boolean isCorrect(Chromosome c);

	@Override
	public List<Chromosome> removeDuplicates(List<Chromosome> pop) {
		List<Chromosome> newPop = new ArrayList<Chromosome>();
		for (Chromosome c : pop) {
			if (!gen.populationContains(newPop, c))
				newPop.add(c);
		}
		return newPop;
	}
}
