/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import SCADASite.SCADAUpdateListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Shawn
 */
public class SiteStatusList extends JList<SCADASite> implements SCADAUpdateListener {
    
    
    public SiteStatusList() {
        super();
        this.setCellRenderer(new SCADAListRenderer());
        this.setModel(new DefaultListModel<SCADASite>());
    }

    public void addSite(SCADASite site) {
        DefaultListModel<SCADASite> model = (DefaultListModel<SCADASite>) this.getModel();
        model.addElement(site);
        site.addSCADAUpdateListener(this);
    }
    
    @Override
    public void update(SCADASite site) {
        updateSite(site);
    }
    
    public void updateSite(SCADASite site) {
        for(int i = 0; i < this.getModel().getSize(); i++) {
            if(site.getID() == this.getModel().getElementAt(i).getID()) {
                this.repaint(this.getCellBounds(i, i));
                return;
            }
        }
    }
    
    private class SCADAListRenderer extends JPanel implements ListCellRenderer<SCADASite> {

        /*
        @Override
        public Component getListCellRendererComponent(JList<? extends SCADASite> list, SCADASite value, int index, boolean isSelected, boolean cellHasFocus) {
            return new SCADAListComponent(value);
        }*/
        private JLabel label = new JLabel("");
        
        @Override
        public Component getListCellRendererComponent(JList<? extends SCADASite> list, SCADASite value, int index, boolean isSelected, boolean cellHasFocus) {
            
                Color color;
                
                final int ALPHA = 150;
                
                if(value.isCritical()) {
                    color = Color.red;
                    color = new Color(255, 0, 0, ALPHA);
                } else if(value.isWarning()) {
                    color = Color.orange;
                    color = new Color(255, 165, 0, ALPHA);
                } else {
                    color = Color.green;
                    color = new Color(0,255,0,ALPHA);
                }
                
                if(cellHasFocus) {
                    color = new Color(0, 255, 255, ALPHA);
                }
                
                setBackground(color);
            
                label.setText(value.getName());
            return this;
        }
        
            public SCADAListRenderer() {
                super();
                init();
            }
        /*
        private class SCADAListComponent extends JPanel {
            
            private SCADASite site;
            
            public SCADAListComponent(SCADASite site) {
                super();
                this.site = site;
                init();
            }*/
            
            private void init() {
                 this.setLayout(new BorderLayout());
                 this.setBorder(new EmptyBorder(10, 10, 10, 10));
                 
                 label = new JLabel("");
                 
                 this.add(label, BorderLayout.CENTER);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
                
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0,
                        getBackground().brighter().brighter(), 0, getHeight(),
                        getBackground().darker());

                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            
        
        
    }
    
}
