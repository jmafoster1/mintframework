package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

public class EQStringsOperator extends EQBooleanOperator {

	public EQStringsOperator(Node<?> a, Node<?> b) {
		super(a, b);
	}

	public EQStringsOperator() {
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
		return new EQStringsOperator(g.generateRandomExpression(depth, Datatype.STRING),
				g.generateRandomExpression(depth, Datatype.STRING));
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> newInstance() {
		return new EQStringsOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.STRING, Datatype.STRING, Datatype.BOOLEAN };
	}

}
