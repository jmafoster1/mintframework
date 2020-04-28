import java.util.ArrayList;
import java.util.Arrays;
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
import mint.inference.gp.tree.nonterminals.integers.MultiplyIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class UpdateSRPlayground {

	static MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

	private static void scenario(int r2, int r2_prime) {
		trainingSet.put(Arrays.asList(new IntegerVariableAssignment("r1", (long) r2)),
				new IntegerVariableAssignment("r1", (long) r2_prime));
	}

	public static void main(String[] args) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(21100));

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		intNonTerms.add(new MultiplyIntegersOperator());
		gpGenerator.addFunctions(intNonTerms);

		List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();
		intTerms.add(new IntegerVariableAssignmentTerminal("r1", false));
		intTerms.add(new IntegerVariableAssignmentTerminal("r2", true));
		intTerms.add(new IntegerVariableAssignmentTerminal(0));
		intTerms.add(new IntegerVariableAssignmentTerminal(1));
		intTerms.add(new IntegerVariableAssignmentTerminal(2));
		intTerms.add(new IntegerVariableAssignmentTerminal(150));
		intTerms.add(new IntegerVariableAssignmentTerminal(100));
		intTerms.add(new IntegerVariableAssignmentTerminal(350));
		intTerms.add(new IntegerVariableAssignmentTerminal(300));
		intTerms.add(new IntegerVariableAssignmentTerminal(250));
		intTerms.add(new IntegerVariableAssignmentTerminal(200));
		intTerms.add(new IntegerVariableAssignmentTerminal(50));
		intTerms.add(new IntegerVariableAssignmentTerminal(-50));
		gpGenerator.addTerminals(intTerms);

		scenario(50, 0);
		scenario(100, 50);
		scenario(150, 100);

		System.out.println("Training set: " + trainingSet);
		System.out.println("IntTerms: " + intTerms);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet, new GPConfiguration(100, 0.9f, 1f, 3, 1));

		NonTerminal<?> add = new AddIntegersOperator();
		add.addChild(new IntegerVariableAssignmentTerminal(100));
		add.addChild(new IntegerVariableAssignmentTerminal("r1", false));
		gp.addSeed(add);

//		IntegerVariableAssignmentTerminal seed = new IntegerVariableAssignmentTerminal(50);
//		gp.addSeed(seed);

		Node<?> best = (Node<?>) gp.evolve(20);
		System.out.println(best + ": " + best.getFitness());
		System.out.println("correct? " + gp.isCorrect(best));
		System.out.println("varsInTree: " + best.varsInTree());
		System.out.println("latentVars: " + best.latentVars());

	}
}
