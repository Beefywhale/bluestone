package com.khronodragon.bluestone.cogs;

import com.khronodragon.bluestone.Bot;
import com.khronodragon.bluestone.Cog;
import com.khronodragon.bluestone.Context;
import com.khronodragon.bluestone.Emotes;
import com.khronodragon.bluestone.annotations.Command;
import com.khronodragon.bluestone.errors.PermissionError;
import com.khronodragon.bluestone.sql.BotAdmin;
import com.khronodragon.bluestone.sql.GuildPrefix;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static com.khronodragon.bluestone.util.NullValueWrapper.val;

public class AdminCog extends Cog {
    private static final String[] ADMIN_PERM = {"admin"};
    private static final String[] PREFIX_MOD_PERMS = {"manageServer", "manageChannel", "messageManage"};
    private static final String ADMIN_NO_COMMAND = "🤔 **I need an action!**\n" +
            "The following are valid:\n" +
            "    \u2022 `test` - test if you're an admin\n" +
            "    \u2022 `list` - list current admins\n" +
            "    \u2022 `add [mention or id]` - add an admin\n" +
            "    \u2022 `remove [mention or id]` - remove an admin\n" +
            "\n" +
            "**__NOTE: This has nothing to do with *server* admins!__**\n" +
            "I will not help you if you try adding an admin with this, and discover that it \"doesn't work\".\n" +
            "It shouldn't. This is for bot-wide admins that have extra powers.";

    public AdminCog(Bot bot) {
        super(bot);
    }

    public String getName() {
        return "Admin";
    }

    public String getDescription() {
        return "Everything admin!";
    }

    @Command(name = "admin", desc = "Manage bot admins.", aliases = {"admins"}, thread = true)
    public void groupAdmin(Context ctx) throws SQLException, PermissionError {
        if (ctx.rawArgs.length() < 1) {
            ctx.send(ADMIN_NO_COMMAND).queue();
            return;
        }
        String invoked = ctx.args.get(0);

        if (invoked.equals("list"))
            adminCmdList(ctx);
        else if (invoked.equals("add"))
            adminCmdAdd(ctx);
        else if (invoked.equals("remove"))
            adminCmdRemove(ctx);
        else if (invoked.equals("test"))
            adminCmdTest(ctx);
        else
            ctx.send(ADMIN_NO_COMMAND).queue();
    }

    private void adminCmdTest(Context ctx) throws SQLException {
        String admin = bot.getAdminDao().idExists(ctx.author.getIdLong()) ? " 👋 Hey there, admin!" : " 😢 You aren't an admin!";
        ctx.send(ctx.mention + admin).queue();
    }

    private void adminCmdList(Context ctx) throws SQLException {
        List<String> adminList = bot.getAdminDao().queryForAll().stream()
                .map(a -> val(a.getLastUsername()).or("Unknown") + " (`" + a.getUserId() + "`)")
                .collect(Collectors.toList());

        if (adminList.size() > 0)
            ctx.send("**Current bot admins:**\n    \u2022 " + String.join("\n    \u2022 ", adminList)).queue();
        else
            ctx.send(Emotes.getFailure() + " There are no admins!").queue();
    }

    private void adminCmdAdd(Context ctx) throws SQLException, PermissionError {
        com.khronodragon.bluestone.Command.checkPerms(ctx, ADMIN_PERM);

        if (ctx.args.size() != 2) {
            ctx.send(Emotes.getFailure() + " I need a mention or user ID!").queue();
            return;
        }
        String input = ctx.args.get(1);
        long userId;
        String username = null;

        if (ctx.message.getMentionedUsers().size() == 1) {
            User user = ctx.message.getMentionedUsers().get(0);
            userId = user.getIdLong();
            username = user.getName();
        } else {
            try {
                userId = MiscUtil.parseSnowflake(input);
            } catch (NumberFormatException e) {
                ctx.send(Emotes.getFailure() + " Invalid user ID!").queue();
                return;
            }

            if (!bot.isSelfbot()) {
                username = ctx.jda.retrieveUserById(userId).complete().getName();
            }
        }

        BotAdmin adminObj = new BotAdmin(userId, username);
        bot.getAdminDao().createOrUpdate(adminObj);

        ctx.send(Emotes.getSuccess() + " User added/updated.").queue();
    }

    private void adminCmdRemove(Context ctx) throws SQLException, PermissionError {
        com.khronodragon.bluestone.Command.checkPerms(ctx, ADMIN_PERM);

        if (ctx.args.size() != 2) {
            ctx.send(Emotes.getFailure() + " I need a mention or user ID!").queue();
            return;
        }
        String input = ctx.args.get(1);
        long userId;

        if (ctx.message.getMentionedUsers().size() == 1) {
            userId = ctx.message.getMentionedUsers().get(0).getIdLong();
        } else {
            try {
                userId = MiscUtil.parseSnowflake(input);
            } catch (NumberFormatException e) {
                ctx.send(Emotes.getFailure() + " Invalid user ID!").queue();
                return;
            }
        }

        bot.getAdminDao().deleteById(userId);

        ctx.send(Emotes.getSuccess() + " User removed.").queue();
    }

    @Command(name = "prefix", desc = "Get or set the command prefix.", aliases = {"setprefix"}, guildOnly = true)
    public void cmdPrefix(Context ctx) throws SQLException, PermissionError {
        if (ctx.rawArgs.length() > 0) {
            com.khronodragon.bluestone.Command.checkPerms(ctx, PREFIX_MOD_PERMS);

            if (ctx.rawArgs.length() > 32) {
                ctx.send(Emotes.getFailure() + " Prefix too long!").queue();
            } else {
                String rawPrefix = ctx.rawArgs;
                if (rawPrefix.equals(ctx.guild.getSelfMember().getAsMention())) {
                    rawPrefix += ' ';
                }

                GuildPrefix prefix = new GuildPrefix(ctx.guild.getIdLong(), rawPrefix);
                bot.getPrefixDao().createOrUpdate(prefix);
                bot.getShardUtil().getPrefixStore().updateCache(ctx.guild.getIdLong(), rawPrefix);

                ctx.send(Emotes.getSuccess() + " Prefix set.").queue();
            }
        } else {
            ctx.send("**Prefix:** `" + ctx.prefix + "`").queue();
        }
    }
}