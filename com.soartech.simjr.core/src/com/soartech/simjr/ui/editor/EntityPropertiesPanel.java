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
 * Created on Mar 29, 2009
 */
package com.soartech.simjr.ui.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.undo.UndoableEdit;

import net.miginfocom.swing.MigLayout;

import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.Model;
import com.soartech.simjr.scenario.ModelChangeEvent;
import com.soartech.simjr.scenario.ModelChangeListener;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityPrototypeDatabase;

/**
 * @author ray
 */
public class EntityPropertiesPanel extends JPanel implements ModelChangeListener, ActionListener
{
    private static final long serialVersionUID = -6065915301912538128L;

    private final ServiceManager services;
    private final Model model;
    private EntityElement entity;
    private final JTextField nameField;
    private final DefaultComboBoxModel typeModel = new DefaultComboBoxModel();
    private final JComboBox typeCombo = new JComboBox(typeModel);
    private final DefaultComboBoxModel forceModel = new DefaultComboBoxModel(EntityConstants.ALL_FORCES);
    private final JComboBox forceCombo = new JComboBox(forceModel);
    private final JCheckBox visibleCheckBox = new JCheckBox();
    private final HeadingSpinner headingSpinner;
    private final ScriptEditPanel initScript;
    
    public EntityPropertiesPanel(final ServiceManager services, final Model model)
    {
        super(new MigLayout());
        
        this.model = model;
        this.services = services;
        
        final UndoService undoService = this.services.findService(UndoService.class);
        
        add(new JLabel("Name"));
        add(nameField = new JTextField(30));
        add(new JLabel("Type"), "gap unrelated");
        add(typeCombo);
        add(new JLabel("Force"), "gap unrelated");
        add(forceCombo);
        
        add(new JLabel("Visible"), "gap unrelated");
        add(visibleCheckBox);
        visibleCheckBox.addActionListener(this);
        
        add(new JLabel("Heading"), "gap unrelated");
        add(headingSpinner = new HeadingSpinner(undoService), "wrap");
        add(new JLabel("Init Script"), "top");
        add(initScript = new ScriptEditPanel(undoService, 50), "span, growx, growy");
        
        new EntryCompletionHandler(nameField) {

            @Override
            public boolean verify(JComponent input)
            {
                if(entity != null)
                {
                    final String newName = nameField.getText().trim();
                    if(!newName.equals(entity.getName()) &&model.getEntities().getEntity(newName) != null)
                    {
                        return false;
                    }
                    
                    final UndoableEdit edit = entity.setName(newName);
                    if(edit != null)
                    {
                        services.findService(UndoService.class).addEdit(edit);
                    }
                }
                return true;
            }};
        
        populateTypeModel();
        new EntryCompletionHandler(typeCombo) {

            @Override
            public boolean verify(JComponent input)
            {
                EntityPrototype p = (EntityPrototype) typeCombo.getSelectedItem();
                if(entity != null)
                {
                    services.findService(UndoService.class).addEdit(entity.setPrototype(p.getId()));
                }
                return true;
            }};
        
        typeCombo.setRenderer(new DefaultListCellRenderer() {

            private static final long serialVersionUID = 418324624637909108L;

            /* (non-Javadoc)
             * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
             */
            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
            {
                final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                final EntityPrototype p = (EntityPrototype) value;
                setText(String.format("<html>%s / %s / <b>%s</b></html>", p.getDomain(), p.getCategory(), p.getSubcategory()));
                return label;
            }});
        
        new EntryCompletionHandler(forceCombo) {

            @Override
            public boolean verify(JComponent input)
            {
                String force = forceCombo.getSelectedItem().toString();
                if(entity != null)
                {
                    services.findService(UndoService.class).addEdit(entity.setForce(force));
                }
                return true;
            }};
        setEntity(null);
        this.model.addModelChangeListener(this);
    }
    
    public EntityElement getEntity()
    {
        return this.entity;
    }
    
    public void setEntity(EntityElement entity)
    {
        this.entity = entity;
        
        final boolean enabled = this.entity != null;
        nameField.setEnabled(enabled);
        typeCombo.setEnabled(enabled);
        forceCombo.setEnabled(enabled);
        visibleCheckBox.setEnabled(enabled);
        
        if(enabled)
        {
            nameField.setText(entity.getName());
            forceCombo.setSelectedItem(entity.getForce());
            final EntityPrototypeDatabase db = EntityPrototypeDatabase.findService(services);
            final EntityPrototype p = db.getPrototype(entity.getPrototype());
            typeCombo.setSelectedItem(p);
            headingSpinner.setElement(entity.getOrientation());
            initScript.setScript(entity.getInitScript());
            visibleCheckBox.setSelected(entity.isVisible());
        }
        else
        {
            headingSpinner.setElement(null);
            initScript.setScript(null);
        }
    }
    
    private void populateTypeModel()
    {
        final EntityPrototypeDatabase db = EntityPrototypeDatabase.findService(services);
        final List<EntityPrototype> prototypes = db.getPrototypes();
        
        // Filter out abstract prototypes and flyouts
        final Iterator<EntityPrototype> it = prototypes.iterator();
        while(it.hasNext())
        {
            final EntityPrototype p = it.next();
            if(p.isAbstract() || p.hasSubcategory("flyout"))
            {
                it.remove();
            }
        }
        
        Collections.sort(prototypes, new Comparator<EntityPrototype>() {

            public int compare(EntityPrototype o1, EntityPrototype o2)
            {
                return o1.toString().compareTo(o2.toString());
            }});
        for(EntityPrototype p : prototypes)
        {
            typeModel.addElement(p);
        }
    }

    public void onModelChanged(ModelChangeEvent e)
    {
        if(e.source != entity)
        {
            return;
        }
        
        if(e.property.equals(EntityElement.NAME)      || 
           e.property.equals(EntityElement.PROTOTYPE) ||
           e.property.equals(EntityElement.FORCE) ||
           e.property.equals(EntityElement.VISIBLE))
        {
            setEntity(entity); // force an update
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        final UndoableEdit edit = entity.setVisible(visibleCheckBox.isSelected());
        services.findService(UndoService.class).addEdit(edit);
    }
    
    
    
}
