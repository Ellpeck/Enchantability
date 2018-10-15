package quarris.enchantability.mod.event;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import quarris.enchantability.mod.Enchantability;
import quarris.enchantability.mod.capability.player.CapabilityHandler;
import quarris.enchantability.mod.capability.player.container.EnchantItemHandler;
import quarris.enchantability.mod.capability.player.container.EnchantItemProvider;
import quarris.enchantability.mod.capability.player.container.IEnchantItemHandler;
import quarris.enchantability.mod.capability.player.enchant.IPlayerEnchHandler;
import quarris.enchantability.mod.capability.player.enchant.PlayerEnchHandler;
import quarris.enchantability.mod.capability.player.enchant.PlayerEnchProvider;
import quarris.enchantability.mod.container.gui.GuiEnchButton;
import quarris.enchantability.mod.network.PacketHandler;
import quarris.enchantability.mod.network.PacketSendCapsToClients;

public class ModEvents {

    @SubscribeEvent
    public void playerJoinWorld(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) e.getEntity();
            if (!e.getWorld().isRemote) {
                PacketHandler.INSTANCE.sendToAll(new PacketSendCapsToClients(player));
            }
        }
    }

    @SubscribeEvent
    public void attachCapabilityEntity(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) e.getObject();
            e.addCapability(new ResourceLocation(Enchantability.MODID, "enchant"), new PlayerEnchProvider(new PlayerEnchHandler(player)));
            e.addCapability(new ResourceLocation(Enchantability.MODID, "enchantInv"), new EnchantItemProvider(new EnchantItemHandler()));

        }
    }

    @SubscribeEvent
    public void cloneCapabilities(PlayerEvent.Clone e) {
        if (e.isWasDeath()) {
            try {
                IPlayerEnchHandler coEnch = e.getOriginal().getCapability(CapabilityHandler.PLAYER_ENCHANT_CAPABILITY, null);
                NBTTagList nbtEnch = coEnch.serializeNBT();
                IPlayerEnchHandler cnEnch = e.getEntityPlayer().getCapability(CapabilityHandler.PLAYER_ENCHANT_CAPABILITY, null);
                cnEnch.deserializeNBT(nbtEnch);

                IEnchantItemHandler coItem = e.getOriginal().getCapability(CapabilityHandler.ENCHANT_INVENTORY_CAPABILITY, null);
                NBTTagCompound nbtItem = coItem.serializeNBT();
                IEnchantItemHandler cnItem = e.getEntityPlayer().getCapability(CapabilityHandler.ENCHANT_INVENTORY_CAPABILITY, null);
                cnItem.deserializeNBT(nbtItem);
            } catch (Exception exp) {
                Enchantability.logger.warn("Failed to clone player " + e.getOriginal().getName(), exp);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void initGuiEnderChest(GuiScreenEvent.InitGuiEvent e) {
        if (e.getGui() instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) e.getGui();
            if (guiChest.inventorySlots instanceof ContainerChest) {
                ContainerChest containerChest = (ContainerChest) guiChest.inventorySlots;
                if (containerChest.getLowerChestInventory() instanceof InventoryBasic && !(containerChest.getLowerChestInventory() instanceof ContainerLocalMenu)) {
                    e.getButtonList().add(new GuiEnchButton(837259834, guiChest, guiChest.getGuiLeft() - 18, guiChest.getGuiTop() + 143, 0));
                }
            }
        }
    }
}