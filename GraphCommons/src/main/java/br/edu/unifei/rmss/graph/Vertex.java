/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.graph;

/**
 *
 * @author rafael
 */
public class Vertex {
	
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_PARTITION_NONE = -1;
    public static final int DEFAULT_LEVEL = 0;
        
    private long id;
    private int weight;
    private int partition;
    private int level;

    public Vertex(long id) {
        this.id = id;
        this.weight = DEFAULT_WEIGHT;
        this.partition = DEFAULT_PARTITION_NONE;
        this.level = DEFAULT_LEVEL;
    }

    public Vertex(long id, int weight) {
        this.id = id;
        this.weight = weight;
        this.partition = DEFAULT_PARTITION_NONE;
        this.level = DEFAULT_LEVEL;
    }

    public Vertex(long id, int weight, int level) {
        this.id = id;
        this.weight = weight;
        this.partition = DEFAULT_PARTITION_NONE;
        this.level = level;
    }

    public Vertex(long id, int weight, int partition, int level) {
        this.id = id;
        this.weight = weight;
        this.partition = partition;
        this.level = level;
    }

    public Vertex() {
        
    }

    public long getId(){
        return this.id;
    };

    public int getWeight(){
        return this.weight;
    };;

    public int getPartition(){
        return this.partition;
    };

    public int getLevel(){
        return this.level;
    };

    public void setWeight(Integer weight){
        this.weight = weight;
    };

    public void setPartition(Integer partition){
        this.partition = partition;
    };    

    public void resetPartition(){
        this.partition = DEFAULT_PARTITION_NONE;
    };

    public boolean hasPartition(){
        if (this.partition != DEFAULT_PARTITION_NONE){
            return true;
        } else {
            return false;
        }
    };

    public boolean hasLevel(){
        if (this.level != DEFAULT_LEVEL){
            return true;
        } else {
            return false;
        }
    };

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 53 * hash + this.weight;
        hash = 53 * hash + this.partition;
        hash = 53 * hash + this.level;
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
        final Vertex other = (Vertex) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.weight != other.weight) {
            return false;
        }
        if (this.partition != other.partition) {
            return false;
        }
        if (this.level != other.level) {
            return false;
        }
        return true;
    }
    
    public void setInner(Vertex v){
        this.id = v.getId();
        this.weight = v.getWeight();
        this.partition = v.getPartition();
        this.level = v.getLevel();
    }

    @Override
    public String toString() {
        return "Vertex{" + "id=" + id + ", weight=" + weight + ", partition=" + partition + ", level=" + level + '}';
    }
        
}
