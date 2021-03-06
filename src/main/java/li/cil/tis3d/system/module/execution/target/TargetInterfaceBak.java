package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.system.module.execution.Machine;

/**
 * Interface for the {@link Target#BAK} target.
 * <p>
 * Since this register can only be accessed using the <tt>SAV</tt> and
 * <tt>SWP</tt> instructions, trying to access it this way is an error
 * in the execution module implementation. Therefore, all methods in here
 * will throw an exception when called.
 */
public final class TargetInterfaceBak extends AbstractTargetInterface {
    public TargetInterfaceBak(final Machine machine) {
        super(machine);
    }

    @Override
    public boolean beginWrite(final int value) {
        throw throwOnWrite();
    }

    @Override
    public void cancelWrite() {
        throw throwOnWrite();
    }

    @Override
    public boolean isWriting() {
        throw throwOnWrite();
    }

    @Override
    public void beginRead() {
        throw throwOnRead();
    }

    @Override
    public boolean isReading() {
        throw throwOnRead();
    }

    @Override
    public boolean canTransfer() {
        throw throwOnRead();
    }

    @Override
    public int read() {
        throw throwOnRead();
    }

    private static IllegalArgumentException throwOnWrite() {
        throw new IllegalStateException("BAK cannot be written to directly.");
    }

    private static IllegalArgumentException throwOnRead() {
        throw new IllegalStateException("BAK cannot be read from directly.");
    }
}
