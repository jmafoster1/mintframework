package mint.inference.gp.tree.nonterminals.integers;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class MultiplyIntegersOperator extends IntegerNonTerminal {
	public MultiplyIntegersOperator() {
	}

	public MultiplyIntegersOperator(Node<IntegerVariableAssignment> a, Node<IntegerVariableAssignment> b) {
		super();
		addChild(a);
		addChild(b);
	}

	@Override
	public IntegerVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		IntegerVariableAssignment childRes1 = null;
		IntegerVariableAssignment childRes2 = null;
		Long c1 = null;
		Long c2 = null;
		try {
			childRes1 = (IntegerVariableAssignment) getChild(0).evaluate();
			childRes2 = (IntegerVariableAssignment) getChild(1).evaluate();
			c1 = childRes1.getValue();
			c2 = childRes2.getValue();
			IntegerVariableAssignment res = copyResVar();
			res.setValue(c1 * c2);
			vals.add(res.getValue());
			return res;
		} catch (Exception e) {
		}

		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public NonTerminal<IntegerVariableAssignment> createInstance(Generator g, int depth) {
		IntegerNonTerminal created = new MultiplyIntegersOperator(
				(Node<IntegerVariableAssignment>) g.generateRandomExpression(depth, Datatype.INTEGER),
				(Node<IntegerVariableAssignment>) g.generateRandomExpression(depth, Datatype.INTEGER));
		created.setResVar(copyResVar());
		return created;
	}

	@Override
	public String nodeString() {
		return "Multiply(" + childrenString() + ")";
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
		return "*";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkMul((ArithExpr) getChild(0).toZ3(ctx), (ArithExpr) getChild(1).toZ3(ctx));
	}

	@Override
	protected NonTerminal<IntegerVariableAssignment> newInstance() {
		return new MultiplyIntegersOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.INTEGER, Datatype.INTEGER, Datatype.INTEGER };
	}
}
