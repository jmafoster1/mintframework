import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
import mint.inference.gp.tree.nonterminals.booleans.AndBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.NotBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.OrBooleanOperator;
import mint.inference.gp.tree.nonterminals.integers.AddIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class GuardSRPlayground {

	public static void main(String[] args) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		long seed = System.currentTimeMillis();
		seed = 0l;

		System.out.println("Seed: " + seed);

		Generator gpGenerator = new Generator(new Random(seed));

		// Boolean terminals
		List<VariableTerminal<?>> boolTerms = new ArrayList<VariableTerminal<?>>();
		boolTerms.add(new BooleanVariableAssignmentTerminal(new BooleanVariableAssignment("tr", true), true, false));
		boolTerms.add(new BooleanVariableAssignmentTerminal(new BooleanVariableAssignment("fa", false), true, false));
		gpGenerator.addTerminals(boolTerms);

		// Boolean nonterminals
		List<NonTerminal<?>> boolNonTerms = new ArrayList<NonTerminal<?>>();
		boolNonTerms.add(new LTBooleanIntegersOperator());
		boolNonTerms.add(new GTBooleanIntegersOperator());
		boolNonTerms.add(new NotBooleanOperator());
		boolNonTerms.add(new AndBooleanOperator());
		boolNonTerms.add(new OrBooleanOperator());
		gpGenerator.addFunctions(boolNonTerms);

		// Integer terminals
		List<VariableTerminal<?>> integerTerms = new ArrayList<VariableTerminal<?>>();
//		integerTerms.add(new IntegerVariableAssignmentTerminal("r1", true));
		integerTerms.add(new IntegerVariableAssignmentTerminal(50));
		integerTerms.add(new IntegerVariableAssignmentTerminal(100));
		integerTerms.add(new IntegerVariableAssignmentTerminal(0));
		gpGenerator.addTerminals(integerTerms);

		// Integer Nonterminals
		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		gpGenerator.addFunctions(intNonTerms);

		List<VariableTerminal<?>> test = integerTerms.stream().filter(x -> x.isConstant()).collect(Collectors.toList());

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		VariableAssignment<?> o1True = new BooleanVariableAssignment("o1", true);
		VariableAssignment<?> o1False = new BooleanVariableAssignment("o1", false);
		VariableAssignment<?> r1_0 = new IntegerVariableAssignment("r1", 0l);

		List<VariableAssignment<?>> s1 = new ArrayList<VariableAssignment<?>>();
		s1.add(r1_0);
		trainingSet.put(s1, o1False);
//
		List<VariableAssignment<?>> s3 = new ArrayList<VariableAssignment<?>>();
		s3.add(r1_0);
		trainingSet.put(s3, o1True);

		System.out.println(trainingSet);

		System.out.println(trainingSet.keys());

		System.out.println(trainingSet.keys().stream().anyMatch(x -> trainingSet.get(x).size() > 1));

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet, new GPConfiguration(50, 0.9f, 1f, 7, 7));

		Node<?> best = (Node<?>) gp.evolve(5);
		System.out.println(best + ":" + best.getFitness());
		System.out.println(best.simp());
		System.out.println(gp.isCorrect(best));
	}

}
