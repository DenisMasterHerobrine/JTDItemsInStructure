package dev.denismasterherobrine.jtditemsinstructure;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Optional;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(JTDItemsInStructures.MODID)
public class JTDItemsInStructures {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "jtditemsinstructure";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public JTDItemsInStructures() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        // Summon an entity at the player's location when the player have an specified item in hands and in the structure.
        // I will use the 'kubejs:hide_and_seek_ticket' item as an required item
        // I need to check for the `twilightforest:labyrith` structure in the player's location
        // I need to check to existing of the entity called `whatareyouvotingfor:rascal` in the player's location

        if (event.isCanceled()) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();

            // Get item from resource location
            // Get ResourceLocation
            ResourceKey<Item> itemKey = ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation("kubejs", "hide_and_seek_ticket"));

            // Check whether the key is existing and abort if not
            if (itemKey == null) {
                return;
            }

            // Check if the player is holding the item
            ResourceKey<Item> itemInHand = player.getMainHandItem().getItem().builtInRegistryHolder().key();

            // Check if the player is in the structure
            // Get ResourceLocation
            ResourceKey<Structure> structureResourceKey = ResourceKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation("twilightforest", "labyrinth"));

            // Check whether the key is existing and abort if not
            if (structureResourceKey == null) {
                return;
            }

            // Check if the player is in the structure using StructureManager.getStructureWithPieceAt
            if (player.getLevel().structureManager().getStructureWithPieceAt(player.blockPosition(), structureResourceKey).isValid()) {
                // Check if the player is holding the item
                if (itemInHand.equals(itemKey)) {
                    MinecraftServer server = player.getServer();

                    if (server != null) {
                        Level level = player.getLevel();

                        // Summon an entity using a command /summon

                        // Check for the gamerule sendCommandFeedback
                        if (level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                            // Set the gamerule to false
                            level.getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(false, level.getServer());
                            level.getServer().getCommands().performPrefixedCommand(server.createCommandSourceStack(), "/execute at " + player.getName().getString() + " run summon whatareyouvotingfor:rascal " + player.getX() + " " + (player.getY() + 0.6) + " " + player.getZ());
                            player.sendSystemMessage(Component.translatable("jtditemsinstructure.summoned_entity_rascal"));
                            // Set the gamerule back to true
                            level.getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(true, level.getServer());
                        } else {
                            level.getServer().getCommands().performPrefixedCommand(server.createCommandSourceStack(), "/execute at " + player.getName().getString() + " run summon whatareyouvotingfor:rascal " + player.getX() + " " + (player.getY() + 0.6) + " " + player.getZ());
                            player.sendSystemMessage(Component.translatable("jtditemsinstructure.summoned_entity_rascal"));
                        }

                        // Reduce the amount of the item in hand by 1
                        player.getMainHandItem().shrink(1);
                    }
                }
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
