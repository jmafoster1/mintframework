package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

public class EQIntegersOperator extends EQBooleanOperator {

	public EQIntegersOperator(Node<?> a, Node<?> b) {
		super(a, b);
	}

	public EQIntegersOperator() {
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
		return new EQIntegersOperator(g.generateRandomExpression(depth, Datatype.INTEGER),
				g.generateRandomExpression(depth, Datatype.INTEGER));
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> newInstance() {
		return new EQIntegersOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.INTEGER, Datatype.INTEGER, Datatype.BOOLEAN };
	}

}
