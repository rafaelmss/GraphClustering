/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.utils;

import br.edu.unifei.rmss.graph.Edge;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import javafx.scene.paint.Color;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

/**
 *
 * @author rafael
 */
public class GraphStreamDisplay {
    
    private Network network;
    protected Graph graph;    
    
    public GraphStreamDisplay (Network network){
        this.network = network;        
        graph = new SingleGraph("chargedGraph");
        graph.setStrict(false);
        graph.setAutoCreate(false);
        graph.addAttribute("ui.stylesheet", "node { fill-color: blue; }");
        chargeData();
    }
    
    private void chargeData(){
        
        //Load all vertex
        for (Vertex v:network.getAllVertex()) {
            Node n = graph.addNode(String.valueOf(v.getId()));       
        }
        
        //Load all edge
        for (Edge e:network.getAllEdges()) {
            String n1 = String.valueOf(e.getStartNode().getId());
            String n2 = String.valueOf(e.getEndNode().getId());
            graph.addEdge(String.valueOf(e.getId()),n1,n2);
        }
    
    }
    
    public void displayGraph(){        
        graph.display();
    }    
    
}
