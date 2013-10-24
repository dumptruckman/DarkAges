package darkages.arena;

import darkages.util.ImmutableLocation;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Arena implements ConfigurationSerializable {

    private Map<String, ImmutableLocation> locations = null;
    private ImmutableLocation respawnLocation = null;
    private String name = "";
    private String regionId = "";

    public Arena(String regionId, String name) {
        locations = new LinkedHashMap<String, ImmutableLocation>(10);
        this.regionId = regionId;
        this.name = name;
    }

    public Arena(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            try {
                if (entry.getKey().equals(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    continue;
                }
                Field field = getClass().getDeclaredField(entry.getKey());
                boolean access = field.isAccessible();
                field.setAccessible(true);
                field.set(this, entry.getValue());
                field.setAccessible(access);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (locations == null) {
            locations = new LinkedHashMap<String, ImmutableLocation>(10);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Field[] fields = getClass().getDeclaredFields();
        Map<String, Object> result = new HashMap<String, Object>(fields.length);
        for (Field field : fields) {
            if (field.getAnnotation(Transient.class) == null) {
                boolean access = field.isAccessible();
                field.setAccessible(true);
                try {
                    result.put(field.getName(), field.get(this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                field.setAccessible(access);
            }
        }
        return result;
    }

    @Nullable
    public ImmutableLocation getLocation(final String name) {
        return locations.get(name);
    }

    @NotNull
    public Collection<String> getLocations() {
        return locations.keySet();
    }

    public void setLocation(final String name, ImmutableLocation location) {
        locations.put(name, location);
    }

    public void clearLocations() {
        locations.clear();
    }

    public void setRespawnLocation(ImmutableLocation location) {
        this.respawnLocation = location;
    }

    public ImmutableLocation getRespawnLocation() {
        return this.respawnLocation;
    }

    public String getName() {
        return name;
    }

    public String getRegionId() {
        return regionId;
    }
}
