package quarris.enchantability.mod.capability.player.enchant;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.Pair;
import quarris.enchantability.api.enchant.EnchantEffectRegistry;
import quarris.enchantability.api.enchant.IEnchantEffect;
import quarris.enchantability.mod.Enchantability;
import quarris.enchantability.mod.capability.player.CapabilityHandler;
import quarris.enchantability.mod.capability.player.container.EnchantItemHandler;
import quarris.enchantability.mod.capability.player.container.IEnchantItemHandler;

import java.util.ArrayList;
import java.util.List;

public class PlayerEnchHandler implements IPlayerEnchHandler {

    protected EntityPlayer player;
    protected boolean dirty = true;
    protected List<Pair<Enchantment, Integer>> enchants;

    public PlayerEnchHandler() {
        this(null);
    }

    public PlayerEnchHandler(EntityPlayer player) {
        this.player = player;
        this.enchants = new ArrayList<>();
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public List<Pair<Enchantment, Integer>> getEnchants() {
        return enchants;
    }

    @Override
    public boolean hasEnchant(Enchantment enchant, int tier) {
        for (Pair<Enchantment, Integer> pair : this.enchants) {
            if (pair.getLeft().equals(enchant) && pair.getRight().equals(tier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hasEnchant(Enchantment enchant) {
        for (Pair<Enchantment, Integer> pair : this.enchants) {
            if (pair.getLeft().equals(enchant)) {
                return pair.getRight();
            }
        }
        return 0;
    }

    @Override
    public void addEnchant(Enchantment enchant, int tier) {
        if (enchant == null) {
            Enchantability.logger.warn("Tried adding enchant which does not exist.");
            return;
        }
        if (tier < 1 || tier > enchant.getMaxLevel()) {
            Enchantability.logger.warn("Tried adding enchant to the player with an invalid tier for that enchant. Enchant: " + enchant.getName() + ", Tier: " + tier);
            return;
        }
        for (Pair<Enchantment, Integer> pair : this.enchants) {
            if (pair.getLeft().equals(enchant)) {
                if (tier < pair.getRight()) {
                    this.enchants.remove(pair);
                    break;
                }
            }
        }
        List<IEnchantEffect> effects = EnchantEffectRegistry.getEffectsFromEnchantment(enchant);
        for (IEnchantEffect effect : effects) {
            effect.onAdded(player, tier);
        }
        this.enchants.add(Pair.of(enchant, tier));
        markDirty();
    }

    @Override
    public void removeEnchant(Enchantment enchant) {
        for (Pair<Enchantment, Integer> pair : this.enchants) {
            if (pair.getLeft().equals(enchant)) {
                List<IEnchantEffect> effects = EnchantEffectRegistry.getEffectsFromEnchantment(pair.getLeft());
                for (IEnchantEffect effect : effects) {
                    effect.onRemoved(player, pair.getRight());
                }
                this.enchants.remove(pair);
                markDirty();
                IEnchantItemHandler cap = player.getCapability(CapabilityHandler.ENCHANT_INVENTORY_CAPABILITY, null);
                for (int i = 0; i < EnchantItemHandler.ENCH_SLOTS; i++) {
                    ItemStack stack = cap.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        NBTTagList enchants = stack.serializeNBT().getCompoundTag("tag").getTagList("StoredEnchantments", 10);
                        NBTTagCompound ench = enchants.getCompoundTagAt(i);
                        if (Enchantment.getEnchantmentByID(ench.getShort("id")).equals(enchant) && ench.getShort("lvl") == pair.getRight()) {
                            cap.setStackInSlot(i, ItemStack.EMPTY);
                            return;
                        }
                    }
                }
                return;
            }
        }
    }

    @Override
    public void clearEnchants() {
        for (Pair<Enchantment, Integer> pair : this.enchants) {
            List<IEnchantEffect> effects = EnchantEffectRegistry.getEffectsFromEnchantment(pair.getLeft());
            for (IEnchantEffect effect : effects) {
                effect.onRemoved(player, pair.getRight());
            }
        }
        this.enchants.clear();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public void markClean() {
        dirty = false;
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList enchantList = new NBTTagList();
        for (Pair<Enchantment, Integer> enchant : enchants) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("enchant", enchant.getLeft().getRegistryName().toString());
            compound.setInteger("tier", enchant.getRight());
            enchantList.appendTag(compound);
        }
        return enchantList;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        clearEnchants();
        for (NBTBase base : nbt) {
            NBTTagCompound compound = (NBTTagCompound) base;
            this.addEnchant(Enchantment.getEnchantmentByLocation(compound.getString("enchant")), compound.getInteger("tier"));
        }
    }
}
