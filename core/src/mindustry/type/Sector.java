package mindustry.type;

import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.graphics.PlanetGrid.*;
import mindustry.world.*;

/** A small section of a planet. */
public class Sector{
    public final SectorRect rect;
    public final Planet planet;
    public final Ptile tile;
    public final int id;

    public final SectorData data;
    //TODO implement a dynamic (?) launch period
    public int launchPeriod = 10;

    public Sector(Planet planet, Ptile tile, SectorData data){
        this.planet = planet;
        this.tile = tile;
        this.rect = makeRect();
        this.id = tile.id;
        this.data = data;
    }

    //TODO implement
    public boolean isLaunchWave(int wave){
        return metCondition() && wave % launchPeriod == 0;
    }

    public boolean metCondition(){
        //TODO implement
        return false;
    }

    /** Projects this sector onto a 4-corner square for use in map gen.
     * Allocates a new object. Do not call in the main loop. */
    private SectorRect makeRect(){
        Vec3[] corners = new Vec3[tile.corners.length];
        for(int i = 0; i < corners.length; i++){
            corners[i] = tile.corners[i].v.cpy().setLength(planet.radius);
        }

        Tmp.v33.setZero();
        for(Vec3 c : corners){
            Tmp.v33.add(c);
        }
        //v33 is now the center of this shape
        Vec3 center = Tmp.v33.scl(1f / corners.length).cpy();
        //radius of circle
        float radius = Tmp.v33.dst(corners[0]) * 0.9f;

        //get plane that these points are on
        Plane plane = new Plane();
        plane.set(corners[0], corners[2], corners[4]);

        //relative vectors
        Vec3 planeTop = plane.project(center.cpy().add(0f, 1f, 0f)).sub(center).setLength(radius);
        Vec3 planeRight = plane.project(center.cpy().rotate(Vec3.Y, -4f)).sub(center).setLength(radius);

        //get angle from first corner to top vector
        Vec3 first = corners[1].cpy().sub(center); //first vector relative to center
        float angle = first.angle(planeTop);

        return new SectorRect(radius, center, planeTop, planeRight, angle);
    }

    public static class SectorRect{
        public final Vec3 center, top, right;
        public final Vec3 result = new Vec3();
        public final float radius, rotation;

        public SectorRect(float radius, Vec3 center, Vec3 top, Vec3 right, float rotation){
            this.center = center;
            this.top = top;
            this.right = right;
            this.radius = radius;
            this.rotation = rotation;
        }

        /** Project a coordinate into 3D space.
         * Both coordinates should be normalized to floats in the range [0, 1] */
        public Vec3 project(float x, float y){
            float nx = (x - 0.5f) * 2f, ny = (y - 0.5f) * 2f;
            return result.set(center).add(right, nx).add(top, ny);
        }
    }

    /** Cached data about a sector. */
    public static class SectorData{
        public Content[] resources = {};
        public int spawnX, spawnY;

        public Block[] floors = {};
        public int[] floorCounts = {};

        public void write(Writes write){
            write.s(resources.length);
            for(Content resource : resources){
                write.b(resource.getContentType().ordinal());
                write.s(resource.id);
            }
            write.s(spawnX);
            write.s(spawnY);
            write.s(floors.length);
            for(int i = 0; i < floors.length; i++){
                write.s(floors[i].id);
                write.i(floorCounts[i]);
            }
        }

        public void read(Reads read){
            resources = new Content[read.s()];
            for(int i = 0; i < resources.length; i++){
                resources[i] = Vars.content.getByID(ContentType.all[read.b()], read.s());
            }
            spawnX = read.s();
            spawnY = read.s();
            floors = new Block[read.s()];
            floorCounts = new int[floors.length];
            for(int i = 0; i < floors.length; i++){
                floors[i] = Vars.content.block(read.s());
                floorCounts[i] = read.i();
            }
        }
    }
}