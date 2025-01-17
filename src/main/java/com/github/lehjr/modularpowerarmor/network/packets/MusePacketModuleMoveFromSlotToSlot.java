package com.github.lehjr.modularpowerarmor.network.packets;

import com.github.lehjr.modularpowerarmor.containers.TinkerTableContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MusePacketModuleMoveFromSlotToSlot {
    int windowId;
    int sourceSlot;
    int targetSlot;

    public MusePacketModuleMoveFromSlotToSlot(int windowIdIn, int sourceSlotIn, int targetSlotIn) {
        this.windowId = windowIdIn;
        this.sourceSlot = sourceSlotIn;
        this.targetSlot = targetSlotIn;
    }

    public static void encode(MusePacketModuleMoveFromSlotToSlot msg, PacketBuffer packetBuffer) {
        packetBuffer.writeInt(msg.windowId);
        packetBuffer.writeInt(msg.sourceSlot);
        packetBuffer.writeInt(msg.targetSlot);
    }

    public static MusePacketModuleMoveFromSlotToSlot decode(PacketBuffer packetBuffer) {
        return new MusePacketModuleMoveFromSlotToSlot(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt());
    }

    public static void handle(MusePacketModuleMoveFromSlotToSlot message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player.openContainer != null && player.openContainer.windowId == message.windowId && player.openContainer instanceof TinkerTableContainer) {
                TinkerTableContainer container = (TinkerTableContainer) player.openContainer;

                Slot source = container.getSlot(message.sourceSlot);
                Slot target = container.getSlot(message.targetSlot);

                ItemStack itemStack = source.getStack();
                ItemStack stackCopy = itemStack.copy();
                // fixme: no idea if this will work with target set as range
                if (source instanceof CraftingResultSlot && source.getHasStack()) {
                    container.consume(player);
                    if (container.mergeItemStack(itemStack, message.targetSlot, message.targetSlot + 1, false)) {
//                        source.onSlotChange(itemStack, stackCopy);
                        source.onTake(player, itemStack);
                    }
                } else {
                    target.putStack(stackCopy);
                    source.decrStackSize(1);
                }
                player.openContainer.detectAndSendChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}