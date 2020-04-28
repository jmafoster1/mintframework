package mint.inference.gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.AndBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.OrBooleanOperator;
import mint.inference.gp.tree.nonterminals.doubles.AddDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.IfThenElseOperator;
import mint.inference.gp.tree.nonterminals.doubles.MultiplyDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.SubtractDoublesOperator;
import mint.inference.gp.tree.nonterminals.integers.CastIntegersOperator;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public class GPFSMInferenceTester {

	@Test
	public void inferenceTester() {
		Generator gpGenerator = new Generator(new Random(0));

		List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
		doubleNonTerms.add(new AddDoublesOperator());
		doubleNonTerms.add(new SubtractDoublesOperator());
		doubleNonTerms.add(new MultiplyDoublesOperator());
		doubleNonTerms.add(new IfThenElseOperator());
		gpGenerator.addFunctions(doubleNonTerms);

		List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("a"), false, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("b"), false, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("1", 1.0), true, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0.5", 0.5), true, false));
		gpGenerator.addTerminals(doubleTerms);

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new CastIntegersOperator());
		gpGenerator.addFunctions(intNonTerms);

		List<NonTerminal<?>> boolNonTerms = new ArrayList<NonTerminal<?>>();
		boolNonTerms.add(new AndBooleanOperator());
		boolNonTerms.add(new OrBooleanOperator());
		boolNonTerms.add(new LTBooleanDoublesOperator());
		boolNonTerms.add(new GTBooleanDoublesOperator());
		gpGenerator.addFunctions(boolNonTerms);

		List<VariableTerminal<?>> boolTerms = new ArrayList<VariableTerminal<?>>();
		VariableAssignment<Boolean> truevar = new BooleanVariableAssignment("true", true);
		BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true, false);
		VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("false", false);
		BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true, false);
		boolTerms.add(trueterm);
		boolTerms.add(falseterm);
		gpGenerator.addTerminals(boolTerms);

		// SingleOutputGP gp = new SingleOutputGP(gpGenerator,
		// generateBooleanTrainingSet(500),new GPConfiguration(600,0.95,0.05,7,10));

		// System.out.println(gp.evolve(20));
	}

}
