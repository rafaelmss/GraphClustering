/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.communities.agglomerative.AverageLinkCommunityDetector;
import br.edu.unifei.rmss.communities.agglomerative.CompleteLinkCommunityDetector;
import br.edu.unifei.rmss.communities.agglomerative.SingleLinkCommunityDetector;
import br.edu.unifei.rmss.communities.classical.FiducciaMattheysesCommunityDetector;
import br.edu.unifei.rmss.communities.classical.KernighanLinCommunityDetector;
import br.edu.unifei.rmss.communities.greedy.DiffGreedyCommunityDetector;
import br.edu.unifei.rmss.communities.greedy.KGreedyCommunityDetector;
import br.edu.unifei.rmss.communities.greedy.MaxMinGreedyCommunityDetector;
import br.edu.unifei.rmss.communities.greedy.StandardGreedyCommunityDetector;
import br.edu.unifei.rmss.communities.multilevel.BandedDiffusionCommunityDetector;
import br.edu.unifei.rmss.communities.multilevel.HeavyEdgePairingCommunityDetector;
import br.edu.unifei.rmss.communities.multilevel.LightEdgePairingCommunityDetector;
import br.edu.unifei.rmss.communities.multilevel.LouvainCommunityDetector;
import br.edu.unifei.rmss.communities.multilevel.RandomPairingCommunityDetector;
import br.edu.unifei.rmss.communities.spectral.EigenCommunityDetector;
import br.edu.unifei.rmss.communities.spectral.NJWCommunityDetector;
import br.edu.unifei.rmss.communities.spectral.UKMeansCommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.neo4j.NetworkNeo4j;
import br.edu.unifei.rmss.utils.GraphStreamDisplay;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author rafael
 */
public class MainTest {

    public static void main(String[] args) {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
 
        String type = "cluster";
        String size = "400";

        Network graph = new NetworkNeo4j(type + "_" + size, true);
        graph.loadFromFile(type + "_" + size + ".txt");

        System.out.println("Grafo " + type + "_" + size + " montado!");
        //GraphStreamDisplay view = new GraphStreamDisplay(graph);
        //view.displayGraph();

        List<Boolean> enables = new ArrayList<>();
        enables.add(true); //CompleteLinkCommunityDetector
        enables.add(true); //SingleLinkCommunityDetector
        enables.add(true); //AverageLinkCommunityDetector
        enables.add(false); //FiducciaMattheysesCommunityDetector
        enables.add(false); //KernighanLinCommunityDetector
        enables.add(false); //BandedDiffusionCommunityDetector
        enables.add(false); //HeavyEdgePairingCommunityDetector                   error mem leaky
        enables.add(false); //LightEdgePairingCommunityDetector
        enables.add(false); //LouvainCommunityDetector
        enables.add(false); //RandomPairingCommunityDetector
        enables.add(false); //EigenCommunityDetector AVG
        enables.add(false); //EigenCommunityDetector GAP
        enables.add(false); //EigenCommunityDetector MEDIAN
        enables.add(false); //EigenCommunityDetector ZERO
        enables.add(false); //EigenCommunityDetector NJW
        enables.add(false); //EigenCommunityDetector UKMeans
        enables.add(false); //DiffGreedyCommunityDetector
        enables.add(false); //KGreedyCommunityDetector
        enables.add(false); //MaxMinGreedyCommunityDetector
        enables.add(false); //StandardGreedyCommunityDetector


        boolean start = false;
        for (int i = 0; i < enables.size(); i++) {
            if (enables.get(i)) {
                start = true;
                break;
            }
        }
        if (start) {

            if (enables.get(0)) {
                System.out.println("Algoritmo: CompleteLinkCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new CompleteLinkCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(1)) {
                System.out.println("Algoritmo: SingleLinkCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new SingleLinkCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }
            
            if (enables.get(2)) {
                System.out.println("Algoritmo: AverageLinkCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new AverageLinkCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }
            
            if (enables.get(3)) {
                System.out.println("Algoritmo: FiducciaMattheysesCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new FiducciaMattheysesCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(4)) {
                System.out.println("Algoritmo: KernighanLinCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new KernighanLinCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(5)) {
                System.out.println("Algoritmo: BandedDiffusionCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new BandedDiffusionCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(6)) {
                System.out.println("Algoritmo: HeavyEdgePairingCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new HeavyEdgePairingCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(7)) {
                System.out.println("Algoritmo: LightEdgePairingCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new LightEdgePairingCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(8)) {
                System.out.println("Algoritmo: LouvainCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new LouvainCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(9)) {
                System.out.println("Algoritmo: RandomPairingCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new RandomPairingCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(10)) {
                System.out.println("Algoritmo: EigenCommunityDetector AVG");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new EigenCommunityDetector(graph, EigenCommunityDetector.ThresholdType.AVG);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(11)) {
                System.out.println("Algoritmo: EigenCommunityDetector GAP");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new EigenCommunityDetector(graph, EigenCommunityDetector.ThresholdType.GAP);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(12)) {
                System.out.println("Algoritmo: EigenCommunityDetector MEDIAN");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new EigenCommunityDetector(graph, EigenCommunityDetector.ThresholdType.MEDIAN);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(13)) {
                System.out.println("Algoritmo: EigenCommunityDetector ZERO");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new EigenCommunityDetector(graph, EigenCommunityDetector.ThresholdType.ZERO);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(14)) {
                System.out.println("Algoritmo: EigenCommunityDetector NJW");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new NJWCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(15)) {
                System.out.println("Algoritmo: EigenCommunityDetector UKMeans");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new UKMeansCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(16)) {
                System.out.println("Algoritmo: DiffGreedyCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new DiffGreedyCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(17)) {
                System.out.println("Algoritmo: KGreedyCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new KGreedyCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(18)) {
                System.out.println("Algoritmo: MaxMinGreedyCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new MaxMinGreedyCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }

            if (enables.get(19)) {
                System.out.println("Algoritmo: StandardGreedyCommunityDetector");
                for (int i = 0; i < 10; i++) {
                    graph.resetPartitionAllNodes();

                    long delta = System.currentTimeMillis();
                    CommunityDetector detector = new StandardGreedyCommunityDetector(graph);
                    detector.compute();
                    delta = System.currentTimeMillis() - delta;

                    System.out.println((i + 1) + " - Grafo particionado em " + Math.abs(delta / 1000) + " segundos (" + Math.abs((delta / 1000) / 60) + " min) - (" + sdf.format(cal.getTime()) + ")");
                    graph.exportPartition("RESULT_" + type + "_" + size + "_" + detector.getName() + ".txt", delta);
                    //graph.exportPartition("result_teste_10.txt", delta);

                }
            }
        }
        
    }
}
