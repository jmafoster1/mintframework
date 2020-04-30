package mint.inference.gp.tree.terminals;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class IntegerVariableAssignmentTerminal extends VariableTerminal<IntegerVariableAssignment> {

	protected long origVal;

	public IntegerVariableAssignmentTerminal(VariableAssignment<Long> var, boolean constant, boolean latent) {
		super(constant, latent);
		if (var.getValue() != null)
			origVal = var.getValue();
		this.terminal = (IntegerVariableAssignment) var;
		if (constant && !var.getName().equals(var.getValue().toString())) {
			throw new IllegalStateException("GOT YOU! " + var.getName() + "=" + var.getValue());
		}
	}

	// For initialising constants
	public IntegerVariableAssignmentTerminal(long value) {
		super(true, false);
		IntegerVariableAssignment var = new IntegerVariableAssignment(String.valueOf(value), value, true);
		this.terminal = var;
	}

	// For initialising variables
	public IntegerVariableAssignmentTerminal(String name, boolean latent) {
		super(false, latent);
		IntegerVariableAssignment var = new IntegerVariableAssignment(name);
		this.terminal = var;
		if (constant && var.getName() != var.getValue().toString()) {
			System.out.println("GOT YOU! " + var.getName() + "=" + var.getValue());
			System.exit(1);
		}
	}

	@Override
	public void setValue(Object val) {
		if ((val instanceof Integer) || (val instanceof Long)) {
			Long intval = (Long) val;
			terminal.setValue(intval);
		}
	}

	@Override
	protected Terminal<IntegerVariableAssignment> getTermFromVals() {
		IntegerVariableAssignment ivar = new IntegerVariableAssignment("res", (Long) vals.iterator().next());
		IntegerVariableAssignmentTerminal term = new IntegerVariableAssignmentTerminal(ivar, true, false);
		return term;
	}

	@Override
	public IntegerVariableAssignmentTerminal copy() {
		VariableAssignment<Long> copied = terminal.copy();
		return new IntegerVariableAssignmentTerminal(copied, constant, LATENT);
	}

	@Override
	public boolean accept(NodeVisitor visitor) {
		visitor.visitEnter(this);
		return visitor.visitExit(this);
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			return ctx.mkInt(this.getTerminal().getValue());
		}
		if (this.isLatent())
			return ctx.mkIntConst("latent" + this.getName());

		return ctx.mkIntConst(this.getName());
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.INTEGER };
	}

	@Override
	protected Node<IntegerVariableAssignment> newInstance() {
		return new IntegerVariableAssignmentTerminal(terminal, constant, LATENT);
	}
}
