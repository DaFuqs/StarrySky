package de.dafuqs.starrysky.spheroids.special_overworld;

import de.dafuqs.starrysky.Support;
import de.dafuqs.starrysky.spheroids.Spheroid;
import de.dafuqs.starrysky.spheroidtypes.special_overworld.BeeHiveSpheroidType;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;

import java.util.ArrayList;
import java.util.List;

public class BeeHiveSpheroid extends Spheroid {

    private int shellRadius;
    private int flowerRingRadius;
    private int flowerRingSpacing;
    private BeehiveBlockEntity queenBeehiveBlockEntity;
    private List<BeehiveBlockEntity> outerBeehiveBlockEntities;

    public BeeHiveSpheroid(BeeHiveSpheroidType beeHiveSpheroidType, ChunkRandom random) {
        super(beeHiveSpheroidType, random);

        this.shellRadius = beeHiveSpheroidType.getRandomShellRadius(random);
        this.radius = beeHiveSpheroidType.getRandomRadius(random);
        this.flowerRingRadius = beeHiveSpheroidType.getRandomFlowerRingRadius(random);
        this.flowerRingSpacing = beeHiveSpheroidType.getRandomFlowerRingSpacing(random);
        this.outerBeehiveBlockEntities = new ArrayList<>();
    }

    @Override
    public void generate(Chunk chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        int x = this.getPosition().getX();
        int y = this.getPosition().getY();
        int z = this.getPosition().getZ();

        int endRingDistance = this.radius;
        int startRingDistance = this.radius - this.flowerRingRadius;
        int shellDistance = startRingDistance - this.flowerRingSpacing;
        int coreDistance = shellDistance - shellRadius;

        random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
        for (int x2 = Math.max(chunkX * 16, x - this.radius); x2 <= Math.min(chunkX * 16 + 15, x + this.radius); x2++) {
            for (int y2 = y - this.radius; y2 <= y + this.radius; y2++) {
                for (int z2 = Math.max(chunkZ * 16, z - this.radius); z2 <= Math.min(chunkZ * 16 + 15, z + this.radius); z2++) {
                    BlockPos currBlockPos = new BlockPos(x2, y2, z2);
                    float d = Math.round(Support.distance(x, y, z, x2, y2, z2));
                    if(d == 0) {
                        // bee hive in center
                        chunk.setBlockState(currBlockPos, Blocks.BEE_NEST.getDefaultState(), false);
                        this.queenBeehiveBlockEntity = new BeehiveBlockEntity();
                        chunk.setBlockEntity(currBlockPos, queenBeehiveBlockEntity);
                    } else if (d < coreDistance) {
                        // core
                        int r = random.nextInt((int) Math.ceil(coreDistance / 3F)); // way more honey in the middle
                        if (coreDistance - r <= d) {
                            chunk.setBlockState(currBlockPos, Blocks.HONEY_BLOCK.getDefaultState(), false);
                        } else {
                            chunk.setBlockState(currBlockPos, Blocks.AIR.getDefaultState(), false);
                        }
                    } else if (d == shellDistance -1 && y2 - y == 0 && random.nextInt(10) == 0) {
                        // middle outer shell: random hives
                        Direction direction;
                        int xDist = x2 - x;
                        int zDist = z2 - z;
                        if(xDist > 0) {
                            if(Math.abs(xDist) > Math.abs(zDist)) {
                                direction = Direction.EAST;
                            } else {
                                if(zDist > 0) {
                                    direction = Direction.SOUTH;
                                } else {
                                    direction = Direction.NORTH;
                                }
                            }
                        } else {
                            if(Math.abs(xDist) < Math.abs(zDist)) {
                                if(zDist > 0) {
                                    direction = Direction.SOUTH;
                                } else {
                                    direction = Direction.NORTH;
                                }
                            } else {
                                direction = Direction.WEST;
                            }
                        }
                        // set the block
                        BlockState blockState = Blocks.BEE_NEST.getDefaultState().with(BeehiveBlock.FACING, direction);
                        chunk.setBlockState(currBlockPos, blockState, false);

                        // set and save the blockentity
                        BeehiveBlockEntity outerBeehiveBlockEntity = new BeehiveBlockEntity();
                        chunk.setBlockEntity(currBlockPos, outerBeehiveBlockEntity);
                        this.outerBeehiveBlockEntities.add(outerBeehiveBlockEntity);
                    } else if (d < shellDistance) {
                        // shell
                        if (random.nextInt(10) == 0) {
                            chunk.setBlockState(currBlockPos, Blocks.HONEY_BLOCK.getDefaultState(), false);
                        } else {
                            chunk.setBlockState(currBlockPos, Blocks.HONEYCOMB_BLOCK.getDefaultState(), false);
                        }
                    } else if (y-y2 == 0 && d > startRingDistance && d <= endRingDistance) {
                        chunk.setBlockState(currBlockPos, Blocks.GRASS_BLOCK.getDefaultState(), false);
                        int rand = random.nextInt(4);
                        if (rand == 0) {
                            chunk.setBlockState(currBlockPos.up(), ((BeeHiveSpheroidType) getSpheroidType()).getRandomFlower(random), false);
                        } else if (rand == 1) {
                            BlockState randomTallFlower = ((BeeHiveSpheroidType) getSpheroidType()).getRandomTallFlower(random);
                            chunk.setBlockState(currBlockPos.up(), randomTallFlower.with(TallPlantBlock.HALF, DoubleBlockHalf.LOWER), false);
                            chunk.setBlockState(currBlockPos.up(2), randomTallFlower.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), false);
                        }
                    }
                }
            }
        }

