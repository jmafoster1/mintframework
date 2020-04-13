package mint.inference.gp.tree.nonterminals.booleans;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class GTBooleanIntegersOperator extends BooleanNonTerminal {

	public GTBooleanIntegersOperator(Node<?> b) {
		super(b);
	}

	public GTBooleanIntegersOperator(Node<IntegerVariableAssignment> a, Node<IntegerVariableAssignment> b) {
		super(null);
		addChild(a);
		addChild(b);
	}

	public GTBooleanIntegersOperator() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
		return new GTBooleanIntegersOperator(
				(Node<IntegerVariableAssignment>) g.generateRandomExpression(depth, Datatype.INTEGER),
				(Node<IntegerVariableAssignment>) g.generateRandomExpression(depth, Datatype.INTEGER));
	}

	@Override
	protected String nodeString() {
		return "GT(" + childrenString() + ")";
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			visitChildren(visitor);
		}
		return visitor.visitExit(this);
	}

	@Override
	public BooleanVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		BooleanVariableAssignment res = new BooleanVariableAssignment("result",
				(Long) children.get(0).evaluate().getValue() > (Long) children.get(1).evaluate().getValue());
		vals.add(res.getValue());
		return res;
	}

	@Override
	public String opString() {
		return ">";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkGt((ArithExpr) getChild(0).toZ3(ctx), (ArithExpr) getChild(1).toZ3(ctx));
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> newInstance() {
		return new GTBooleanIntegersOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.INTEGER, Datatype.INTEGER, Datatype.BOOLEAN };
	}
}
