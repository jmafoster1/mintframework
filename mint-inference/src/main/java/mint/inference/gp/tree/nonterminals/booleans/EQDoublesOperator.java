package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

public class EQDoublesOperator extends EQBooleanOperator {

	public EQDoublesOperator(Node<?> a, Node<?> b) {
		super(a, b);
	}

	public EQDoublesOperator() {
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
		return new EQDoublesOperator(g.generateRandomExpression(depth, Datatype.DOUBLE),
				g.generateRandomExpression(depth, Datatype.DOUBLE));
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> newInstance() {
		return new EQDoublesOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.DOUBLE, Datatype.DOUBLE, Datatype.BOOLEAN };
	}

}
