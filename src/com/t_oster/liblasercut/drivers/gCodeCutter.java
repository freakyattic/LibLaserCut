/**
 * This file is part of LibLaserCut.
 * Copyright (C) 2011 - 2014 Thomas Oster <mail@thomas-oster.de>
 *
 * This class is a modified copy version of the LAOSCutter driver class, all
 * credit goes to the author Thomas Oster <thomas.oster@rwth-aachen.de>
 * Modifications to support the generation of Gcode are done by Freakyattic
 * 
 * LibLaserCut is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibLaserCut is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LibLaserCut. If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package com.t_oster.liblasercut.drivers;

import com.t_oster.liblasercut.IllegalJobException;
import com.t_oster.liblasercut.JobPart;
import com.t_oster.liblasercut.LaserCutter;
import com.t_oster.liblasercut.LaserJob;
import com.t_oster.liblasercut.LaserProperty;
import com.t_oster.liblasercut.ProgressListener;
import com.t_oster.liblasercut.Raster3dPart;
import com.t_oster.liblasercut.RasterPart;
import com.t_oster.liblasercut.VectorCommand;
import com.t_oster.liblasercut.VectorPart;
import com.t_oster.liblasercut.platform.Point;
import com.t_oster.liblasercut.platform.Util;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * This class implements a driver for a Generic gCode Lasercutter board. 
 * To be used with a gCode interpreter like ; Mach3, Marlin, Linuxcn
 * 
 * This class is a modified copy version of the LAOSCutter driver class, all
 * credit goes to the author Thomas Oster <thomas.oster@rwth-aachen.de>
 * Modifications to support the generation of Gcode by Freakyattic
 * 
 * @author FreakyAttic - JavierFG <freakyattic@javitonet.com>
 */
public class gCodeCutter extends LaserCutter
{

  private static final String SETTING_OUTFILE = "gCode Output File";
  private static final String SETTING_BEDWIDTH = "Laserbed width";
  private static final String SETTING_BEDHEIGHT = "Laserbed height";
  private static final String SETTING_RASTER_WHITESPACE = "Additional space per Raster line";
  private static final String SETTING_SUPPORTS_VENTILATION = "Supports ventilation";
  private static final String SETTING_SUPPORTS_POWER = "Supports Power Modulation";
  private static final String SETTING_SUPPORTS_FOCUS = "Supports focus (Z-axis movement)";
  private static final String SETTING_GCODEHEADER = "gCode Start Header";
  private static final String SETTING_GCODEFOOTER = "gCode End Footer";
  private static final String SETTING_GCODEVENTON = "gCode Vent. On";
  private static final String SETTING_GCODEVENTOFF = "gCode Vent. Off";
  private static final String SETTING_GCODELASERON = "gCode Laser On";
  private static final String SETTING_GCODELASEROFF = "gCode Laser Off";
  private static final String SETTING_MIRRORXAXIS = "Mirror X Axis";
  private static final String SETTING_MIRRORYAXIS = "Mirror Y Axis";
  
  
//#############################################################################
//        Properties
//#############################################################################
  
  protected String outputFileName = "c:\\VisiCut_GCode.nc";
  
  protected String gcodeHeader = "G90 ; Absolut Position\\nG21 ; Units in mm";
  protected String gcodeFooter = "M02";
  
  protected String gcodeVentOn  = "M106 ; Ventilation On";
  protected String gcodeVentOff = "M107 ; Ventilation Off";
  
  protected String gcodeLaserOn  = "M171";
  protected String gcodeLaserOff = "M170";
  
  protected double bedWidth = 300;
  protected double bedHeight = 210;
  
  protected boolean mirrorXaxis = false;
  protected boolean mirrorYaxis = true;
  
  private boolean supportsPower = true;
  private boolean supportsFocus = false;
  private boolean supportsVentilation = false;
  private double  addSpacePerRasterLine = 0;
  
  private static String[] settingAttributes = new String[]{
    SETTING_OUTFILE,
    SETTING_BEDWIDTH,
    SETTING_BEDHEIGHT,
    SETTING_MIRRORXAXIS,
    SETTING_MIRRORYAXIS,
    SETTING_SUPPORTS_VENTILATION,
    //SETTING_SUPPORTS_FOCUS,
    SETTING_SUPPORTS_POWER,
    //SETTING_RASTER_WHITESPACE,
    SETTING_GCODEHEADER,
    SETTING_GCODEFOOTER,
    SETTING_GCODEVENTON,
    SETTING_GCODEVENTOFF,
    SETTING_GCODELASERON,
    SETTING_GCODELASEROFF,
  };

