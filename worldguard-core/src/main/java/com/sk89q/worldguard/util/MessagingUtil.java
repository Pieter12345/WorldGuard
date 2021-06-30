/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.util;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.CommandUtils;

public final class MessagingUtil {

    private MessagingUtil() {
    }

    public static void sendStringToChat(LocalPlayer player, String message) {
        String effective = CommandUtils.replaceColorMacros(message);
        effective = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(player, effective);
        for (String mess : effective.replaceAll("\\\\n", "\n").split("\\n")) {
            player.printRaw(mess);
        }
    }

    public static void sendStringToTitle(LocalPlayer player, String message) {
        String[] parts = message.replaceAll("\\\\n", "\n").split("\\n", 2);
        String title = CommandUtils.replaceColorMacros(parts[0]);
        title = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(player, title);
        if (parts.length > 1) {
            String subtitle = CommandUtils.replaceColorMacros(parts[1]);
            subtitle = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(player, subtitle);
            player.sendTitle(title, subtitle);
        } else {
            player.sendTitle(title, null);
        }
    }

    // WoeshEdit - Add method.
    public static void sendStringToActionbar(LocalPlayer player, String message) {
        String effective = CommandUtils.replaceColorMacros(message);
        effective = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(player, effective);
        final String finalMessage = effective.replaceAll("\\\\n", "\n");
        if (!player.sendActionbarMessage(finalMessage)) {
            for (String mess : finalMessage.split("\\n")) {
                player.printRaw(mess);
            }
        }
    }
}
