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
 * Created on Jun 11, 2007
 */
package com.soartech.simjr.ui.pvd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXPanel;

/**
 * @author ray
 */
public class ActiveEditorPanel extends JXPanel
{
    private static final long serialVersionUID = -1L;
    private static final Logger logger = Logger.getLogger(ActiveEditorPanel.class);

    private PlanViewDisplay pvd = null;
    
    private JButton doneButton = new JButton("Done");
    
    public ActiveEditorPanel(PlanViewDisplay pvd)
    {
        super();
        
        this.pvd = pvd;
        
        this.add(doneButton);
        
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ActiveEditorPanel.this.pvd.remove(ActiveEditorPanel.this);
            }
        });
        
        setAlpha(0.7f);
        
        final int width = 100;
        final int height = 35;
        setBounds(pvd.getWidth()/2 - width/2, 10, width, height);
        
        pvd.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                logger.info("PVD resized: " + evt);
                setBounds(ActiveEditorPanel.this.pvd.getWidth()/2 - width/2, 10, width, height);
            }
        });
        
        this.pvd.add(this);
    }
    
}
