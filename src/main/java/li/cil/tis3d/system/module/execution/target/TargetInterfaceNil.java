package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.system.module.execution.Machine;

/**
 * Interface for the {@link Target#NIL} target.
 * <p>
 * Provides instant read and write on the virtual <tt>NIL</tt> register,
 * which will always return <tt>0</tt> when read, and silently consume all
 * values written to it.
 */
public final class TargetInterfaceNil extends AbstractTargetInterface {
    public TargetInterfaceNil(final Machine machine) {
        super(machine);
    }

    @Override
    public boolean beginWrite(final int value) {
        return true;
    }

    @Override
    public void cancelWrite() {
    }

    @Override
    public boolean isWriting() {
        return false;
    }

    @Override
    public void beginRead() {
    }

    @Override
    public boolean isReading() {
        return false;
    }

    @Override
    public boolean canTransfer() {
        return true;
    }

    @Override
    public int read() {
        return 0;
    }
}
