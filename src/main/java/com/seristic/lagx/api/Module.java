package com.seristic.lagx.api;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;

public class Module {
   public static void registerHelp(String command, String help) {
      Help.addCommandH(new Help.HoverCommand(command, help, "module.permission", false));
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public final File getDataFolder() {
      return new File(LagX.modDir, LagX.getData(this)[0]);
   }

   public final Logger getLogger() {
      return LagX.getInstance().getLogger();
   }

   public boolean onCommand(CommandSender sender, String label, String[] args) {
      return false;
   }
}
