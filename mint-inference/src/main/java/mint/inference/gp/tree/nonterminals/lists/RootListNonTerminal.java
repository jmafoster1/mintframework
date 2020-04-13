package mint.inference.gp.tree.nonterminals.lists;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.ListVariableAssignment;

/**
 *
 * For the string, set out the types of the elements in this list: d - double b
 * - boolean i - integer
 *
 * Created by neilwalkinshaw on 5/03/18.
 */
public class RootListNonTerminal extends NonTerminal<ListVariableAssignment> {

	protected String types;

	public RootListNonTerminal(String types) {
		this.types = types;
	}

	public RootListNonTerminal(List<Node<?>> a) {
		super();

		for (Node<?> element : a) {

			addChild(element);
		}

	}

	@Override
	public NonTerminal<ListVariableAssignment> createInstance(Generator g, int depth) {
		List<Node<?>> elements = new ArrayList<Node<?>>();
		for (int i = 0; i < types.length(); i++) {
			char c = types.charAt(i);
			if (c == 'd')
				elements.add(g.generateRandomExpression(depth, Datatype.DOUBLE));
			else if (c == 'i')
				elements.add(g.generateRandomExpression(depth, Datatype.INTEGER));
			else
				elements.add(g.generateRandomExpression(depth, Datatype.BOOLEAN));
		}
		return new RootListNonTerminal(elements);
	}

	@Override
	public ListVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < getChildren().size(); i++) {
			result.add(getChild(i).evaluate().getValue());
		}
		ListVariableAssignment res = new ListVariableAssignment("result", result);
		vals.add(res);
		return res;
	}

	@Override
	public String nodeString() {
		return "R:" + childrenString();
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			for (Node<?> child : children) {
				child.accept(visitor);
			}
		}
		return visitor.visitExit(this);
	}

	@Override
	public int depth() {
		return 0;
	}

	/**
	 * Not meaningful for a list.
	 * 
	 * @return
	 */
	@Override
	public Terminal<ListVariableAssignment> getTermFromVals() {
		return null;
	}

	@Override
	public String opString() {
		return "";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return getChild(0).toZ3(ctx);
	}

	@Override
	public NonTerminal<ListVariableAssignment> newInstance() {
		return new RootListNonTerminal(types);
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.LIST };
	}
}
