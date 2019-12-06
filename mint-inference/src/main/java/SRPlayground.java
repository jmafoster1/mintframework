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
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.integers.AddIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class SRPlayground {

	public static void main(String[] args) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		long seed = System.currentTimeMillis();

//		seed = 1575651767134L;
		System.out.println("Seed: " + seed);

		Generator gpGenerator = new Generator(new Random(seed));

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		gpGenerator.setIntegerFunctions(intNonTerms);

		List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();

		intTerms.add(new IntegerVariableAssignmentTerminal("i0", false));
		intTerms.add(new IntegerVariableAssignmentTerminal("r1", true));

		for (int i : new int[] { 0, 1, 50, 10, 20, 100, 30, 70 })
			intTerms.add(new IntegerVariableAssignmentTerminal(i));

		gpGenerator.setIntegerTerminals(intTerms);

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		List<VariableAssignment<?>> i10 = new ArrayList<VariableAssignment<?>>();
		i10.add(new IntegerVariableAssignment("i0", 10));

		List<VariableAssignment<?>> i20 = new ArrayList<VariableAssignment<?>>();
		i20.add(new IntegerVariableAssignment("i0", 20));

		List<VariableAssignment<?>> i50 = new ArrayList<VariableAssignment<?>>();
		i50.add(new IntegerVariableAssignment("i0", 50));

		List<VariableAssignment<?>> i100 = new ArrayList<VariableAssignment<?>>();
		i100.add(new IntegerVariableAssignment("i0", 100));

		trainingSet.put(i50, new IntegerVariableAssignment("o1", 50));
		trainingSet.put(i50, new IntegerVariableAssignment("o1", 100));
		trainingSet.put(i100, new IntegerVariableAssignment("o1", 100));

		trainingSet.put(i10, new IntegerVariableAssignment("o1", 10));
		trainingSet.put(i20, new IntegerVariableAssignment("o1", 30));
		trainingSet.put(i50, new IntegerVariableAssignment("o1", 70));
		trainingSet.put(i100, new IntegerVariableAssignment("o1", 100));

		System.out.println("Training set: " + trainingSet);
		System.out.println("IntTerms: " + intTerms);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet,
				new GPConfiguration(20, 0.9f, 0.01f, 5, 2));

//		AddIntegersOperator seed = new AddIntegersOperator(new IntegerVariableAssignmentTerminal("i0", false),
//				new IntegerVariableAssignmentTerminal("r1", true));
//		gp.addSeed(seed);

		Node<?> best = (Node<?>) gp.evolve(100);
		System.out.println(best + ": " + best.getFitness());
		System.out.println("correct? " + gp.isCorrect(best));
		System.out.println(best.simp());

//		System.out.println();
//		for (Chromosome c1 : gp.getPopulation()) {
//			for (Chromosome c2 : gp.getPopulation()) {
//				System.out.print(c1 + " == " + c2 + "? ");
//				System.out.print(c1.sameSyntax(c2) + " ");
//			}
//			System.out.println();
//		}

//		int counter = 0;
//		for (Chromosome c : gp.getPopulation()) {
//			counter++;
//			Node<?> node = (Node<?>) c;
//			LatentVariableFitness<?> fit = new IntegerFitness(trainingSet, (Node<VariableAssignment<Integer>>) node, 0);
//			try {
//				System.out.println(counter + ". " + node + ": " + fit.call());
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
}
