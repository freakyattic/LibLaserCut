FreakyAttic LibLaserCut Modifications
-------------------------------------

This repository is a copy from the original one, and used to implement 
a custom **gCode output driver** to generate a file for GCode interpreters 
such as Mach3, LinuxCNC or Marlin Firmware for lasers.

This driver was implemented without knowing that the branch smoothieboard
was under development of a gcode driver, and since there are a few differences
in the implementation this driver it is going to be supported.

Features:
- Generation of a **output gCode text file**, that can be used to send it to the laser. No support for HTTP or Serial transfer.
- **Custom GCode** codes for configuring turning on/off laser, ventilation, gcode header and footer.
- Implemented functions for **Mark, Cut, Raster and Raster3D**.
- Configuration options for: bidirectional cutting, multiple passes, mirror of X and Y axis.
- **Optimized gCode** commands to reduce file size and redundant gCodes.

Todo List:
- Implement a option for multipasses depth movement in mm.
- Posibility to configure Z focus.

You can request any feature to suit your machine.
