package toast.specialMobs;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public abstract class RandomHelper
{
    // The total weights for each MONSTER_KEY.
    public static final int[] totalMonsterWeights = RandomHelper.buildTotalMonsterWeights();

    // Produces a random monster based on the monster key given.
    public static EntityLiving nextMonster(int key, World world) {
        return RandomHelper.nextEntity(key, world, "monster", _SpecialMobs.MONSTER_KEY[key], _SpecialMobs.MONSTER_TYPES[key], Properties.monsterWeights()[key], RandomHelper.totalMonsterWeights[key]);
    }

    // Produces a random mob based on the info given, or null if the mob should not be replaced.
    public static EntityLiving nextEntity(int key, World world, String category, String mobKey, String[] types, int[] weights, int totalWeight) {
        int choice = _SpecialMobs.random.nextInt(totalWeight);
        for (int i = weights.length; i-- > 0;) {
            choice -= weights[i];
            if (choice < 0) {
                if (i == 0)
                    return Properties.monsterVanilla()[key] ? null : (EntityLiving)EntityList.createEntityByName("SpecialMobs.Special" + mobKey, world);
                i--; /// Adjusts for the +1 offset of weights[].
                return (EntityLiving)EntityList.createEntityByName("SpecialMobs." + types[i] + mobKey, world);
            }
        }
        _SpecialMobs.debugException("Weighting error: " + category + " (" + key + ")!");
        return null;
    }

    // Builds the totalMonsterWeights[] variable automatically.
    private static int[] buildTotalMonsterWeights() {
        int[] totalWeights = new int[_SpecialMobs.MONSTER_KEY.length];
        int[][] weights = Properties.monsterWeights();
        for (int i = weights.length; i-- > 0;) {
            for (int j = weights[i].length; j-- > 0;) {
                totalWeights[i] += weights[i][j];
            }
        }
        return totalWeights;
    }
}