  @Override
  public LaserCutter clone()
  {
    gCodeCutter clone = new gCodeCutter();
    clone.outputFileName = outputFileName;
    clone.bedHeight = bedHeight;
    clone.bedWidth = bedWidth;
    clone.addSpacePerRasterLine = addSpacePerRasterLine;
    clone.supportsPower = supportsPower;
    clone.supportsVentilation = supportsVentilation;
    clone.supportsFocus = supportsFocus;
    clone.gcodeVentOn = gcodeVentOn;
    clone.gcodeVentOff = gcodeVentOff;
    return clone;
  }
  
  @Override
  public String[] getPropertyKeys()
  {
    return settingAttributes;
  }

  @Override
  public Object getProperty(String attribute)
  {
    if (SETTING_OUTFILE.equals(attribute))
    {
      return this.outputFileName;
    }
    else if (SETTING_RASTER_WHITESPACE.equals(attribute))
    {
      return (Double) this.addSpacePerRasterLine;
    }
    else if (SETTING_SUPPORTS_POWER.equals(attribute))
    {
      return (Boolean) this.supportsPower;
    }
    else if (SETTING_SUPPORTS_VENTILATION.equals(attribute))
    {
      return (Boolean) this.supportsVentilation;
    }
    else if (SETTING_SUPPORTS_FOCUS.equals(attribute))
    {
      return (Boolean) this.supportsFocus;
    }
    else if (SETTING_BEDWIDTH.equals(attribute))
    {
      return (Double) this.bedWidth;
    }
    else if (SETTING_BEDHEIGHT.equals(attribute))
    {
      return (Double) this.bedHeight;
    }
    else if (SETTING_MIRRORXAXIS.equals(attribute))
    {
      return (Boolean) this.mirrorXaxis;
    }
    else if (SETTING_MIRRORYAXIS.equals(attribute))
    {
      return (Boolean) this.mirrorYaxis;
    }
    else if (SETTING_GCODEHEADER.equals(attribute))
    {
      return (String) this.gcodeHeader;
    }
    else if (SETTING_GCODEFOOTER.equals(attribute))
    {
      return (String) this.gcodeFooter;
    }
    else if (SETTING_GCODEVENTON.equals(attribute))
    {
      return (String) this.gcodeVentOn;
    }
    else if (SETTING_GCODEVENTOFF.equals(attribute))
    {
      return (String) this.gcodeVentOff;
    }
    else if (SETTING_GCODELASERON.equals(attribute))
    {
      return (String) this.gcodeLaserOn;
    }
    else if (SETTING_GCODELASEROFF.equals(attribute))
    {
      return (String) this.gcodeLaserOff;
    }
    return null;
  }

  @Override
  public void setProperty(String attribute, Object value)
  {
    if (SETTING_OUTFILE.equals(attribute))
    {
      this.outputFileName = (value != null ? (String) value : "");
    }
    else if (SETTING_RASTER_WHITESPACE.equals(attribute))
    {
      this.addSpacePerRasterLine = (Double) value;
    }
    else if (SETTING_SUPPORTS_POWER.equals(attribute))
    {
      this.supportsPower = (Boolean) value;
    }
    else if (SETTING_SUPPORTS_VENTILATION.equals(attribute))
    {
      this.supportsVentilation = (Boolean) value;
    }
    else if (SETTING_SUPPORTS_FOCUS.equals(attribute))
    {
      this.supportsFocus = (Boolean) value;
    }
    else if (SETTING_BEDWIDTH.equals(attribute))
    {
      this.bedWidth = (Double)value;
    }
    else if (SETTING_BEDHEIGHT.equals(attribute))
    {
      this.bedHeight = (Double)value;
    }
    else if (SETTING_MIRRORXAXIS.equals(attribute))
    {
      this.mirrorXaxis = (Boolean)value;
    }
    else if (SETTING_MIRRORYAXIS.equals(attribute))
    {
      this.mirrorYaxis = (Boolean)value;
    }
    else if (SETTING_GCODEHEADER.equals(attribute))
    {
      this.gcodeHeader = (String) value;
    }
    else if (SETTING_GCODEFOOTER.equals(attribute))
    {
      this.gcodeFooter = (String) value;
    }
    else if (SETTING_GCODEVENTON.equals(attribute))
    {
      this.gcodeVentOn = (String) value;
    }
    else if (SETTING_GCODEVENTOFF.equals(attribute))
    {
      this.gcodeVentOff = (String) value;
    }
    else if (SETTING_GCODELASERON.equals(attribute))
    {
      this.gcodeLaserOn = (String) value;
    }
    else if (SETTING_GCODELASEROFF.equals(attribute))
    {
      this.gcodeLaserOff = (String) value;
    }
  }

