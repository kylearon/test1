/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Oct 26, 2009
 */
package com.soartech.simjr.ui;

import java.awt.Component;
import java.awt.Point;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soartech.simjr.ui.pvd.MapImage;

/**
 * @author ray
 */
public class MapImageOpacityController extends JPanel
{
    private static final long serialVersionUID = -8232810196699751398L;

    public static void showPopupEditor(JFrame frame, Component mapContainer, Point point, MapImage mapImage)
    {
        MapImageOpacityController mioc = new MapImageOpacityController(mapContainer, mapImage);
        NiftyPopup popup = new NiftyPopup(frame, mioc, point.x, point.y);
        popup.show();
    }

    public MapImageOpacityController(final Component mapContainer, final MapImage mapImage)
    {
        super();

        Box box = new Box(BoxLayout.Y_AXIS);
        
        box.add(new JLabel("Adjust map opacity"));
        
        final JSlider slider = new JSlider(0, 100, (int) (mapImage.getOpacity() * 100));
        slider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                int value = slider.getValue();
                mapImage.setOpacity(((float) value) / 100.0f);
                mapContainer.repaint();
            }
        });
        
        box.add(slider);
        
        if (mapImage.getImage(1) == null)
        {
            return;
        }
        
        box.add(new JLabel("Adjust terrain opacity"));
        
        final JSlider terrainSlider = new JSlider(0, 100, (int) (mapImage.getOpacity(1) * 100));
        terrainSlider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                int value = terrainSlider.getValue();
                mapImage.setOpacity(1, ((float) value) / 100.0f);
                mapContainer.repaint();
            }
        });
        
        box.add(terrainSlider);

        this.add(box);
    }
}
