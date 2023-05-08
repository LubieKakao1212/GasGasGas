package com.LubieKakao1212.gasgas.event

import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@EventBusSubscriber
object RegisterCommands {

    @SubscribeEvent
    fun registerCommands(event : RegisterCommandsEvent) {
        GasCommand.register(event.dispatcher)
    }

}