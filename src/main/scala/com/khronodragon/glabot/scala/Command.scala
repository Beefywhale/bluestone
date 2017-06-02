package com.khronodragon.bluestone

import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import scala.reflect.runtime.universe.MethodMirror

class Command(cmdName: String, cmdDesc: String,
              cmdUsage: String = "", cmdHidden: Boolean = false,
              cmdNPerms: Array[String] = Array[String](), cmdNoPm: Boolean = false,
              cmdAliases: Array[String] = Array[String](), cmdCall: MethodMirror) {
    final val name: String = cmdName
    final val description: String = cmdDesc
    final val usage: String = cmdUsage
    final val hidden: Boolean = cmdHidden
    final val permsRequired: Array[String] = cmdNPerms
    final val noPm: Boolean = cmdNoPm
    final val aliases: Array[String] = cmdAliases
    final val function: MethodMirror = cmdCall

    def invoke(bot: Bot, event: MessageReceivedEvent, args: Array[String], prefix: String, invokedName: String): Unit = {
        val context = new Context(bot, event, args, prefix, invokedName)
        this.function(context)
    }
}
