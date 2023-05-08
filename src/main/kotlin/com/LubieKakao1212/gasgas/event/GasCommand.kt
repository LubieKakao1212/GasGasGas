package com.LubieKakao1212.gasgas.event

import com.LubieKakao1212.gasgas.GasGas
import com.LubieKakao1212.gasgas.gasses.Node
import com.LubieKakao1212.gasgas.gasses.calculateZone
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.commands.CommandRuntimeException
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

object GasCommand {

    val currentZone = mutableListOf<Node>()

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("gas")
                .then(Commands.literal("spawn")
                    .then(Commands.argument("source", BlockPosArgument.blockPos())
                        .then(Commands.argument("size", IntegerArgumentType.integer(0))
                            .executes {
                                spawnGas(it.source.level, BlockPosArgument.getLoadedBlockPos(it, "source"), IntegerArgumentType.getInteger(it, "size"))
                                0
                            }
                        )
                    )
                )
                .then(Commands.literal("debug")
                    .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .then(Commands.argument("showPath", BoolArgumentType.bool())
                            .then(Commands.argument("showBends", BoolArgumentType.bool())
                                .executes {
                                    debug(it.source.level, IntegerArgumentType.getInteger(it, "index"), BoolArgumentType.getBool(it, "showPath"), BoolArgumentType.getBool(it, "showBends"))
                                    0
                                }
                            ).executes {
                                debug(it.source.level, IntegerArgumentType.getInteger(it, "index"), BoolArgumentType.getBool(it, "showPath"), false)
                                0
                            }
                        )
                    )
                    .then(Commands.literal("all")
                        .then(Commands.literal("clear")
                            .executes {
                                debugAllClear(it.source.level)
                                0
                            }
                        )
                        .executes {
                            debugAll(it.source.level)
                            0
                        }
                    )
                )
        )
    }

    private fun spawnGas(level : Level, source : BlockPos, size : Int) {
        currentZone.clear()
        currentZone.addAll(calculateZone(level, source, size.toUInt()))
    }

    private fun debug(level : Level, index : Int, showPath : Boolean, showBends : Boolean) {
        if(level.isClientSide) {
            GasGas.LOGGER.warn("Client")
            return
        }

        if(currentZone.size <= index) {
            throw CommandRuntimeException(TextComponent("Index out of bounds $index, element count ${currentZone.size}"))
        }

        val node = currentZone[index]

        if(showPath) {
            setBlocks(level, node.chain) { Blocks.GLASS.defaultBlockState() }
        }else {
            setBlocks(level, node.chain) { Blocks.AIR.defaultBlockState() }
        }

        if(showBends) {
            setBlocks(level, node.bendChain) { Blocks.LIME_STAINED_GLASS.defaultBlockState() }
        }
    }

    private fun debugAll(level : Level) {
        if(level.isClientSide) {
            GasGas.LOGGER.warn("Client")
            return
        }

        setBlocks(level, currentZone) {
            if(it.lastBend === it.previous)
                Blocks.LIME_STAINED_GLASS.defaultBlockState()
            else
                Blocks.GLASS.defaultBlockState()
        }
    }

    private fun debugAllClear(level : Level) {
        if(level.isClientSide) {
            GasGas.LOGGER.warn("Client")
            return
        }

        setBlocks(level, currentZone) {
            Blocks.AIR.defaultBlockState()
        }
    }

    private fun setBlocks(level : Level, nodes : List<Node>, block : (Node) -> BlockState) {
        for(link in nodes) {
            level.setBlock(link.pos, block(link), 3)
        }
    }

}