  private List<Double> resolutions;

  @Override
  public List<Double> getResolutions()
  {
    if (resolutions == null)
    {
      //TODO: Calculate possible resolutions
      //according to mm/step
      resolutions = Arrays.asList(new Double[]
        {
          100d,
          200d,
          300d,
          500d,
          600d,
          1000d,
          1200d,
          1500d,
          2000d,
          4000d,
          8000d,
        });
    }
    return resolutions;
  }
  
  /**
   * Get the value of bedWidth
   *
   * @return the value of bedWidth
   */
  @Override
  public double getBedWidth()
  {
    return bedWidth;
  }
  
  /**
   * Get the value of bedHeight
   *
   * @return the value of bedHeight
   */
  @Override
  public double getBedHeight()
  {
    return bedHeight;
  }
 
//#############################################################################
//      Output functions
//#############################################################################
  
  private double prevX = -1;
  private double prevY = -1;

  private float previousPower = -1;
  
  private boolean gcodeG00Printed = false;
  private boolean gcodeG01Printed = false;
  
  private void move(PrintStream out, double x, double y)
  {
    if(mirrorXaxis)
      x *= -1;
    if(mirrorYaxis)
      y *= -1;
    
    if((prevX == x)&&(prevY ==y))
      return;
   
    //Update power if its value it is updated.
    if( previousPower != -1)
    {
      previousPower = -1;
      
      out.println(this.gcodeLaserOff);
    }
    
    //Print G00 movement code
    if(!gcodeG00Printed)
    {
      out.print("G00 ");
      gcodeG00Printed = true;
      gcodeG01Printed = false;
    }  
    
    if(prevX != x)
      out.printf("X%.4f ", x);
    
    if(prevY != y)
      out.printf("Y%.4f", y);
    
    out.print("\n"); //end of line
    
    prevX = x;
    prevY = y;
  }

  private void line(PrintStream out, double x, double y)
  {
    //Mirror Axis
    if(mirrorXaxis)
      x *= -1;
    if(mirrorYaxis)
      y *= -1;
    
    //If position has not changed
    if((prevX == x)&&(prevY ==y))
      return;
    
    //Update power if its value it is updated.
    if( previousPower != currentPower)
    {
      previousPower = currentPower;
      
      if(this.supportsPower)
        out.printf("%s S%d\n", this.gcodeLaserOn, (int)(this.currentPower*100));
      else
        out.println(this.gcodeLaserOn);
    }
 
    //Print G01 movement code
    if(!gcodeG01Printed)
    {
      out.print("G01 ");
      gcodeG00Printed = false;
      gcodeG01Printed = true;
    }  
    
    if(prevX != x)
      out.printf("X%.4f ", x);
    
    if(prevY != y)
      out.printf("Y%.4f", y);
    
    out.print("\n"); //end of line
    
    prevX = x;
    prevY = y;
  }

  private float currentPower = -1;
  private void setPower(float power)
  {
    currentPower = power;
  }

  private float currentSpeed = -1;
  private void setSpeed(PrintStream out, float speed)
  {
    if (currentSpeed != speed)
    {
      out.printf("F%d ;Feed Rate\n", (int) (speed));
      currentSpeed = speed;
    }
  }

  private float currentFocus = 0;
  private void setFocus(PrintStream out, float focus)
  {
    if (currentFocus != focus)
    {
      out.printf(Locale.US, "Focus 2 %d\n", (int)focus);
      currentFocus = focus;
    }
  }

  private Boolean currentVentilation = null;
  private void setVentilation(PrintStream out, boolean ventilation)
  {
    if (currentVentilation == null || !currentVentilation.equals(ventilation))
    {
      if(ventilation)
        out.println(this.gcodeVentOn.replace("\\n","\n").replace("\\r", "\r"));
      else
        out.println(this.gcodeVentOff.replace("\\n","\n").replace("\\r", "\r"));
      
      currentVentilation = ventilation;
    }
  }

