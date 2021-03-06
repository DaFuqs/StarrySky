package de.dafuqs.starrysky.dimension;

import de.dafuqs.starrysky.Support;
import de.dafuqs.starrysky.spheroid.spheroids.Spheroid;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Random;

public abstract class SpheroidDecorator {

    /**
     * In contrast to vanilla the spheroid decorators are queried by the spheroid
     * not ran after the chunk generation
     * The spheroid tracks all blocks that can be decorated and the decorator
     * takes them, checks for spawning criteria and
     */
    public abstract void decorateSpheroid(StructureWorldAccess world, Spheroid spheroid, ArrayList<BlockPos> decorationBlockPositions, Random random);

    protected ArrayList<BlockPos> getDecorationBlockPosInChunk(Chunk chunk, ArrayList<BlockPos> decorationBlocks) {
        ArrayList<BlockPos> decorationBlockPosInChunk = new ArrayList<>();
        for(BlockPos currentDecorationPos : decorationBlocks) {
            if(Support.isBlockPosInChunkPos(chunk.getPos(), currentDecorationPos)) {
                decorationBlockPosInChunk.add(currentDecorationPos);
            }
        }
        return decorationBlockPosInChunk;
    }

    protected void placeLootChestAtPosition(StructureWorldAccess world, BlockPos blockPos, Identifier lootTable, Random random) {
        BlockState chestBlockState = Blocks.CHEST.getDefaultState();

        // if the chest is placed in water: waterlog it!
        if(world.getBlockState(blockPos) == Blocks.WATER.getDefaultState()) {
            chestBlockState = chestBlockState.with(ChestBlock.WATERLOGGED, true);
        }

        // Random direction placement for the chest
        int r = random.nextInt(4);
        Direction randomDirection;
        switch (r) {
            case 0: {
                randomDirection = Direction.NORTH;
                break;
            }
            case 1: {
                randomDirection = Direction.SOUTH;
                break;
            }
            case 2: {
                randomDirection = Direction.EAST;
                break;
            }
            default: {
                randomDirection = Direction.WEST;
                break;
            }
        }

        // set the chest and add loot table
        world.setBlockState(blockPos, chestBlockState.with(ChestBlock.FACING, randomDirection), 3);
        BlockEntity chestBlockEntity = world.getBlockEntity(blockPos);
        if (chestBlockEntity instanceof ChestBlockEntity) {
            ((ChestBlockEntity) chestBlockEntity).setLootTable(lootTable, random.nextLong());
        }
    }

    protected BlockPos findNextNonAirBlockInDirection(StructureWorldAccess world, BlockPos blockPos, Direction direction, int maxBlocks) {
        for(int i = 0; i < maxBlocks; i++) {
            if(!world.getBlockState(blockPos.offset(direction, i)).isAir()) {
                return blockPos;
            }
        }
        return null;
    }

}
