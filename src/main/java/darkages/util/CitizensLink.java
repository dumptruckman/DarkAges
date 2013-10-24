package darkages.util;

import darkages.arena.ArenaMasterTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class CitizensLink {

    public static void registerTraits() {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ArenaMasterTrait.class).withName("arenamaster"));
    }
}
