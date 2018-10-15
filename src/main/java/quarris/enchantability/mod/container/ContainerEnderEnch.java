package quarris.enchantability.mod.container;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import quarris.enchantability.mod.capability.player.CapabilityHandler;
import quarris.enchantability.mod.capability.player.container.IEnchantItemHandler;
import quarris.enchantability.mod.capability.player.enchant.IPlayerEnchHandler;
import quarris.enchantability.mod.network.PacketHandler;
import quarris.enchantability.mod.network.PacketSendCapsToClients;

import java.util.Objects;

public class ContainerEnderEnch extends Container {

    public final IEnchantItemHandler inv;
    public final InventoryPlayer playerInv;
    public final InventoryEnderChest enderInv;
    private final EntityPlayer player;

    public ContainerEnderEnch(InventoryPlayer playerInv, InventoryEnderChest enderInv, EntityPlayer player) {
        this.player = player;
        this.playerInv = playerInv;
        this.enderInv = enderInv;
        this.inv = player.getCapability(CapabilityHandler.ENCHANT_INVENTORY_CAPABILITY, null);

        // Enchant Inv Slots
        for (int slot = 0; slot < Objects.requireNonNull(this.inv).getSlots(); slot++) {
            this.addSlotToContainer(new SlotEnchant(player, this.inv, slot, 8, 18 + slot * 18));
        }

        // Ender Inv Slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(enderInv, col + row * 9, 33 + col * 18, 18 + row * 18));
            }
        }

        // Player Inv Slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInv, 9 + row * 9 + col, 33 + col * 18, 85 + row * 18));
            }
        }
        // Hotbar slots
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlotToContainer(new Slot(playerInv, hotbarSlot, 33 + hotbarSlot * 18, 143));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();
            if (index < containerSlots) {
                if (!this.mergeItemStack(itemstack1, containerSlots, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, containerSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    private void updateEnchants() {
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            NBTTagList enchants = stack.serializeNBT().getCompoundTag("tag").getTagList("StoredEnchantments", 10);
            if (!stack.isEmpty() && enchants.tagCount() == 1) {
                IPlayerEnchHandler cap = player.getCapability(CapabilityHandler.PLAYER_ENCHANT_CAPABILITY, null);
                if (cap == null) return;
                Enchantment ench = Enchantment.getEnchantmentByID(enchants.getCompoundTagAt(0).getShort("id"));
                int level = enchants.getCompoundTagAt(0).getShort("lvl");
                if (level > 0 && cap.hasEnchant(ench) < level); {
                    cap.addEnchant(ench, level);
                    PacketHandler.INSTANCE.sendToAll(new PacketSendCapsToClients(player));
                }
            }
        }
    }
}
