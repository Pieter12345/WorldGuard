package woesh.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;

public abstract class WoeshUtils {
    
    private static boolean isCompatible = true;
    
    private static Method getHandleMethod;
    private static Field connectionField;
    private static Method aMethod;
    private static Constructor<?> packetPlayOutChatConstructor;
    private static Method sendPacketMethod = null;
    private static Object chatMessageTypeObj; // This can be (byte) 2 for MC versions prior to 1.12 or ChatMessageType.GAME_INFO for versions after 1.12.
    
    /**
     * sendActionBarMessage method.
     * Shows the message above the action bar of the given player. The message is sent through a direct Packet using NMS code.
     * @param player
     * @param message
     * @return {@code true} on success, {@code false} if something went wrong in the NMS code.
     */
    public static boolean sendActionBarMessage(LocalPlayer player, String message) {
        return player instanceof BukkitPlayer && sendActionBarMessage(((BukkitPlayer) player).getPlayer(), message);
    }
    
    /**
     * sendActionBarMessage method.
     * Shows the message above the action bar of the given player. The message is sent through a direct Packet using NMS code.
     * @param player
     * @param message
     * @return {@code true} on success, {@code false} if something went wrong in the NMS code.
     */
    public static boolean sendActionBarMessage(Player player, String message) {
        if(!isCompatible) {
            return false; // Errors won't disappear, let's just accept our loss.
        }
        try {
            
            // Initialize reflection objects (only runs once if successful).
            if(sendPacketMethod == null) {
            
                // Get the NMS EntityPlayer.
                getHandleMethod = player.getClass().getDeclaredMethod("getHandle");
                Object nmsPlayerObj = getHandleMethod.invoke(player);
                
                // Get the EntityPlayer.connection.
                connectionField = nmsPlayerObj.getClass().getDeclaredField("connection");
                Object playerConnectionObj = connectionField.get(nmsPlayerObj);
                
                // Get the IChatBaseComponent message creation Method.
                Class<?> chatSerializerClass = Class.forName(
                		"net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
                aMethod = chatSerializerClass.getDeclaredMethod("a", String.class);
                
                // Get the PacketPlayOutChat Constructor.
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
                Class<?> packetPlayOutChatClass = Class.forName(
                		"net.minecraft.network.protocol.game.PacketPlayOutChat");
                Class<?> chatMessageTypeClass = Class.forName("net.minecraft.network.chat.ChatMessageType");
                packetPlayOutChatConstructor = packetPlayOutChatClass.getDeclaredConstructor(
                		iChatBaseComponentClass, chatMessageTypeClass, UUID.class);
                chatMessageTypeObj = chatMessageTypeClass.getField("GAME_INFO").get(null);
                
                // Get the sendPacket method.
                Class<?> packetClass = Class.forName("net.minecraft.network.protocol.Packet");
                sendPacketMethod = playerConnectionObj.getClass().getDeclaredMethod("sendPacket", packetClass);
            }
            
            // Get the NMS EntityPlayer.
            Object nmsPlayerObj = getHandleMethod.invoke(player);
            
            // Get the EntityPlayer.playerConnection.
            Object playerConnectionObj = connectionField.get(nmsPlayerObj);
            
            // Construct the IChatBaseComponent object (message).
            Object iChatBaseComponentObj;
            try {
                iChatBaseComponentObj = aMethod.invoke(null,
                		"{\"text\": \"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
            } catch(InvocationTargetException e) {
                return false; // Invalid JSON format.
            }
            
            // Construct the PacketPlayOutChat.
            Object packetPlayOutObj = packetPlayOutChatConstructor.newInstance(
            		iChatBaseComponentObj, chatMessageTypeObj, new UUID(0, 0));
            
            // Send the packet.
            sendPacketMethod.invoke(playerConnectionObj, packetPlayOutObj);
            return true;
            
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | NoSuchFieldException | ClassNotFoundException | InstantiationException e) {
            System.out.println("[DEBUG] [" + WoeshUtils.class.getName() + "] An Exception occured while running NMS code to send an above-actionbar message. Here's the stacktrace:\n");
            e.printStackTrace();
            isCompatible = false;
            return false;
        }
    }
    
    /**
     * Replaces colorcodes in a string.
     * This is how WorldGuard used to colorize plot greetings and farewells before WorldGuard 7.0.0.
     * @param str - The string to colorize.
     * @return The new string
     */
    public static String replaceColorMacros(String str) {
        for(int ind = str.length() - 2; ind >= 0; ind--) {
            ChatColor color = null;
            if(str.charAt(ind) == '`') {
                switch(str.charAt(ind + 1)) {
                    case 'r': color = ChatColor.RED; break;
                    case 'R': color = ChatColor.DARK_RED; break;
                    case 'y': color = ChatColor.YELLOW; break;
                    case 'Y': color = ChatColor.GOLD; break;
                    case 'g': color = ChatColor.GREEN; break;
                    case 'G': color = ChatColor.DARK_GREEN; break;
                    case 'c': color = ChatColor.AQUA; break;
                    case 'C': color = ChatColor.DARK_AQUA; break;
                    case 'b': color = ChatColor.BLUE; break;
                    case 'B': color = ChatColor.DARK_BLUE; break;
                    case 'p': color = ChatColor.LIGHT_PURPLE; break;
                    case 'P': color = ChatColor.DARK_PURPLE; break;
                    case '0': color = ChatColor.BLACK; break;
                    case '1': color = ChatColor.DARK_GRAY; break;
                    case '2': color = ChatColor.GRAY; break;
                    case 'w': color = ChatColor.WHITE; break;
                    
                    case 'k': color = ChatColor.MAGIC; break;
                    case 'l': color = ChatColor.BOLD; break;
                    case 'm': color = ChatColor.STRIKETHROUGH; break;
                    case 'n': color = ChatColor.UNDERLINE; break;
                    case 'o': color = ChatColor.ITALIC; break;
                    case 'x': color = ChatColor.RESET; break;
                }
            } else if(str.charAt(ind) == '&') {
                switch(str.charAt(ind + 1)) {
                    case 'c': color = ChatColor.RED; break;
                    case '4': color = ChatColor.DARK_RED; break;
                    case 'e': color = ChatColor.YELLOW; break;
                    case '6': color = ChatColor.GOLD; break;
                    case 'a': color = ChatColor.GREEN; break;
                    case '2': color = ChatColor.DARK_GREEN; break;
                    case 'b': color = ChatColor.AQUA; break;
                    case '3': color = ChatColor.DARK_AQUA; break;
                    case '9': color = ChatColor.BLUE; break;
                    case '1': color = ChatColor.DARK_BLUE; break;
                    case 'd': color = ChatColor.LIGHT_PURPLE; break;
                    case '5': color = ChatColor.DARK_PURPLE; break;
                    case '0': color = ChatColor.BLACK; break;
                    case '8': color = ChatColor.DARK_GRAY; break;
                    case '7': color = ChatColor.GRAY; break;
                    case 'f': color = ChatColor.WHITE; break;
                    
                    case 'k': color = ChatColor.MAGIC; break;
                    case 'l': color = ChatColor.BOLD; break;
                    case 'm': color = ChatColor.STRIKETHROUGH; break;
                    case 'n': color = ChatColor.UNDERLINE; break;
                    case 'o': color = ChatColor.ITALIC; break;
                    case 'x': color = ChatColor.RESET; break;
                    case 'r': color = ChatColor.RESET; break;
                }
            }
            if(color != null) {
                str = str.substring(0, ind) + color.toString() + str.substring(ind + 2);
            }
        }
        return str;
    }
}
