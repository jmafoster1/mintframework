package mint.inference.gp.tree.nonterminals.doubles;

import com.microsoft.z3.ArithExpr;
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
public class DivideDoublesOperator extends DoubleNonTerminal {

	public DivideDoublesOperator() {
	}

	protected DivideDoublesOperator(Node<DoubleVariableAssignment> a, Node<DoubleVariableAssignment> b) {
		super();

		addChild(a);
		addChild(b);
	}

	@Override
	public DoubleVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		try {
			double top = (Double) getChild(0).evaluate().getValue();
		} catch (ClassCastException e) {
			System.out.println(this);
			e.printStackTrace();
			System.exit(1);
		}
		double top = (Double) getChild(0).evaluate().getValue();
		double bottom = (Double) getChild(1).evaluate().getValue();
		double result = top / bottom;
		// should throw an exception for divide-by-zero. Would lead to a penalisation in
		// fitness function.
		DoubleVariableAssignment res = copyResVar();
		res.setValue(result);
		vals.add(res.getValue());
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth) {
		DivideDoublesOperator ddo = new DivideDoublesOperator(
				(Node<DoubleVariableAssignment>) g.generateRandomExpression(depth, Datatype.DOUBLE),
				(Node<DoubleVariableAssignment>) g.generateRandomExpression(depth, Datatype.DOUBLE));
		ddo.setResVar(copyResVar());
		return ddo;
	}

	@Override
	public String nodeString() {
		return "Div(" + childrenString() + ")";
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
		return "/";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkDiv((ArithExpr) getChild(0).toZ3(ctx), (ArithExpr) getChild(1).toZ3(ctx));
	}

	@Override
	public NonTerminal<DoubleVariableAssignment> newInstance() {
		return new DivideDoublesOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE };
	}
}