  protected void writeJobCode(LaserJob job, OutputStream out, ProgressListener pl) throws UnsupportedEncodingException, IOException
  {
    out.write(this.generateInitializationCode( job ));
    pl.progressChanged(this, 20);
    
    prevX = -1;
    prevY = -1;
    previousPower = -1;
    gcodeG01Printed = false;
    gcodeG00Printed = false;
    currentSpeed = -1;
    
    int i = 0;
    int max = job.getParts().size();
    
    for (JobPart p : job.getParts())
    {
      if (p instanceof Raster3dPart)
      {
        out.write(this.generatePseudoRaster3dGCode((Raster3dPart) p, p.getDPI()));
      }
      else if (p instanceof RasterPart)
      {
        out.write(this.generategCodeRasterCode((RasterPart) p, p.getDPI()));
      }
      else if (p instanceof VectorPart)
      {
        out.write(this.generateVectorGCode((VectorPart) p, p.getDPI()));
      }
      i++;
      pl.progressChanged(this, 20 + (int) (i*(double) 60/max));
    }
    out.write(this.generateShutdownCode());
    out.close();
  }
  
  private byte[] generateVectorGCode(VectorPart vp, double resolution) throws UnsupportedEncodingException
  {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(result, true, "US-ASCII");
    gCodeEngraveProperty prop = vp.getCurrentCuttingProperty() instanceof gCodeEngraveProperty ? (gCodeEngraveProperty) vp.getCurrentCuttingProperty() : new gCodeEngraveProperty(vp.getCurrentCuttingProperty());
    
    out.println("; Vector gCode ------------------------------------------------------");
    
    for( int x = 0 ; x < prop.getPasses(); x++)
    {
      if(prop.getPasses()>1)
        out.println(";--Pass number " + (x+1) + "--");
      
      for (VectorCommand cmd : vp.getCommandList())
      {
        switch (cmd.getType())
        {
          case MOVETO:
            move(out, Util.px2mm(cmd.getX(), resolution), Util.px2mm(cmd.getY(), resolution));
            break;
          case LINETO:
            line(out, Util.px2mm(cmd.getX(), resolution), Util.px2mm(cmd.getY(), resolution));
            break;
          case SETPROPERTY:
          {
            this.setCurrentProperty(out, cmd.getProperty());
            break;
          }
        }
      }
    }
    
    return result.toByteArray();
  }

  
  private byte[] generatePseudoRaster3dGCode(Raster3dPart rp, double resolution) throws UnsupportedEncodingException
  {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(result, true, "US-ASCII");
    out.println("; Raster 3D gCode ------------------------------------------------------");
    
    boolean dirRight = true;
    
    Point rasterStart = rp.getRasterStart();
    
    gCodeEngraveProperty prop = rp.getLaserProperty() instanceof gCodeEngraveProperty ? (gCodeEngraveProperty) rp.getLaserProperty() : new gCodeEngraveProperty(rp.getLaserProperty());
    this.setCurrentProperty(out, prop);
    float maxPower = this.currentPower;
    boolean bu = prop.isEngraveBottomUp();
    for (int line = bu ? rp.getRasterHeight()-1 : 0; bu ? line >= 0 : line < rp.getRasterHeight(); line += bu ? -1 : 1 )
    {
      Point lineStart = rasterStart.clone();
      lineStart.y += line;
      List<Byte> bytes = rp.getRasterLine(line);
      //remove heading zeroes
      while (bytes.size() > 0 && bytes.get(0) == 0)
      {
        bytes.remove(0);
        lineStart.x += 1;
      }
      //remove trailing zeroes
      while (bytes.size() > 0 && bytes.get(bytes.size() - 1) == 0)
      {
        bytes.remove(bytes.size() - 1);
      }
      if (bytes.size() > 0)
      {
        if (dirRight)
        {
          //move to the first nonempyt point of the line
          move(out, lineStart.x, lineStart.y);
          byte old = bytes.get(0);
          for (int pix = 0; pix < bytes.size(); pix++)
          {
            if (bytes.get(pix) != old)
            {
              if (old == 0)
              {
                move(out, lineStart.x + pix, lineStart.y);
              }
              else
              {
                setPower(maxPower * (0xFF & old) / 255);
                line(out, lineStart.x + pix - 1, lineStart.y);
                move(out, lineStart.x + pix, lineStart.y);
              }
              old = bytes.get(pix);
            }
          }
          //last point is also not "white"
          setPower(maxPower * (0xFF & bytes.get(bytes.size() - 1)) / 255);
          line(out, lineStart.x + bytes.size() - 1, lineStart.y);
        }
        else
        {
          //move to the last nonempty point of the line
          move(out, lineStart.x + bytes.size() - 1, lineStart.y);
          byte old = bytes.get(bytes.size() - 1);
          for (int pix = bytes.size() - 1; pix >= 0; pix--)
          {
            if (bytes.get(pix) != old || pix == 0)
            {
              if (old == 0)
              {
                move(out, lineStart.x + pix, lineStart.y);
              }
              else
              {
                setPower( maxPower * (0xFF & old) / 255);
                line(out, lineStart.x + pix + 1, lineStart.y);
                move(out, lineStart.x + pix, lineStart.y);
              }
              old = bytes.get(pix);
            }
          }
          //last point is also not "white"
          setPower(maxPower * (0xFF & bytes.get(0)) / 255);
          line(out, lineStart.x, lineStart.y);
        }
      }
      if (!prop.isEngraveUnidirectional())
      {
        dirRight = !dirRight;
      }
    }
    return result.toByteArray();
  }

