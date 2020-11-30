package de.dafuqs.starrysky.dimension;

import de.dafuqs.starrysky.StarrySkyCommon;
import de.dafuqs.starrysky.spheroid.lists.SpheroidListVanilla;
import de.dafuqs.starrysky.spheroid.spheroids.Spheroid;
import de.dafuqs.starrysky.spheroid.types.SpheroidType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkRandom;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SystemGenerator {

    public class TempPosition {
        int xPos;
        int yPos;
        int zPos;

        public BlockPos toBlockPos() {
            return new BlockPos(xPos, yPos, zPos);
        }

        private int distanceSquared(Spheroid pl1) {
            int xDist = xPos - pl1.getPosition().getX();
            int yDist = yPos - pl1.getPosition().getY();
            int zDist = zPos - pl1.getPosition().getZ();

            return xDist * xDist + yDist * yDist + zDist * zDist;
        }
    }

    // spawning probabilities
    private final HashMap<Point, List<Spheroid>> cache = new HashMap<>();
    public static SpheroidLoader spheroidLoader;

    public SystemGenerator() {
        spheroidLoader = new SpheroidLoader();
    }

    /**
     * System size of 50 results in system 0,0 at 0>+800, -1,0 at -800>0
     * @param chunkX X coordinate of chunk
     * @param chunkZ Z coordinate of chunk
     * @return the system point
     */
    private Point getSystemCoordinateFromChunkCoordinate(int chunkX, int chunkZ) {
        int sysX;
        if (chunkX >= 0) {
            sysX = chunkX / StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks;
        } else {
            sysX = (int) Math.floor(chunkX / (float) StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks);
        }

        int sysZ;
        if (chunkZ >= 0) {
            sysZ = chunkZ / StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks;
        } else {
            sysZ = (int) Math.floor(chunkZ / (float) StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks);
        }
        return new Point(sysX, sysZ);
    }

    /**
     * Returns the system at the goven chunk coordinates
     * If a system does not exist yet it will be generated
     * @param chunkX chunk chunkX location
     * @param chunkZ chunk chunkZ location
     * @return List of planetoids representing the system this chunk is in
     */
    public List<Spheroid> getSystemAtChunkPos(int chunkX, int chunkZ) {
        //check if the system of this chunk is cached
        Point systemPos = getSystemCoordinateFromChunkCoordinate(chunkX, chunkZ);
        List<Spheroid> curSystem = cache.get(systemPos);

        if (curSystem == null) {
            //doesn't exist. Generate new system and cache it
            curSystem = generatePlanetsAtSystemPosition(systemPos);
            cache.put(systemPos, curSystem);
        }

        return curSystem;
    }


    private ChunkRandom getSystemRandom(Point systemPoint) {
        int firstChunkPosX = systemPoint.x * StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks;
        int firstChunkPosZ = systemPoint.y * StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks;
        ChunkRandom systemRandom = new ChunkRandom(StarrySkyCommon.starryWorld.getSeed());
        systemRandom.setTerrainSeed(firstChunkPosX, firstChunkPosZ); // and the seed from the first chunk+
        StarrySkyCommon.LOGGER.log(Level.DEBUG, "[StarrySky] Generated seed for system at " + systemPoint.x + "," + systemPoint.y + "(first chunk: " + firstChunkPosX + "," + firstChunkPosZ);
        return systemRandom;
    }


    private List<Spheroid> generatePlanetsAtSystemPosition(Point systemPoint) {
        int systemPointX = systemPoint.x;
        int systemPointZ = systemPoint.y;

        ChunkRandom systemRandom = getSystemRandom(systemPoint);
        List<Spheroid> spheroids = new ArrayList<Spheroid>();

        //If systemPointX and Z are zero, generate a log/leaf planet at 16, 16
        if (systemPointX == 0 && systemPointZ == 0) {
            Spheroid homeSpheroid = SpheroidListVanilla.OAK_WOOD.getRandomSphere(systemRandom);
            homeSpheroid.setPositionAndCalculateGenerationChunks(new BlockPos(16, 70, 16));
            spheroids.add(homeSpheroid);
        }

        // try to create DENSITY planets in system
        int worldHeight = StarrySkyCommon.starryWorld.getHeight();
        for (int currentDensity = 0; currentDensity < StarrySkyCommon.STARRY_SKY_CONFIG.sphereDensity; currentDensity++) {

            // create new planets
            Spheroid currentSpheroid = getRandomSpheroid(systemRandom);
            TempPosition tempPosition = new TempPosition();

            // set position, check bounds with system edges
            tempPosition.xPos = -1;
            while (tempPosition.xPos == -1) {
                int curTry = systemRandom.nextInt(StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks * 16);
                if (curTry + currentSpheroid.getRadius() < StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks * 16 && curTry - currentSpheroid.getRadius() >= 0) {
                    tempPosition.xPos = curTry;
                }
            }
            tempPosition.xPos += StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks * 16 * systemPointX;

            tempPosition.yPos = systemRandom.nextInt(worldHeight - currentSpheroid.getRadius() * 2 - StarrySkyCommon.STARRY_SKY_CONFIG.floorHeight) + currentSpheroid.getRadius() + StarrySkyCommon.STARRY_SKY_CONFIG.floorHeight;

            tempPosition.zPos = -1;
            while (tempPosition.zPos == -1) {
                int curTry = systemRandom.nextInt(StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks * 16);
                if (curTry + currentSpheroid.getRadius() < StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks * 16 && curTry - currentSpheroid.getRadius() >= 0) {
                    tempPosition.zPos = curTry;
                }
            }
            tempPosition.zPos += StarrySkyCommon.STARRY_SKY_CONFIG.systemSizeChunks * 16 * systemPointZ;

            // check for collisions with existing spheroids
            // if any collision, discard it
            boolean discard = false;
            for (Spheroid pl : spheroids) {
                //each spheroid has to be at least pl1.radius + pl2.radius + min distance apart
                int distMin = pl.getRadius() + currentSpheroid.getRadius() + StarrySkyCommon.STARRY_SKY_CONFIG.minDistanceBetweenSpheres;
                if (tempPosition.distanceSquared(pl) < distMin * distMin) {
                    discard = true;
                    break;
                }
            }
            if (!discard) {
                BlockPos finalSpheroidBlockPos = tempPosition.toBlockPos();
                currentSpheroid.setPositionAndCalculateGenerationChunks(finalSpheroidBlockPos);

                // add it to the list
                spheroids.add(currentSpheroid);
            }
        }
        StarrySkyCommon.LOGGER.log(Level.INFO, "[StarrySky] Created a new system with " + spheroids.size() + " spheroids at system position " + systemPointX + "," + systemPointZ);
        return spheroids;
    }

    private Spheroid getRandomSpheroid(ChunkRandom systemRandom) {
        SpheroidType spheroidType = spheroidLoader.getWeightedRandomSpheroid(systemRandom);
        StarrySkyCommon.LOGGER.log(Level.DEBUG, "[StarrySky] Created a new sphere of type " + spheroidType);
        return spheroidType.getRandomSphere(systemRandom);
    }


}
