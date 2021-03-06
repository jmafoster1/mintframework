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
import mint.inference.gp.tree.nonterminals.booleans.AndBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQStringsOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.NotBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.OrBooleanOperator;
import mint.inference.gp.tree.nonterminals.integers.AddIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.StringVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
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
		boolNonTerms.add(new EQStringsOperator());
		boolNonTerms.add(new EQIntegersOperator());
		boolNonTerms.add(new NotBooleanOperator());
		boolNonTerms.add(new AndBooleanOperator());
		boolNonTerms.add(new OrBooleanOperator());
		gpGenerator.addFunctions(boolNonTerms);

		// Integer terminals
		List<VariableTerminal<?>> integerTerms = new ArrayList<VariableTerminal<?>>();
		integerTerms.add(new IntegerVariableAssignmentTerminal(0));
		integerTerms.add(new IntegerVariableAssignmentTerminal(1));
		integerTerms.add(new IntegerVariableAssignmentTerminal(2));
		gpGenerator.addTerminals(integerTerms);

		// Integer Nonterminals
		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		gpGenerator.addFunctions(intNonTerms);

		// String terminals
		List<VariableTerminal<?>> stringTerms = new ArrayList<VariableTerminal<?>>();
		stringTerms.add(new StringVariableAssignmentTerminal(new StringVariableAssignment("i0"), false, false));
		stringTerms.add(new StringVariableAssignmentTerminal(new StringVariableAssignment("tea", "tea"), true, false));
		stringTerms
				.add(new StringVariableAssignmentTerminal(new StringVariableAssignment("soup", "soup"), true, false));
		gpGenerator.addTerminals(stringTerms);

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		VariableAssignment<?> g1True = new BooleanVariableAssignment("g1", true);
		VariableAssignment<?> g1False = new BooleanVariableAssignment("g1", false);

		List<VariableAssignment<?>> s1 = new ArrayList<VariableAssignment<?>>();
		s1.add(new StringVariableAssignment("i0", "tea"));
		trainingSet.put(s1, g1False);

		List<VariableAssignment<?>> s2 = new ArrayList<VariableAssignment<?>>();
		s2.add(new StringVariableAssignment("i0", "soup"));
		trainingSet.put(s2, g1True);

		System.out.println(trainingSet);

		System.out.println(trainingSet.keys());

		System.out.println(trainingSet.keys().stream().anyMatch(x -> trainingSet.get(x).size() > 1));

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet, new GPConfiguration(5, 2, 1f, 2));

		Node<?> best = (Node<?>) gp.evolve(100);
		System.out.println(best + ":" + best.getFitness());
		System.out.println(best.simp());
		System.out.println(gp.isCorrect(best));
	}

}
