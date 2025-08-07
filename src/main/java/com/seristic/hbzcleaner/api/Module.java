package com.seristic.hbzcleaner.api;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;

import com.seristic.hbzcleaner.inf.Help;
import com.seristic.hbzcleaner.main.LaggRemover;

/* loaded from: LaggRemover-2.0.6.jar:drew6017/lr/api/Module.class */
public class Module {
    public static void registerHelp(String command, String help) {
        Help.addCommandH(new Help.HoverCommand(command, help, "module.permission", false));
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public final File getDataFolder() {
        return new File(LaggRemover.modDir, LaggRemover.getData(this)[0]);
    }

    public final Logger getLogger() {
        return LaggRemover.getInstance().getLogger();
    }

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        return false;
    }
}
