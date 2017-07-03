#####Version 1.5-12
 - Added tilted control panels
   - Panels are no longer created from machine casings, there is a dedicated item for that now, the Unfinished Control Panel
   - Angle and height can be configured in the Engineer's Workbench
 - Fixed a CME when multi-threaded chunk rendering is enabled
 - Fixed control panels not connecting or disconnecting properly when a panel connector between the panel and the RS controller is broken/placed
 - Improved the performance of the control panel hitbox rendering

#####Version 1.5-11
 - Updated to Minecraft 1.11.2
 - Added Panel Meters to monitor a redstone signal with reasonable accuracy
 - Multiple components on the same panel network can modify the same signal now without causing undefined behavior
 - Lock Switches no longer break the model cache

#####Version 1.4-10
 - added lock switches for control panels (backport from 1.11)
   - Can only be turned on by someone with the correct key to prevent unauthorized access
   - up to 10 keys can be added to a keyring to reduce inventory spam
 - IC2 items can be added to the appropriate sections of the engineers toolbox (backport from 1.11)
 - Components (lighted button, indicator light, etc.) on panels now actually light up (backport from 1.11)
 - Fixed power loss when no energy is being transmitted
#####Version 1.4-9
 - added Control Panels
   - They can be used to control and monitor a lot of redstone signals from a few blocks

#####Version 1.3-8
 - the converters and the motor don't have missing textures any more when using Chisel
#####Version 1.3-7
 - added Jacob's Ladders/High voltage travelling arcs
   - they don't have a particular purpose aside from looking nice

#####Version 1.2-6
 - reduced the discrepancies between IC2 cables and Industrial Wires
   - machines don't explode when they shouldn't except in some corner cases
 - potentially fixed missing textures on the mechanical converters
 - added Chinese translations (thanks SihenZhang)

#####Version 1.2-5 (10,000 download celebratory release)
 - added mechanical converters and the rotational motor
   - they convert between IE rotational energy (windmill, dynamo etc) and IC2 kinetic energy
   - Rotational motor: produces IE rotational energy from IF
   - No lossless conversion
   - Can be disabled in the config
 - wire coils show when they are out of range (to match the behavior of IE coils in the latest dev version)

#####Version 1.1-4
 - fixed an insane amount of log-spam in an edgecase (probably a Vanilla or Forge bug)
 - added config values for wire length per connection and per coil item
 
#####Version 1.1-3
 - fixed incompatibility with IE build 48
   - reduced the amount of calls to core IE classes to make such incompatibilities less likely
 - fixed localization of the creative tab

#####Version 1.1-2
 -  wire coils now use a different amount of wire depending on how long the connection is
   - wire coils are crafted by placing any amount of IC2 cables and/or the corresponding wire coils in a crafting table now
 - added Glass Fiber Wire
 - changed license to GPL3
 - changed the amount of connectors/relays the recipes yield