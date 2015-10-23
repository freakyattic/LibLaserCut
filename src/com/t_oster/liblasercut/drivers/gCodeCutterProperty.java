/**
 * This file is part of LibLaserCut.
 * Copyright (C) 2011 - 2014 Thomas Oster <mail@thomas-oster.de>
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

import com.t_oster.liblasercut.FloatPowerSpeedFocusFrequencyProperty;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author Thomas Oster <thomas.oster@rwth-aachen.de>
 */
public class gCodeCutterProperty extends FloatPowerSpeedFocusFrequencyProperty {
  
  private boolean hideVentilation = false;
  private boolean hideFocus = false;
  
  private float speed = 100;
  private boolean ventilation = true;
  private int passes = 1;

  public gCodeCutterProperty( boolean hideVentilation, boolean hideFocus)
  {
    //this.hidePurge = hidePurge;
    this.hideVentilation = hideVentilation;
    this.hideFocus = hideFocus;
  }
  
  public gCodeCutterProperty()
  {
    this( false, false);
  }
  
  @Override
  public String[] getPropertyKeys()
  {
    LinkedList<String> result = new LinkedList<String>();
    result.addAll(Arrays.asList(super.getPropertyKeys()));
      
    result.remove("frequency");
    result.add("passes");
    
    if (this.hideFocus)
    {
      result.remove("focus");
    }
    if (!this.hideVentilation)
    {
      result.add("ventilation");
    }
    
    return result.toArray(new String[0]);
  }

  @Override
  public Object getProperty(String name)
  {
    if ("ventilation".equals(name))
    {
      return (Boolean) this.ventilation;
    }
    else if ("passes".equals(name))
    {
      return (int) this.getPasses();
    }
    else if ("speed".equals(name))
    {
      return (Float) this.getSpeed();
    }
    else
    {
      return super.getProperty(name);
    }
  }

  @Override
  public void setProperty(String name, Object value)
  {
    if ("ventilation".equals(name))
    {
      this.ventilation = ((Boolean) value);
    }
    else if ("passes".equals(name))
    {
      this.setPasses( (Integer) value);
    }
    else if ("speed".equals(name))
    {
      this.setSpeed((Float)value);
    }
    else
    {
      super.setProperty(name, value);
    }
  }

  @Override
  public gCodeCutterProperty clone()
  {
    gCodeCutterProperty result = new gCodeCutterProperty();
    for (String s:this.getPropertyKeys())
    {
      result.setProperty(s, this.getProperty(s));
    }
    return result;
  }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final gCodeCutterProperty other = (gCodeCutterProperty) obj;
        if (this.ventilation != other.ventilation) {
            return false;
        }
        if (this.passes != other.passes) {
            return false;
        }
        if (this.speed != other.speed) {
            return false;
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.ventilation ? 1 : 0);
        hash = 97 * hash + super.hashCode();
        return hash;
    }
  
    @Override
    public void setSpeed(float speed)
    {
      System.out.print ("Mierda " + speed);
      
      speed = speed < 0 ? 0 : speed;
      speed = speed > 4000 ? 4000 : speed;
      this.speed = speed;
    }
    
    @Override
    public float getSpeed()
    {
      return this.speed;
    }
  
    public boolean getVentilation()
    {
      return this.ventilation;
    }
  
    public Integer getPasses()
    {
      return this.passes;
    }
    
    public void setPasses(int passes)
    {      
      passes = passes < 1 ? 1 : passes;
      this.passes = passes;
    }
}
