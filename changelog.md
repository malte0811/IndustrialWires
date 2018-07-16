###Minecraft 1.12.2

####Version 1.7-27
 - Fixed panel components causing issues on dedicated servers
 - Added an automatic update checker (Using the Forge update JSON)

####Version 1.7-26
 - Added Mechanical Multiblocks (energy storage and EU<->FE conversion)
   - There will be an explanation video for these once I have time to make one
 - Fixed the small mechanical converter blowing up tin wires
 - Fixed wires not joining their outputs correctly
 - Fixed wire connectors not breaking when the block they're on is broken

####Version 1.6-25
 - Updated to IE build 77
   - IC2 wires cause damage
   - Added insulated versions of gold, copper and tin wires. Due to heat sensitive insulation the transfer capacity is half of the normal capacity
   - Added feedthrough insulators for all IC2 wire types
 - Wire coils now automatically "merge" when picked up
 - Config values are applied to the game directly now

####Version 1.6-24
 - Fixed an infinite energy bug
 - Changed the default value for maximum energy conversion

####Version 1.6-23
 - Added a command to allow taking screenshots of Marx generator discharges (/ciw triggermarxscreenshot)
 - Panel components have to be shift-clicked to place them in the world
 - Fixed wire coil crafting
 - Added a recipe for the Seven-Segment displays

####Version 1.6-22
 - Added a Seven-Segment display for control panels
 - Panel components update their values as soon as they are changed in the GUI
 - Fixed a crash when breaking a panel with buttons, locks or toggle switches on it under special circumstances
 - Fixed server crashes when using panel components with a second controller id, but no channel set
 - Fixed various NPE crashes with control panel models under heavy load
 - Fixed wrong panel component ordering with raytracing

####Version 1.6-21
 - Added shaders for the Marx generator and Jacob's ladder
 - Fixed wires connected to a Marx generator on a server being invisible
 - The Marx generator actually gives output when processing ores now. Oops...
 - The "safe distance" formulas for the Marx generator in the manual now match the real safe distance
 - Improved rendering of the Marx generator in the manual. Some of this is only enabled with maven build 275+ or official build 75+ (not released yet)

####Version 1.6-20
 - Added the Marx Generator, an alternative ore processing method
   - Hearing protection absolutely required!
   - You may need to do some math and measurements for ore processing to work. It will kill entites just fine without any science
 - IC2 is no longer a hard dependency. The wires and converters will obviously be disabled without it
 - Vastly improved snapping in the panel creator
 - Added some Mirage (Albedo) compat
 - IW is signed now!
 - Analog panel components can interact with 2 channels now, rough and fine control
 - Fixed GUI background and item tooltips
 - Fixed some components resetting when the chunk is unloaded
 - Chunks with control panels properly unload now

####Version 1.5-15
 - Components can be placed in the world now to use the as conventional levers/etc.
 - Added documentation on the key ring. It also shows all attached keys on the tooltip now
 - Fixed some bugs with key ring crafting
 - Fixed control panels causing disconnects on servers

####Version 1.5-14
 - Updated to Minecraft 1.12
 - Added a recipe for the key ring. Kind of forgot about adding one when I added the ring itself...
 - Fixed wire length crafting leaving wrong coils when the output has maximum length
 - Fixed some more connection issues with control panels

###Minecraft 1.11.2

####Version 1.5-19
 - Fixed a crash with SpongeForge, chunk loading issues without
 - Fixed some components resetting on chunk unload

####Version 1.5-17
 - Backported some fixes from 1.12

####Version 1.5-13
 - Labels no longer break the model cache and cause lag
 - Labels don't break on dedicated servers any more

####Version 1.5-12
 - Added tilted control panels
   - Panels are no longer created from machine casings, there is a dedicated item for that now, the Unfinished Control Panel
   - Angle and height can be configured in the Engineer's Workbench
 - Fixed a CME when multi-threaded chunk rendering is enabled
 - Fixed control panels not connecting or disconnecting properly when a panel connector between the panel and the RS controller is broken/placed
 - Improved the performance of the control panel hitbox rendering

####Version 1.5-11
 - Updated to Minecraft 1.11.2
 - Added Panel Meters to monitor a redstone signal with reasonable accuracy
 - Multiple components on the same panel network can modify the same signal now without causing undefined behavior
 - Lock Switches no longer break the model cache

###Minecraft 1.10.2

####Version 1.4-18
 - Fixed a crash with SpongeForge, chunk loading issues without
 - Fixed some components resetting on chunk unload


####Version 1.4-16
 - Backported a lot of fixes from 1.11 and 1.12

####Version 1.4-10
 - added lock switches for control panels (backport from 1.11)
   - Can only be turned on by someone with the correct key to prevent unauthorized access
   - up to 10 keys can be added to a keyring to reduce inventory spam
 - IC2 items can be added to the appropriate sections of the engineers toolbox (backport from 1.11)
 - Components (lighted button, indicator light, etc.) on panels now actually light up (backport from 1.11)
 - Fixed power loss when no energy is being transmitted

####Version 1.4-9
 - added Control Panels
   - They can be used to control and monitor a lot of redstone signals from a few blocks

####Version 1.3-8
 - the converters and the motor don't have missing textures any more when using Chisel

####Version 1.3-7
 - added Jacob's Ladders/High voltage travelling arcs
   - they don't have a particular purpose aside from looking nice

####Version 1.2-6
 - reduced the discrepancies between IC2 cables and Industrial Wires
   - machines don't explode when they shouldn't except in some corner cases
 - potentially fixed missing textures on the mechanical converters
 - added Chinese translations (thanks SihenZhang)

####Version 1.2-5 (10,000 download celebratory release)
 - added mechanical converters and the rotational motor
   - they convert between IE rotational energy (windmill, dynamo etc) and IC2 kinetic energy
   - Rotational motor: produces IE rotational energy from IF
   - No lossless conversion
   - Can be disabled in the config
 - wire coils show when they are out of range (to match the behavior of IE coils in the latest dev version)

####Version 1.1-4
 - fixed an insane amount of log-spam in an edgecase (probably a Vanilla or Forge bug)
 - added config values for wire length per connection and per coil item
 
####Version 1.1-3
 - fixed incompatibility with IE build 48
   - reduced the amount of calls to core IE classes to make such incompatibilities less likely
 - fixed localization of the creative tab

####Version 1.1-2
 -  wire coils now use a different amount of wire depending on how long the connection is
   - wire coils are crafted by placing any amount of IC2 cables and/or the corresponding wire coils in a crafting table now
 - added Glass Fiber Wire
 - changed license to GPL3
 - changed the amount of connectors/relays the recipes yield