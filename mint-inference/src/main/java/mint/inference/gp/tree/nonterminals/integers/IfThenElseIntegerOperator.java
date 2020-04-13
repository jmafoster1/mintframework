package mint.inference.gp.tree.nonterminals.integers;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class IfThenElseIntegerOperator extends NonTerminal<VariableAssignment<?>> {

	public IfThenElseIntegerOperator() {
		super();
	}

	public IfThenElseIntegerOperator(Node<BooleanVariableAssignment> a, Node<?> yes, Node<?> no) {
		super();
		addChild(a);
		addChild(yes);
		addChild(no);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NonTerminal<VariableAssignment<?>> createInstance(Generator g, int depth) {
		return new IfThenElseIntegerOperator(
				(Node<BooleanVariableAssignment>) g.generateRandomExpression(depth, Datatype.BOOLEAN),
				g.generateRandomExpression(depth, Datatype.INTEGER),
				g.generateRandomExpression(depth, Datatype.INTEGER));
	}

	@Override
	protected String nodeString() {
		return "IF-THEN-ELSE(" + childrenString() + ")";
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			visitChildren(visitor);
		}
		return visitor.visitExit(this);
	}

	@Override
	public Terminal<VariableAssignment<?>> getTermFromVals() {
		return null;
	}

	@Override
	public VariableAssignment<?> evaluate() throws InterruptedException {
		checkInterrupted();
		boolean condition = (Boolean) getChild(0).evaluate().getValue();
		if (condition) {
			VariableAssignment<?> val = getChild(1).evaluate();
			vals.add(val);
			return val;
		} else {
			VariableAssignment<?> val = getChild(2).evaluate();
			vals.add(val);
			return val;
		}
	}

	@Override
	public String opString() {
		return "ite";
	}

	@Override
	public Expr toZ3(Context ctx) {
		BoolExpr b1 = (BoolExpr) getChild(0).toZ3(ctx);
		return ctx.mkITE(b1, getChild(1).toZ3(ctx), getChild(2).toZ3(ctx));
	}

	@Override
	public NonTerminal<VariableAssignment<?>> newInstance() {
		return new IfThenElseIntegerOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.BOOLEAN, Datatype.INTEGER, Datatype.INTEGER };
	}
}
