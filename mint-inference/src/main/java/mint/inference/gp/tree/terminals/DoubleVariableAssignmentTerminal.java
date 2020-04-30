package mint.inference.gp.tree.terminals;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class DoubleVariableAssignmentTerminal extends VariableTerminal<DoubleVariableAssignment> {

	double origVal;

	public DoubleVariableAssignmentTerminal(VariableAssignment<Double> var, boolean constant, boolean latent) {
		super(constant, latent);
		if (var.getValue() != null)
			origVal = var.getValue();
		this.terminal = (DoubleVariableAssignment) var;
	}

	@Override
	public void setValue(Object val) {
		if (val instanceof Double)
			terminal.setValue((Double) val);
		else if (val instanceof Integer) {
			Integer intval = (Integer) val;
			Double doubVal = (double) intval.intValue();
			terminal.setValue(doubVal);
		}
	}

	@Override
	protected Terminal<DoubleVariableAssignment> getTermFromVals() {
		DoubleVariableAssignment dvar = new DoubleVariableAssignment("res", (Double) vals.iterator().next());
		DoubleVariableAssignmentTerminal term = new DoubleVariableAssignmentTerminal(dvar, true, false);
		return term;
	}

	@Override
	public DoubleVariableAssignmentTerminal copy() {
		VariableAssignment<Double> copied = terminal.copy();
		return new DoubleVariableAssignmentTerminal(copied, constant, LATENT);
	}

	@Override
	public boolean accept(NodeVisitor visitor) {
		visitor.visitEnter(this);
		return visitor.visitExit(this);
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			return ctx.mkReal(this.getTerminal().getValue().longValue());
		}
		if (this.isLatent())
			return ctx.mkRealConst("latent" + this.getName());

		return ctx.mkRealConst(this.getName());
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.DOUBLE };
	}

	@Override
	protected Node<DoubleVariableAssignment> newInstance() {
		return new DoubleVariableAssignmentTerminal(terminal, constant, LATENT);
	}
}
