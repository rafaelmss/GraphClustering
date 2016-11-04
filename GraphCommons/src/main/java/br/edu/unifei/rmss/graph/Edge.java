/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.graph;

import br.edu.unifei.rmss.graph.neo4j.NetworkNeo4jProperties;
import java.util.Objects;

/**
 *
 * @author rafael
 */
public class Edge {
	
    public static final int DEFAULT_WEIGHT = 1;
    
    private long id;
    private int weight;
    private Vertex startNode;
    private Vertex endNode;

    public Edge() {
    }

    public Edge(long id, Vertex start, Vertex end) {
        this.id = id;
        this.weight = DEFAULT_WEIGHT;
        this.startNode = start;
        this.endNode = end;
    }

    public Edge(long id, int weight, Vertex start, Vertex end) {
        this.id = id;
        this.weight = weight;
        this.startNode = start;
        this.endNode = end;
    }

    public int getWeight(){
        return this.weight;
    }

    public long getId(){
        return this.id;
    }

    public Vertex getOtherNode(Vertex node){
        if (node.getId() == startNode.getId()) {
            return this.endNode;
        } else {
            return this.startNode;
        }
    }

    public boolean isEdgeOnCut(){
        
        if (!this.startNode.hasPartition()){
            return false;
        }
        
        if (!this.endNode.hasPartition()){
            return false;
        }
        
        int part_1 = this.startNode.getPartition();
        int part_2 = this.endNode.getPartition();
        
        if (part_1 == part_2){
            return false;
        } else {
            return true;
        }
    }

    public Vertex getStartNode(){
        return this.startNode;
    }

    public Vertex getEndNode(){
        return this.endNode;
    }

    public void sumWeight(int weightToAdd){
        this.weight = this.weight + weightToAdd;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 83 * hash + this.weight;
        hash = 83 * hash + Objects.hashCode(this.startNode);
        hash = 83 * hash + Objects.hashCode(this.endNode);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge other = (Edge) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.weight != other.weight) {
            return false;
        }
        if (!Objects.equals(this.startNode, other.startNode)) {
            return false;
        }
        if (!Objects.equals(this.endNode, other.endNode)) {
            return false;
        }
        return true;
    }
    
    public void setInner(Edge e){
        this.id = e.getId();
        this.weight = e.getWeight();
        this.startNode = e.getStartNode();
        this.endNode = e.getEndNode();
    }

    @Override
    public String toString() {
        return "Edge{" + "id=" + id + ", weight=" + weight + ", startNode=" + startNode + ", endNode=" + endNode + '}';
    }
       
}
