package toast.specialMobs;

import java.util.ArrayList;

import toast.specialMobs.entity.SpecialMobData;

/**
 * Stores all known keys for data watcher values on hostile entities.
 */
public class DataWatcherHelper
{
	/** Keys must be below the max number of keys ({@value}) and at least 0. */
	public static final byte MAX_KEYS = 32;

	public static DataWatcherHelper instance;

	public static void init() {
		DataWatcherHelper.instance = new DataWatcherHelper();
		DataWatcherHelper.instance.GENERIC.init();

		SpecialMobData.init();
	}
	public static void verify() {
		DataWatcherHelper.instance.GENERIC.verify();
		DataWatcherHelper.instance = null;
	}

	// Used by ISpecialMob - this is the root of all entity data
	public final EntityDataRoot GENERIC = new EntityDataRoot("specialmob",
			0, 1, // Entity
			6, 7, 8, 9, // EntityLivingBase
			10, 11 // EntityLiving
		).needs(1);

	// Used by the basic mob types
	public final EntityData BLAZE = new EntityData("blaze", this.GENERIC, 16);
	public final EntityData CAVE_SPIDER = new EntityData("cavespider", this.GENERIC);
	public final EntityData CREEPER = new EntityData("creeper", this.GENERIC, 16, 17, 18).needs(1);
	public final EntityData ENDERMAN = new EntityData("enderman", this.GENERIC, 16, 17, 18);
	public final EntityData GHAST = new EntityData("ghast", this.GENERIC, 16);
	public final EntityData PIG_ZOMBIE = new EntityData("pigzombie", this.GENERIC);
	public final EntityData SILVERFISH = new EntityData("silverfish", this.GENERIC);
	public final EntityData SKELETON = new EntityData("skeleton", this.GENERIC, 13).needs(1);
	public final EntityData SLIME = new EntityData("slime", this.GENERIC, 16);
	public final EntityData SPIDER = new EntityData("spider", this.GENERIC, 16);
	public final EntityData WITCH = new EntityData("witch", this.GENERIC, 21);
	public final EntityData ZOMBIE = new EntityData("zombie", this.GENERIC, 12, 13, 14);

	// Used by specific mobs
	public final EntityData PIG_ZOMBIE_FISHING = new EntityData("pigzombie-fishing", this.PIG_ZOMBIE).needs(1);
	public final EntityData SKELETON_NINJA = new EntityData("skeleton-ninja", this.SKELETON).needs(3);
	public final EntityData ZOMBIE_FISHING = new EntityData("zombie-fishing", this.ZOMBIE).needs(1);

	public static class EntityDataRoot extends EntityData {

		public EntityDataRoot(String id, int... initialKeys) {
			super(id + "(root)", initialKeys);
		}

		@Override
		public EntityDataRoot needs(int keys) {
			super.needs(keys);
			return this;
		}

		public void init() {
			this.generateNeededKeys(0);
		}

		public void verify() {
			this.verifyNeededKeys();
		}
	}

	public static class EntityData {

		private final String id;
		private final EntityData parent;
		private final ArrayList<EntityData> children = new ArrayList<EntityData>();

		// The hard-coded data watcher keys of all parents combined.
		private final int initialKeysUsed;
		// The data watcher keys which can not be added at this level (includes all built-in child keys).
		private int keysUsed;

		// The data watcher keys which have been generated at this level.
		private byte[] autoKeys;
		// The index of the last auto-generated key given.
		private byte autoKeyIndex = 0;

		public EntityData(String id, EntityData parent, int... initialKeys) {
			this.id = id;
			if (parent == null)
				throw new IllegalArgumentException("Error in " + this.id + " - Parent must be non-null!");

			this.parent = parent;
			parent.children.add(this);
			this.keysUsed = parent.initialKeysUsed;
			this.addKeys(initialKeys);
			this.initialKeysUsed = this.keysUsed;
		}

		// Returns the next generated key. This should be called the same number of times as the number of needed keys.
		public byte nextKey() {
			return this.autoKeys[this.autoKeyIndex++];
		}

		// Marks the number of keys to generate at this level.
		public EntityData needs(int keys) {
			this.autoKeys = new byte[keys];
			return this;
		}

		// Only used by the root constructor.
		protected EntityData(String id, int... initialKeys) {
			this.id = id;
			this.parent = null;
			this.keysUsed = 0;
			this.addKeys(initialKeys);
			this.initialKeysUsed = this.keysUsed;
		}

		private void addKeys(int... newKeys) {
			if (this.parent != null) {
				this.parent.addKeys(newKeys);
			}
			for (int key : newKeys) {
				this.keysUsed = this.addKey(this.keysUsed, (byte) key);
			}
		}

		protected void generateNeededKeys(int newKeys) {
			this.keysUsed |= newKeys; // Add keys already generated

			if (this.autoKeys != null) {
				int index = 0;
				for (byte key = 0; index < this.autoKeys.length && key < DataWatcherHelper.MAX_KEYS; key++) {
					if (this.isKeyUnused(key)) {
						newKeys = this.addKey(newKeys, key);
						this.autoKeys[index++] = key;
					}
				}
				if (index < this.autoKeys.length) {
					_SpecialMobs.debugException("Not enough data watcher keys available for " + this.id + "! (" + (this.autoKeys.length - index) + " more needed)");
				}
				this.keysUsed |= newKeys; // Add keys just generated
			}

			for (EntityData child : this.children) {
				child.generateNeededKeys(newKeys); // Pass all generated keys to each child
			}
		}

		private boolean isKeyUnused(byte key) {
			return (this.keysUsed & 1 << key) == 0;
		}
		private int addKey(int keys, byte newKey) {
			return keys | 1 << newKey;
		}

		// Throws a debug exception if this data has not had all of its keys used.
		protected void verifyNeededKeys() {
			if (this.autoKeys != null && this.autoKeyIndex < this.autoKeys.length) {
				_SpecialMobs.debugException("Too many data watcher keys requested for " + this.id + "! (" + (this.autoKeys.length - this.autoKeyIndex) + " extras)");
			}

			for (EntityData child : this.children) {
				child.verifyNeededKeys();
			}
		}
	}
}
