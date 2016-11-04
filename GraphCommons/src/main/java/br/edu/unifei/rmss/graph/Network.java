/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javafx.util.Pair;
import org.graphstream.graph.Graph;
import org.neo4j.helpers.collection.IteratorUtil;

/**
 *
 * @author rafael
 */
public abstract class Network {

    public boolean generateRandomGraph(int num) {

        try {

            for (int i = 0; i < num; i++) {
                createVertex(i, 1);
            }

            Random generator = new Random();
            for (int i = 0; i < Math.round(0.8 * num); i++) {

                int id1 = generator.nextInt(num);
                int id2 = generator.nextInt(num);

                if (!edgeExist(id1, id2) && id1 != id2) {
                    createEdge(1, (long) id1, (long) id2);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public boolean generateRandomConnectedGraph(int num) {

        try {

            for (int i = 0; i < num; i++) {
                createVertex(i, 1);
            }

            Random generator = new Random();

            while (!isConnected()) {

                int id1 = generator.nextInt(num);
                int id2 = generator.nextInt(num);

                if (!edgeExist(id1, id2) && id1 != id2) {

                    createEdge(1, (long) id1, (long) id2);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public boolean loadFromFile(String fileName) {

        BufferedReader br = null;
        int num_nodes = 0;

        try {
            br = new BufferedReader(new FileReader(fileName));

            String line;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (num_nodes == 0) {
                    num_nodes = Integer.valueOf(line);
                    for (int i = 0; i < num_nodes; i++) {
                        createVertex(i, 1);
                    }
                } else {
                    String[] parts = line.split(" ");
                    int part1 = Integer.valueOf(parts[0]);
                    int part2 = Integer.valueOf(parts[1]);
                    createEdge(1, (long) part1, (long) part2);
                }
                count++;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return true;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public abstract boolean isConnected();

    public abstract void updateVertex(Vertex v);

    public abstract void updateEdge(Edge e);

    public abstract Vertex createVertex(long id, int weight);

    public abstract Vertex createVertex(long id, int weight, int level, long id_origen);

    public abstract Vertex getVertex(long id);

    public abstract Vertex getVertex(long id, int level);

    public abstract Vertex getCoarsenVertex(long id, int level);

    public abstract Edge createEdge(int weight, long startNode, long endNode);

    public abstract Edge createEdge(int weight, long startNode, long endNode, int level);

    public abstract Edge createCoarsenEdge(long startNode, long endNode, int levelTop);

    public abstract Iterable<Edge> getAllEdges();

    public abstract Iterable<Edge> getAllEdges(int level);

    public abstract Iterable<Edge> getAllEdgesSortedByWight();

    public abstract Iterable<Edge> getAllEdgesSortedByWight(int level);

    public abstract Iterable<Edge> getAllEdgesSortedByWightDesc();

    public abstract Iterable<Edge> getAllEdgesSortedByWightDesc(int level);

    public abstract Iterable<Edge> getEdgesFromNode(Vertex v);

    public abstract Iterator<Long> getIdFromNeighbor(Vertex v);

    public abstract Iterable<Vertex> getNeighbor(Vertex v);

    public abstract Iterable<Vertex> getAllVertex();

    public abstract Iterable<Vertex> getAllVertex(int level);

    public abstract Iterable<Vertex> getAllVertexWithDegree(int degree);

    public abstract Iterable<Vertex> getAllVertexWithDegree(int degree, int level);

    public abstract void resetPartitionAllNodes();

    public abstract void finish();

    public abstract int getNumberOfNodes();

    public abstract int getNumberOfNodes(int level);

    public abstract int getNumberOfEdges();

    public abstract int getNumberOfEdges(int level);

    public abstract int getDegreeOfNode(long id);

    public abstract int getDegreeOfNode(long id, int level);

    public abstract boolean edgeExist(long id1, long id2);

    public abstract boolean edgeExist(long id1, long id2, int level);

    public abstract Edge getEdge(long id1, long id2);

    public abstract Edge getEdge(long id1, long id2, int level);

    public abstract Edge getEdge(long id);

    public abstract Edge getEdge(long id, int level);

    public abstract int getWeightEdges(long id);

    public abstract Iterator<Integer> getPartitions();

    public abstract int getNumberOfPartitions();
    
    public abstract Pair<Integer,Integer> getMinDistPartitions();
    
    public abstract Pair<Integer,Integer> getMaxDistPartitions();
    
    public abstract Pair<Integer,Integer> getAVGDistPartitions();
    
    public abstract int getNumVertexWithoutPartition();

    public abstract Iterable<Vertex> getPartitionVertex(int i);

    public abstract Iterable<Vertex> getPartitionVertex(int i, int level);

    public abstract Iterable<Vertex> getPartitioFrontier(int i);

    public abstract Iterable<Vertex> getPartitioFrontierSortedByWight(int i);

    public abstract int getVertexFrontierWeight(long id, int partition);

    public abstract Iterable<Vertex> getFrontierBetweenPartitions(int partition1, int partition2, int num);

    public abstract Network getSubnetworkFrontier(int partition1, int partition2, int num);

    public abstract int getSubnetworkFrontierNum(int partition1, int partition2, int num);

    public abstract int getCutWeight();

    public abstract List<Pair> getPatitionsWithNumVertex();

    public abstract int getExpansion();

    public abstract int getConductance();

    public abstract int gainOfVertex(Vertex v);

    public abstract List<Pair> gainOfAllVertex();

    public abstract void uncoarsen();

    public abstract double getGlobalClusteringCoefficient();

    public abstract double getGlobalClusteringCoefficient(int partition);

    public abstract double getVertexLocalClusteringCoefficient(long index);

    public abstract double getVertexLocalClusteringCoefficient(long index, int partition);

    public abstract double getLocalClusteringCoefficient();

    public abstract double getLocalClusteringCoefficient(int partition);

    public double getGCC() {

        double value = 0;
        int num = 0;

        Iterator<Integer> partitions = getPartitions();
        while (partitions.hasNext()) {
            int partition = partitions.next();
            value += getGlobalClusteringCoefficient(partition);
            num++;
        }

        if (num > 0) {
            value = value / num;
        }

        return value;
    }

    public double getLCC() {

        double value = 0;
        int num = 0;

        Iterator<Integer> partitions = getPartitions();
        while (partitions.hasNext()) {
            int partition = partitions.next();
            value += getLocalClusteringCoefficient(partition);
            num++;
        }

        if (num > 0) {
            value = value / num;
        }

        return value;

    }

    public void exportPartition(String fileName, long timeSpent) {

        File dir = new File("RESULTS"); 
        if (!dir.exists()) {
            dir.mkdirs(); 
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        StringBuffer bf = new StringBuffer();
        bf.append("|Total of nodes|" + this.getNumberOfNodes()
                + "|Time to run|" + timeSpent
                + "|Num. of partitions|" + this.getNumberOfPartitions()
                + "|Cut weight|" + this.getCutWeight()
                + "|GCC Medium|" + String.format("%.2f", this.getGCC())
                + "|LCC Medium|" + String.format("%.2f", this.getLCC()));

        for (Iterator it = getPartitions(); it.hasNext();) {
            int part = (int) it.next();
            int count = IteratorUtil.count(getPartitionVertex(part));
            bf.append("|Num vertex in part "+part+"|"+count);
        }
        bf.append("\n");

        try {
            FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath()+"/"+fileName, true);
            fos.write(bf.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            System.err.println("Erro ao gerar Arquivo");
        }
    }
}
