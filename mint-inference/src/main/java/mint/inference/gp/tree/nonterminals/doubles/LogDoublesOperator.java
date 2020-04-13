package mint.inference.gp.tree.nonterminals.doubles;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class LogDoublesOperator extends DoubleNonTerminal {

	public LogDoublesOperator() {
	}

	protected LogDoublesOperator(Node<DoubleVariableAssignment> a) {
		super();
		addChild(a);
	}

	@Override
	public DoubleVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		DoubleVariableAssignment res = copyResVar();
		res.setValue(Math.log((Double) children.get(0).evaluate().getValue()));
		vals.add(res.getValue());
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth) {
		LogDoublesOperator ldo = new LogDoublesOperator(
				(Node<DoubleVariableAssignment>) g.generateRandomExpression(depth, Datatype.DOUBLE));
		ldo.setResVar(copyResVar());
		return ldo;
	}

	@Override
	public String nodeString() {
		return "Log(" + childrenString() + ")";
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			visitChildren(visitor);
		}
		return visitor.visitExit(this);
	}

	@Override
	public String opString() {
		return "log";
	}

	@Override
	public Expr toZ3(Context ctx) {
		throw new IllegalArgumentException("Cannot do Log to z3");
	}

	@Override
	public NonTerminal<DoubleVariableAssignment> newInstance() {
		return new LogDoublesOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.DOUBLE, Datatype.DOUBLE };
	}
}
