# LaggRemover Folia Remastered

This is a remastered version of [LaggRemover](https://dev.bukkit.org/projects/laggremover). 

## Why This Project Exist
LaggRemove is an excellent optimization plugin, but it does not support Folia server and is unable to work well in Spigot/Paper server above 1.14. 
We tried to send a issue to the original author of LaggRemover, but there's no feedback. So we consider to migrate this plugin to high version Spigot/Paper/Folia server.

## Is this legal?
We tried to contact the author via GitHub to get the access, but we didn't get any feedback so we decided to migrate this plugin directly. We will always acknowledge that this plugin is owned by the original author, and we will remove this project immediately if the original author want us to do so.

## Features
### Ram Usage Reduction
LaggRemover decreases the amount of ram your server uses by monitoring and controlling the amount of chunks loaded into memory. The more chunks loaded, the more lag your server will have. LaggRemover eliminates this problem by keeping the bare minimum of chunks required loaded.
### Advanced Server Tick Tracking Technology
Get the most accurate Ticks Per a Second (TPS) reading of your server with the latest and most efficient methods possible.
### Impression Factor
First impression is everything with a server. If your server is lagging like crazy when someone joins it for the first time, they are not going to want to keep playing on it. Don't loose potential members to lag ever again.
### Accurate Readings
Check to see if your server host is really giving your the amount of ram they say they are. You can list the amount of ram you are currently using, the max that you can use, and the amount you currently have allocated.
### LaggRemoverAI
This feature detects and removes lag before it becomes a problem. There is nothing like this in any other plugin out there.
Automatic Lag Removal - Want to be extra save or maybe just make it look like something is happening? You can have a lag protocol automatically be run after a certain period of time.
Lag Machine Prevention - With LaggRemover's mob thinning, you can prevent players from making an effective lag machine. Lag machines are meant to harm your server horrifically by spawning loads of entities in the same location. Mob thinning can prevent such machines by disabling the ability for more than a certain amount of mobs to spawn in a single chunk.


## Commands

Every command now has a short-hand shown in parenthesis to the side of the command. For example: "/lr help 1" may be abbreviated "/lr h 1"

`/lr help` - Lists all commands in LaggRemover.<br>
`/lr tps` - Displays the servers TPS.<br>
`/lr ram` - Generic ram info command.<br>
`/lr chunk` - Lists the number of chunks loaded in that world.<br>
`/lr master` - Displays a lot of information about the world and server you are in.<br>
`/lr clear` - Clears items/entities on the ground.       <-- both of these commands have a lot of sub options that are not listed here<br>
`/lr count` - Counts items/entities on the ground.    <--<br>
`/lr unload` - Unloads all chunks in a world that you specify.<br>
`/lr gc` - One of the BEST commands in LaggRemover. This command unloads unnecessary items from RAM to increase your servers performance. Tests showed that it was able to decrease the amount of RAM being used by an average of 50% when used at start up.<br>
`/lr protocol` - Runs/lists/etc the various protocols LaggRemover has "under the hood" as well as what are added by 3rd party modules.<br>
`/lr modules` - Lists all loaded modules.<br>
`/lr info` - Displays info about the LaggRemover plugin.<br>
`/lr ping` - Lets you check the ping of a single player rather than an average of the pings of every player on the server (as in /lr master)<br>

## Permissions

`lr.help` - Gives access to the help command<br>
`lr.master` - Gives access to the master listing of all performance data from the server<br>
`lr.lagg` - Gives access to view the Ticks Per a Second(TPS) of the server<br>
`lr.world` - View data about a certain world<br>
`lr.clear` - Allows you to clear all items on the ground<br>
`lr.unload` - Allows players to unload all chunks in a world.<br>
`lr.gc` - Allows players to use decrease the amount of ram your server uses by unloading irrelevant items.<br>
`lr.nochatdelay` - Makes a player immune to the chat delay.<br>
`lr.modules` - Allows you to list loaded modules.<br>
`lr.update` - Allows LaggRemover to notify a player when an update is downloaded.<br>
`lr.ram` - Allows players to list the ram available on the server.<br>
`lr.protocol` - Allows players to view/manipulate protocols.<br>
`lr.ping` - Allows a player to view the ping of another player (or themself)<br>

## Configuration
View the configuration [HERE](https://dev.bukkit.org/projects/laggremover/pages/configuration).

## Donate the Original Author of LaggRemover
See [the original plugin page](https://dev.bukkit.org/projects/laggremover)
