This is a library intended to provide suppport
for Lasercutters on any platform.

Currently it supports moste Epilog Lasers
and the current LAOS board. (www.laoslaser.org).

It was created for VisiCut (http://visicut.org)
but you are invited to use it for your own programs.

If your Lasercutter is not supported, please contribute by implementing
the your driver as a subclass of the LaserCutter.java class.

FreakyAttic Modifications
-------------------------

This repository is a copy from the original one, and used to implement 
a custom **gCode output driver** to generate a file for GCode interpreters 
such as Mach3, LinuxCNC or Marlin Firmware for lasers.
