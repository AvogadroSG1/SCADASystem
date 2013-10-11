/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.util.ArrayList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * Just a copy of the previous jtree, however it has 
 * different nodes for critical and warning
 * @author Shawn
 */
public class SCADAJTreePrioritized extends SCADAJTree{

    private DefaultMutableTreeNode critical, warning;
    
    public SCADAJTreePrioritized() {
        super();
        init();
    }

    private void init() {
        critical = new DefaultMutableTreeNode("Critical");
        warning = new DefaultMutableTreeNode("Warning");
        
        root.add(critical);
        root.add(warning);
        
        this.setRootVisible(false);
    }
    
    /*
    @Override
    public void updateSCADASites(ArrayList<SCADASite> sites) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultMutableTreeNode critical = new DefaultMutableTreeNode("Critical");
        DefaultMutableTreeNode warning = new DefaultMutableTreeNode("Warning");
        
        root.add(critical);
        root.add(warning);
        

        
        TreeModel siteModel = new DefaultTreeModel(root);
        
        for(SCADASite site: sites) {
            if(site.getAlarm()) {
                critical.add(new SCADANode(site));
            }
            if(site.getWarning()) {
                warning.add(new SCADANode(site));
            }
        }
        warning.

        this.setModel(siteModel);
        
        for (int i = 0; i < this.getRowCount(); i++) 
        {
         this.expandRow(i);
        }
        
    }
    */
    
    @Override
    public void updateSCADASites(ArrayList<SCADASite> sites) {
        
        for(SCADASite site: sites) { 
            SCADANode node = getSCADANode(site);
            if(node != null) {
                node.removeFromParent();
                placeIntoCorrectTree(node);
            } else { // if the site isn't found
                placeIntoCorrectTree(new SCADANode(site));
            }
        }
        
    }
    
    private void placeIntoCorrectTree(SCADANode node) {
        if(node.getSite().isCritical()) {
            critical.add(node);
        } else if(node.getSite().isWarning()) {
            warning.add(node);
        }
    }
    
        public SCADASite getSelected(TreeSelectionEvent tse)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           getLastSelectedPathComponent();

            if(node instanceof SCADANode)
            {
                return ((SCADANode) node).getSite();
            }
            else
                return null;
        }
    
    
    
}
