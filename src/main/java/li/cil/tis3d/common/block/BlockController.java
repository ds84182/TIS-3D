package li.cil.tis3d.common.block;

import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.common.tile.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Block for the controller driving the casings.
 */
public final class BlockController extends Block {
    public BlockController() {
        super(Material.iron);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean isSideSolid(final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        // Allow levers to be placed on us (wouldn't work because of isFullCube = false otherwise).
        return true;
    }

    @Override
    public boolean isFullCube() {
        // Prevent fences from visually connecting.
        return false;
    }

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new TileEntityController();
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(final World world, final BlockPos pos) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            return controller.getState() == TileEntityController.ControllerState.READY ? 15 : 0;
        }
        return 0;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @Override
    public void onNeighborBlockChange(final World world, final BlockPos pos, final IBlockState state, final Block neighborBlock) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            for (final EnumFacing facing : EnumFacing.VALUES) {
                checkNeighbor(controller, facing);
            }
        }
        super.onNeighborBlockChange(world, pos, state, neighborBlock);
    }

    private static void checkNeighbor(final TileEntityController controller, final EnumFacing facing) {
        final BlockPos neighborPos = controller.getPos().offset(facing);
        if (controller.getWorld().isBlockLoaded(neighborPos)) {
            final TileEntity neighborTileEntity = controller.getWorld().getTileEntity(neighborPos);
            if (neighborTileEntity instanceof TileEntityController) {
                // If we have a controller that means we have more than one in
                // our multi-block. Rescan to enter appropriate error state.
                controller.scheduleScan();
            } else if (neighborTileEntity instanceof TileEntityCasing) {
                // Rescan if we don't know that casing (yet).
                final TileEntityCasing casing = (TileEntityCasing) neighborTileEntity;
                if (casing.getController() != controller) {
                    controller.scheduleScan();
                }
            }
        } else {
            // Make sure we notice we're on the border of the loaded area.
            controller.scheduleScan();
        }
    }
}