        this.setChunkFinished(chunk.getPos());
    }


    @Override
    public String getDescription() {
        return this.getSpheroidType().getDescription() +
                "\nPosition: x=" + this.getPosition().getX() + " y=" + this.getPosition().getY() + " z=" + this.getPosition().getZ() +
                "\nRadius: " + this.radius +
                "\nShellRadius: " + this.shellRadius +
                "\nFlowerRingRadius: " + this.flowerRingRadius +
                "\nFlowerRingSpacing: " + this.flowerRingSpacing;
    }

    @Override
    public boolean shouldPopulateEntities(ChunkPos chunkPos) {
        boolean ret = (chunkPos.getStartX() >= this.getPosition().getX()
                && chunkPos.getStartX() <= this.getPosition().getX() + 15
                && chunkPos.getStartZ() >= this.getPosition().getZ()
                && chunkPos.getStartZ() <= this.getPosition().getZ() + 15);
        if(ret) {
            return ret;
        }
        return ret;
    }

    @Override
    public void populateEntities(ChunkPos chunkPos, ChunkRegion chunkRegion, ChunkRandom chunkRandom) {

        if(queenBeehiveBlockEntity != null) {
            // queen
            BeeEntity queen = new BeeEntity(EntityType.BEE, chunkRegion.toServerWorld());
            setRandomQueenProperties(queen, chunkRandom);
            queenBeehiveBlockEntity.tryEnterHive(queen, false);
        }

        for(BeehiveBlockEntity beehiveBlockEntity : this.outerBeehiveBlockEntities) {
            // 2 bees
            BeeEntity bee1 = new BeeEntity(EntityType.BEE, chunkRegion.toServerWorld());
            beehiveBlockEntity.tryEnterHive(bee1, false);
            BeeEntity bee2 = new BeeEntity(EntityType.BEE, chunkRegion.toServerWorld());
            beehiveBlockEntity.tryEnterHive(bee2, false);
        }
    }


    public void setRandomQueenProperties(BeeEntity beeEntity, ChunkRandom chunkRandom) {
        beeEntity.setCustomName(new TranslatableText("bee.queen"));
        beeEntity.setHealth(beeEntity.getHealth() * (random.nextFloat()*3 + 5)); //way higher than default
        beeEntity.setMovementSpeed((float) (beeEntity.getMovementSpeed() * (random.nextFloat()* 0.5 + 0.5))); //slower than default
        beeEntity.setAbsorptionAmount((float) (beeEntity.getAbsorptionAmount() * (random.nextFloat()*1.5 + 1))); //higher than default

        StatusEffectInstance statusEffectInstance1 = new StatusEffectInstance(StatusEffects.HASTE, Integer.MAX_VALUE, 1);
        StatusEffectInstance statusEffectInstance2 = new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 3);
        StatusEffectInstance statusEffectInstance3 = new StatusEffectInstance(StatusEffects.REGENERATION, Integer.MAX_VALUE, 1);
        beeEntity.addStatusEffect(statusEffectInstance1);
        beeEntity.addStatusEffect(statusEffectInstance2);
        beeEntity.addStatusEffect(statusEffectInstance3);
        beeEntity.setGlowing(true);
        beeEntity.setAngerTime(Integer.MAX_VALUE);
    }



}
