package darkages.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImmutableLocation extends Location implements ConfigurationSerializable {

    public static final ImmutableLocation ZERO_LOC = new ImmutableLocation(Bukkit.getWorlds().get(0), 0, 0, 0, 0, 0);

    public ImmutableLocation(final World world, final double x, final double y, final double z) {
        super(world, x, y, z);
    }

    public ImmutableLocation(final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public ImmutableLocation(final Location l) {
        super(l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }

    public static ImmutableLocation deserialize(Map<String, Object> data) {
        if (data.containsKey("world")
                && data.containsKey("x")
                && data.containsKey("y")
                && data.containsKey("z")
                && data.containsKey("yaw")
                && data.containsKey("pitch")) {
            try {
                World world = Bukkit.getWorld(UUID.fromString(data.get("world").toString()));
                double x = (Double) data.get("x");
                double y = (Double) data.get("y");
                double z = (Double) data.get("z");
                float yaw = ((Double) data.get("yaw")).floatValue();
                float pitch = ((Double) data.get("pitch")).floatValue();
                if (world != null) {
                    return new ImmutableLocation(world, x, y, z, yaw, pitch);
                }
            } catch (ClassCastException ignore) { }
        }
        return ZERO_LOC;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>(6);
        result.put("world", getWorld().getUID().toString());
        result.put("x", getX());
        result.put("y", getY());
        result.put("z", getZ());
        result.put("yaw", Float.valueOf(getYaw()).doubleValue());
        result.put("pitch", Float.valueOf(getPitch()).doubleValue());
        return result;
    }

    @Override
    public void setWorld(final World world) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setX(final double x) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setY(final double y) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setZ(final double z) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setYaw(final float yaw) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPitch(final float pitch) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location add(final Location vec) {
        return mutableCopy(this).add(vec);
    }

    @Override
    public Location add(final Vector vec) {
        return mutableCopy(this).add(vec);
    }

    @Override
    public Location add(final double x, final double y, final double z) {
        return mutableCopy(this).add(x, y, z);
    }

    @Override
    public Location subtract(final Location vec) {
        return mutableCopy(this).subtract(vec);
    }

    @Override
    public Location subtract(final Vector vec) {
        return mutableCopy(this).subtract(vec);
    }

    @Override
    public Location subtract(final double x, final double y, final double z) {
        return mutableCopy(this).subtract(x, y, z);
    }

    @Override
    public Location multiply(final double m) {
        return mutableCopy(this).multiply(m);
    }

    @Override
    public Location zero() {
        return mutableCopy(this).zero();
    }

    public static Location mutableCopy(ImmutableLocation l) {
        return new Location(l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }
}
