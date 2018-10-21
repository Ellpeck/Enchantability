package quarris.enchantability.api.enchant;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class AbstractEnchantEffect implements IEnchantEffect {

    @Override
    public void onLootTableFillInventory(EntityPlayer player, LootTable table, IInventory inv, Random rand, LootContext context) {

    }

    @Override
    public void onItemCrafted(EntityPlayer player, ItemStack output, int tier) {

    }

    @Override
    public void onLivingUpdate(EntityPlayer player, int tier) {

    }

    @Override
    public float onPlayerHurt(EntityPlayer player, DamageSource source, float amount, int tier) {
        return amount;
    }

    @Override
    public boolean onPlayerDeath(EntityPlayer player, int tier) {
        return false;
    }

    @Override
    public void onTick(EntityPlayer player, int tier) {
    }

    @Override
    public boolean onPlayerAttack(EntityPlayer player, Entity target, int tier) {
        return true;
    }

    @Override
    public float onPlayerDamageTaken(EntityPlayer player, DamageSource source, float amount, int tier) {
        return amount;
    }

    @Override
    public float breakSpeed(EntityPlayer player, IBlockState state, BlockPos pos, float originalSpeed, int tier) {
        return originalSpeed;
    }

    @Override
    public boolean onRenderPlayer(EntityPlayer player, RenderPlayer render, int tier) {
        return false;
    }

    @Override
    public void onRemoved(EntityPlayer player, int tier) {
    }

    @Override
    public void onAdded(EntityPlayer player, int tier) {

    }

    @Nonnull
    @Override
    public abstract Enchantment getEnchantment();
}
