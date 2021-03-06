package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionJumpEqualZero extends AbstractInstructionJumpConditional {
    public InstructionJumpEqualZero(final String label) {
        super(label);
    }

    @Override
    protected boolean isConditionTrue(final MachineState state) {
        return state.acc == 0;
    }

    @Override
    public String toString() {
        return "JEZ " + label;
    }
}
