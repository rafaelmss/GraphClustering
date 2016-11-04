package br.edu.unifei.rmss.graphgenerator.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public abstract class BaseGenerator {
    
    private int size;
    private int partitions;
    protected Graph graph;    
    protected Generator gen;
    
    public BaseGenerator(int size, int partitions) {
        this.size = size;
        this.partitions = partitions;
        
        graph = new SingleGraph("mygraph");
        graph.setStrict(false);
        graph.setAutoCreate(true);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public void generate(){
        gen.begin();
        process();                
        graph.addAttribute("ui.stylesheet", "node { fill-color: blue; }");
        gen.end();
    }
    
    protected abstract void process();
    
    public void displayGraph(){        
        graph.display();
    }
          
    public void exportGraph(String fileName){
          
        FileWriter arquivo;  
          
        try {  
            arquivo = new FileWriter(new File(fileName+".txt"));  
            
            arquivo.write(graph.getNodeCount()+"\n");
            for (Edge edge : graph.getEachEdge()) {
                int n1 = edge.getNode0().getIndex();
                int n2 = edge.getNode1().getIndex();
                arquivo.write(n1+" "+n2+"\n");
            }
            
            arquivo.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
    }
    
}
