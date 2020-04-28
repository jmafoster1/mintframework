import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import mint.inference.evo.GPConfiguration;
import mint.inference.gp.Generator;
import mint.inference.gp.LatentVariableGP;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.nonterminals.integers.AddIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class SRPlayground {

	public static MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> generateTrainingSet() {
		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		List<VariableAssignment<?>> i10 = new ArrayList<VariableAssignment<?>>();
		i10.add(new IntegerVariableAssignment("i0", 10l));

		List<VariableAssignment<?>> i20 = new ArrayList<VariableAssignment<?>>();
		i20.add(new IntegerVariableAssignment("i0", 20l));

		List<VariableAssignment<?>> i50 = new ArrayList<VariableAssignment<?>>();
		i50.add(new IntegerVariableAssignment("i0", 50l));

		List<VariableAssignment<?>> i100 = new ArrayList<VariableAssignment<?>>();
		i100.add(new IntegerVariableAssignment("i0", 100l));

		trainingSet.put(i50, new IntegerVariableAssignment("o1", 50l));
		trainingSet.put(i50, new IntegerVariableAssignment("o1", 100l));
		trainingSet.put(i100, new IntegerVariableAssignment("o1", 100l));

		trainingSet.put(i10, new IntegerVariableAssignment("o1", 10l));
		trainingSet.put(i20, new IntegerVariableAssignment("o1", 30l));
		trainingSet.put(i50, new IntegerVariableAssignment("o1", 70l));
		trainingSet.put(i100, new IntegerVariableAssignment("o1", 100l));

		return trainingSet;
	}

	public static boolean run(long seed) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(seed));

		gpGenerator.add(new AddIntegersOperator());
		gpGenerator.add(new SubtractIntegersOperator());

		gpGenerator.add(new IntegerVariableAssignmentTerminal("i0", false));
		gpGenerator.add(new IntegerVariableAssignmentTerminal("r1", true));

		for (int i : new int[] { 0, 1, 50, 10, 20, 100, 30, 70 })
			gpGenerator.add(new IntegerVariableAssignmentTerminal(i));

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = generateTrainingSet();

		System.out.println("Training set: " + trainingSet);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet,
				new GPConfiguration(20, 0.9f, 0.01f, 5, 2));

//		AddIntegersOperator seed = new AddIntegersOperator(new IntegerVariableAssignmentTerminal("i0", false),
//				new IntegerVariableAssignmentTerminal("r1", true));
//		gp.addSeed(seed);

		Node<?> best = (Node<?>) gp.evolve(100);
		System.out.println(best + ": " + best.getFitness());
		System.out.println(best.simp());
		System.out.println("correct? " + gp.isCorrect(best));

		return best.toString().equals(best.simp().toString());
	}

	public static void main(String[] args) {
//		run(1575902062141l);
		long seed = System.currentTimeMillis();
		boolean stop = false;
		while (!stop) {
			System.out.println("Seed: " + seed);
			stop = !run(seed);
			seed = System.currentTimeMillis();
		}

	}
}
