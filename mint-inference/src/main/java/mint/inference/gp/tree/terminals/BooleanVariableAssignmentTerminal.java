package mint.inference.gp.tree.terminals;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class BooleanVariableAssignmentTerminal extends VariableTerminal<BooleanVariableAssignment> {

	public BooleanVariableAssignmentTerminal(VariableAssignment<Boolean> var, boolean constant, boolean latent) {
		super(constant, latent);
		this.terminal = (BooleanVariableAssignment) var;
	}

	@Override
	public BooleanVariableAssignmentTerminal copy() {
		VariableAssignment<Boolean> copied = terminal.copy();
		return new BooleanVariableAssignmentTerminal(copied, constant, LATENT);
	}

	@Override
	public boolean accept(NodeVisitor visitor) {
		visitor.visitEnter(this);
		return visitor.visitExit(this);
	}

	@Override
	public void setValue(Object val) {
		terminal.setValue((Boolean) val);
	}

	@Override
	protected Terminal<BooleanVariableAssignment> getTermFromVals() {
		BooleanVariableAssignment bvar = new BooleanVariableAssignment("res", (Boolean) vals.iterator().next());
		BooleanVariableAssignmentTerminal term = new BooleanVariableAssignmentTerminal(bvar, true, false);
		return term;
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			return ctx.mkBool(this.getTerminal().getValue());
		}
		if (this.isLatent())
			return ctx.mkBoolConst("latent" + this.getName());

		return ctx.mkBoolConst(this.getName());
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.BOOLEAN };
	}

	@Override
	protected Node<BooleanVariableAssignment> newInstance() {
		return new BooleanVariableAssignmentTerminal(terminal, constant, LATENT);
	}

}
