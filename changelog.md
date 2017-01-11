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