  /**
   * This Method takes a raster-line represented by a list of bytes,
   * where: byte0 ist the left-most byte, in one byte, the MSB is the
   * left-most bit, 0 representing laser off, 1 representing laser on.
   * The Output List of longs, where each value is the unsigned dword
   * of 4 bytes of the input each, where the first dword is the leftmost
   * dword and the LSB is the leftmost bit. If outputLeftToRight is false,
   * the first dword is the rightmost dword and the LSB of each dword is the
   * the Output is padded with zeroes on the right side, if leftToRight is true,
   * on the left-side otherwise
   * rightmost bit
   * @param line
   * @param outputLeftToRight
   * @return
   */
  public List<Long> byteLineToDwords(List<Byte> line, boolean outputLeftToRight)
  {
    List<Long> result = new ArrayList<Long>();
    int s = line.size();
    for (int i=0;i<s;i++)
    {
      line.set(i, (byte) (Integer.reverse(0xFF&line.get(i))>>>24));
    }
    for(int i=0; i<s; i+=4)
    {
      result.add(
        (((long) (i+3 < s ? 0xFF&line.get(i+3) : 0))<<24)
        + (((long) (i+2 < s ? 0xFF&line.get(i+2) : 0))<<16)
        + (((long) (i+1 < s ? 0xFF&line.get(i+1) : 0))<<8)
        + ((long) (0xFF&line.get(i)))
        );
    }
    if (!outputLeftToRight)
    {
      Collections.reverse(result);
      for(int i=0;i<result.size();i++)
      {
        result.set(i, Long.reverse(result.get(i)) >>> 32);
      }
    }
    return result;
  }

  private byte[] generategCodeRasterCode(RasterPart rp, double resolution) throws UnsupportedEncodingException, IOException
  {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(result, true, "US-ASCII");
    out.println("; Raster gCode ------------------------------------------------------");
    
    boolean dirRight = true;
    Point rasterStart = rp.getRasterStart();
    gCodeEngraveProperty prop = rp.getLaserProperty() instanceof gCodeEngraveProperty ? (gCodeEngraveProperty) rp.getLaserProperty() : new gCodeEngraveProperty(rp.getLaserProperty());
    this.setCurrentProperty(out, prop);
    boolean bu = prop.isEngraveBottomUp();
    for (int line = bu ? rp.getRasterHeight()-1 : 0; bu ? line >= 0 : line < rp.getRasterHeight(); line += bu ? -1 : 1)
    {
      Point lineStart = rasterStart.clone();
      lineStart.y += line;
      List<Byte> bytes = rp.getRasterLine(line);
      //remove heading zeroes
      while (bytes.size() > 0 && bytes.get(0) == 0)
      {
        lineStart.x += 8;
        bytes.remove(0);
      }
      //remove trailing zeroes
      while (bytes.size() > 0 && bytes.get(bytes.size()-1) == 0)
      {
        bytes.remove(bytes.size()-1);
      }
      if (bytes.size() > 0)
      {
        //add space on the left side
        int space = (int) Util.mm2px(this.addSpacePerRasterLine, resolution);
        while (space > 0 && lineStart.x >= 8)
        {
          bytes.add(0, (byte) 0);
          space -= 8;
          lineStart.x -=8;
        }
        //add space on the right side
        space = (int) Util.mm2px(this.addSpacePerRasterLine, resolution);
        int max = (int) Util.mm2px(this.getBedWidth(), resolution);
        while (space > 0 && lineStart.x+(8*bytes.size()) < max-8)
        {
          bytes.add((byte) 0);
          space -= 8;
        }
        if (dirRight)
        {
          //move to the first point of the line
          move(out, lineStart.x, lineStart.y);
          List<Long> dwords = this.byteLineToDwords(bytes, true);
          //TODO FREAKYATTIC RasterImage  loadBitmapLine(out, dwords);
          line(out, lineStart.x + (dwords.size()*32), lineStart.y);
        }
        else
        {
          //move to the first point of the line
          List<Long> dwords = this.byteLineToDwords(bytes, false);
          move(out, lineStart.x+(dwords.size()*32), lineStart.y);
          //TODO FREAKYATTIC RasterImage  loadBitmapLine(out, dwords);
          line(out, lineStart.x, lineStart.y);
        }
      }
      if (!prop.isEngraveUnidirectional())
      {
        dirRight = !dirRight;
      }
    }
    return result.toByteArray();
  }
  
