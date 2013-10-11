/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/**
 *
 * @author Shawn
 */
public class SCADAJTree extends JTree
{
    DefaultMutableTreeNode root;
    DefaultTreeModel siteModel;
    
    public SCADAJTree() {
        super();
        root = new DefaultMutableTreeNode("Sites");
        siteModel = new DefaultTreeModel(root);
        this.setModel(siteModel);
        this.setCellRenderer(new SCADACellRenderer());
        this.setFocusable(true);
        
    }
    
    public void updateSCADASites(ArrayList<SCADASite> sites) {
        
        for(SCADASite site: sites) {
            
            boolean found = false;
            
            for(int i = 0; i < root.getChildCount() && !found; i++) {
                TreeNode node = root.getChildAt(i);
                
                if(node instanceof SCADANode) {
                    SCADANode  sNode = (SCADANode) node;
                    if(sNode.getSite().getID() == site.getID()) {
                        siteModel.reload(node);
                        found = true;
                    }
                }
                
            }
            
            if(!found)
                addSite(site);
            
        }
        
    }

    public void addSite(SCADASite site) {
        root.add(new SCADANode(site));
    }
    
    public void removeSite(SCADASite site) {
        SCADANode scadaNode = getSCADANode(site);
        if(scadaNode != null) {
            scadaNode.removeFromParent();
        }
    }
    
    SCADANode getSCADANode(SCADASite site) {
        // searches the entire tree, leafs and all
        
        return searchParent(root, site);
    }
    
    protected SCADANode searchParent(DefaultMutableTreeNode node, SCADASite site) { //returns the scadanode belonging to the site or null if not found
        
        if(!node.isLeaf()) { //if is a directory
            DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[node.getChildCount()];
            for(int i = 0; i < nodes.length; i++) {
                nodes[i] = (DefaultMutableTreeNode) node.getChildAt(i);
                SCADANode siteNode = searchParent(nodes[i], site);
                if(siteNode != null)
                    return siteNode;
            }
        } else {
            if(node instanceof SCADANode) {
                SCADANode sNode = (SCADANode) node;
                if(sNode.getSite().getID() == site.getID())
                    return sNode;
            }
        }
        
        return null;
        
    }
    
    class SCADANode extends DefaultMutableTreeNode {
        
        private SCADASite site;
        
        public SCADANode(SCADASite site) {
            this.site = site;
        }
        
        public SCADASite getSite() {
            return site;
        }
    }
    
    private class SCADACellRenderer implements TreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree jtree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = new JLabel();
            
            if(value instanceof SCADANode) {
                SCADANode node = (SCADANode) value;
                SCADASite site = node.getSite();
                label.setText(site.getName());
                
                if(site.isCritical()) {
                    label.setForeground(Color.red);
                } else if(site.isWarning()) {
                    label.setForeground(Color.orange);
                } else {
                    label.setForeground(Color.black);
                }
                
                if(selected) {
                    if(!hasFocus) {
                        label.setBackground(Color.gray.brighter());
                    } else {
                        label.setBackground(Color.cyan);
                    }
                }
                
            } else if(value instanceof DefaultMutableTreeNode){
                label.setText(((DefaultMutableTreeNode) value).toString());
            } else {
                label.setText("Invalid argument passed");
            }
            
            return label;
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