  private void setCurrentProperty(PrintStream out, LaserProperty p)
  {
    if (p instanceof gCodeCutterProperty)
    {
      gCodeCutterProperty prop = (gCodeCutterProperty) p;
      if (this.supportsFocus)
      {
        setFocus(out, prop.getFocus());
      }
      if (this.supportsVentilation)
      {
        setVentilation(out, prop.getVentilation());
      }
      setSpeed(out, prop.getSpeed());
      setPower(prop.getPower());
    }
    else
    {
      throw new RuntimeException("The gCode driver only accepts gCodeCutter properties (was "+p.getClass().toString()+")");
    }
  }
  
  private byte[] generateInitializationCode( LaserJob job ) throws UnsupportedEncodingException
  {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(result, true, "US-ASCII");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    
    out.println("; VisiCut - Generated GCode program");
    out.println("; Generic gCode Driver by FreakyAttic.com");
    out.println("; FILE: " + job.getTitle());
    out.println("; DATE: "+timeStamp);
    out.println(";\n;");
    out.println(this.gcodeHeader.replace("\\n","\n").replace("\\r", "\r"));
    
    out.println(this.gcodeLaserOff);
    
    return result.toByteArray();
  }

  private byte[] generateShutdownCode() throws UnsupportedEncodingException
  {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(result, true, "US-ASCII");
    
    out.println(this.gcodeLaserOff);
    
    this.setFocus(out, 0f);
    this.setVentilation(out, false);
    
    out.println(this.gcodeFooter.replace("\\n","\n").replace("\\r", "\r"));
    out.println("; End-Of-File");
    
    return result.toByteArray();
  }
  

  @Override
  public void sendJob(LaserJob job, ProgressListener pl, List<String> warnings) throws IllegalJobException, Exception
  {
    currentPower = -1;
    currentSpeed = -1;
    currentFocus = 0;
    currentVentilation = false;
    
    pl.progressChanged(this, 0);
    
    BufferedOutputStream out;
    ByteArrayOutputStream buffer = null;
    pl.taskChanged(this, "Checking job");
    
    checkJob(job);
    job.applyStartPoint();
    
    buffer = new ByteArrayOutputStream();
    out = new BufferedOutputStream(buffer);
    pl.taskChanged(this, "Buffering");
    
    this.writeJobCode(job, out, pl);  // calculate all the Gcode
    
    pl.taskChanged(this, "File writing " + outputFileName);
    FileOutputStream o = new FileOutputStream(new File(outputFileName));
    o.write(buffer.toByteArray());
    o.close();

    pl.taskChanged(this, "Finished");

    pl.progressChanged(this, 100);
  }
  
//only kept for backwards compatibility. unused
  private transient boolean unidir = false;
  
  @Override
  public gCodeCutterProperty getLaserPropertyForVectorPart()
  {
    return new gCodeCutterProperty(!this.supportsVentilation, !this.supportsFocus);
  }

  @Override
  public gCodeEngraveProperty getLaserPropertyForRasterPart()
  {
    return new gCodeEngraveProperty(!this.supportsVentilation, !this.supportsFocus);
  }

  @Override
  public gCodeEngraveProperty getLaserPropertyForRaster3dPart()
  {
    return new gCodeEngraveProperty(!this.supportsVentilation, !this.supportsFocus);
  }

  @Override
  public String getModelName()
  {
    return "Generic gCode driver";
  }
}

