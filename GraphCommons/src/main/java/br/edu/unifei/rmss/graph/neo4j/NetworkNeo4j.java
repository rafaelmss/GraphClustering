/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.graph.neo4j;

import br.edu.unifei.rmss.graph.Edge;
import br.edu.unifei.rmss.graph.EdgeIterable;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import br.edu.unifei.rmss.graph.VertexIterable;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;

/**
 *
 * @author rafael
 */
public class NetworkNeo4j extends Network {

    private String networkFileName;

    public static RelationshipType LINK = DynamicRelationshipType.withName("LINK");
    public static RelationshipType COARSEN = DynamicRelationshipType.withName("COARSEN");

    public static Label NODE = DynamicLabel.label("NODE");
    public static Label COARSEN_NODE = DynamicLabel.label("COARSEN_NODE");

    private GraphDatabaseService graphDb;

    private Transaction tx = null;

    //construtor da classe de grafo
    public NetworkNeo4j(String networFileName) {
        this.networkFileName = "graph4j_" + networFileName;
        initDB();
    }

    //construtor da classe de grafo
    public NetworkNeo4j(String networFileName, boolean removeOld) {
        this.networkFileName = "graph4j_" + networFileName;
        File db = new File(this.networkFileName);
        if (removeOld) {
            deleteFileOrDirectory(db);
        }
        initDB();
    }

    //iniciar conexões com o banco de dados
    private boolean initDB() {
        File db = new File(this.networkFileName);
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(db);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
        //graphDb.beginTx();
        return true;
    }

    //iniciar uma transação de banco de dados
    private void beginTransaction() {
        tx = graphDb.beginTx();
    }

    //concluir uma transação de banco de dados
    private void endTransaction() {
        tx.success();
        tx.close();
    }

    //encerra a conexão com o banco de dados
    public void shutdown() {
        if (graphDb != null) {
            graphDb.shutdown();
            graphDb = null;

            File f = new File(this.networkFileName);
            //deleteFileOrDirectory(f);
        }
    }

    @Override
    public boolean isConnected() {
        boolean connected = false;

        try {
            beginTransaction();

            Result result = graphDb.execute("MATCH (a:NODE)-[r:LINK*]-(b), (c:NODE) "
                    + "WITH count(distinct(b)) AS connected, count(distinct(c)) AS total "
                    + "RETURN total, connected");

            if (result.hasNext()) {
                Map<String, Object> row = result.next();
                long total = (long) row.get("total");
                long conn = (long) row.get("connected");

                if (conn == total) {

                    if (total != 0) {
                        connected = true;
                    }
                }
            }

        } finally {
            endTransaction();
        }

        return connected;
    }

    @Override
    public void updateEdge(Edge e) {
        try {

            beginTransaction();

            //modificar
            if (e.getEndNode().hasLevel()) {

                Relationship rel = getInnerRelationship(e.getId(), e.getEndNode().getLevel());

                if (rel != null) {
                    rel.setProperty(NetworkNeo4jProperties.WEIGHT, e.getWeight());
                } else {
                    createEdge(e.getWeight(), e.getStartNode().getId(), e.getEndNode().getId(), e.getEndNode().getLevel());
                }

            } else {

                Relationship rel = getInnerRelationship(e.getId());
                if (rel != null) {
                    rel.setProperty(NetworkNeo4jProperties.WEIGHT, e.getWeight());
                } else {
                    createEdge(e.getWeight(), e.getStartNode().getId(), e.getEndNode().getId());
                }

            }

        } finally {
            endTransaction();
        }
    }

    @Override
    public void updateVertex(Vertex v) {
        try {
            beginTransaction();

            if (v.hasLevel()) {
                Result result = graphDb.execute("MERGE (a:COARSEN_NODE{INDEX:" + v.getId() + ", LEVEL:" + v.getLevel() + "}) RETURN a");
                Iterator<Node> n_column = result.columnAs("a");
                Node node = null;
                for (Node n : IteratorUtil.asIterable(n_column)) {
                    node = n;
                }

                node.setProperty(NetworkNeo4jProperties.WEIGHT, v.getWeight());

                if (v.getPartition() == Vertex.DEFAULT_PARTITION_NONE) {
                    node.removeProperty(NetworkNeo4jProperties.PARTITION);
                } else {
                    node.setProperty(NetworkNeo4jProperties.PARTITION, v.getPartition());
                }

            } else {
                Result result = graphDb.execute("MERGE (a:NODE{INDEX:" + v.getId() + "}) RETURN a");
                Iterator<Node> n_column = result.columnAs("a");
                Node node = null;
                for (Node n : IteratorUtil.asIterable(n_column)) {
                    node = n;
                }

                node.setProperty(NetworkNeo4jProperties.WEIGHT, v.getWeight());

                if (v.getPartition() == Vertex.DEFAULT_PARTITION_NONE) {
                    node.removeProperty(NetworkNeo4jProperties.PARTITION);
                } else {
                    node.setProperty(NetworkNeo4jProperties.PARTITION, v.getPartition());
                }
            }

        } finally {
            endTransaction();
        }
    }

    @Override
    public Vertex createVertex(long id, int weight) {
        Vertex v = null;
        try {
            beginTransaction();
            Result result = graphDb.execute("MERGE (a:NODE{INDEX:" + id + "}) RETURN a");
            Iterator<Node> n_column = result.columnAs("a");
            Node node = null;
            for (Node n : IteratorUtil.asIterable(n_column)) {
                node = n;
            }
            node.setProperty(NetworkNeo4jProperties.WEIGHT, weight);
            node.setProperty(NetworkNeo4jProperties.LEVEL, (int) 0);
            v = new Vertex(id, weight);
        } finally {
            endTransaction();
        }

        return v;
    }

    @Override
    public Vertex createVertex(long id, int weight, int level, long id_origen) {

        if (level == 0) {
            return createVertex(id, weight);
        }

        Vertex v = null;

        try {

            beginTransaction();

            Node node = null;
            Node origen = null;

            Result result = graphDb.execute("MERGE (a:COARSEN_NODE{INDEX:" + id + ",WEIGHT:" + weight + ",LEVEL:" + level + "}) RETURN a");
            Iterator<Node> n_column = result.columnAs("a");
            for (Node n : IteratorUtil.asIterable(n_column)) {
                node = n;
            }
            node.setProperty(NetworkNeo4jProperties.WEIGHT, weight);
            node.setProperty(NetworkNeo4jProperties.LEVEL, level);

            v = new Vertex(id, weight, level);

            if (level - 1 == 0) {
                origen = getInnerNode(id_origen);
            } else {
                origen = getInnerNode(id_origen, level - 1);
            }

            if (origen != null) {

                Relationship edge = origen.createRelationshipTo(node, COARSEN);

            }

        } finally {

            endTransaction();

        }

        return v;
    }

    @Override
    public Vertex getVertex(long id) {
        Vertex v = null;
        try {
            beginTransaction();
            Node node = getInnerNode(id);

            int partition = Vertex.DEFAULT_PARTITION_NONE;
            int level = Vertex.DEFAULT_LEVEL;

            if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
            }

            if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
            }

            v = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                    (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                    partition,
                    level);

        } finally {
            endTransaction();
        }
        return v;
    }

    @Override
    public Vertex getVertex(long id, int level) {
        if (level == 0) {
            return getVertex(id);
        }

        Vertex v = null;
        try {
            beginTransaction();
            Node node = getInnerNode(id, level);

            int partition = Vertex.DEFAULT_PARTITION_NONE;

            if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
            }

            v = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                    (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                    partition,
                    level);

        } finally {
            endTransaction();
        }

        return v;
    }

    @Override
    public Vertex getCoarsenVertex(long id, int level) {

        Vertex v = null;

        try {
            beginTransaction();

            Result result = null;

            if (level == 0) {
                result = graphDb.execute("MATCH (a:NODE{INDEX:" + id + "})-[r:COARSEN]->(b) RETURN b LIMIT 1");
            } else {
                result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + id + ", LEVEL:" + level + "})-[r:COARSEN]->(b) RETURN b LIMIT 1");
            }

            Node node_result = null;
            Iterator<Node> n_column = result.columnAs("b");
            for (Node node : IteratorUtil.asIterable(n_column)) {
                node_result = node;
            }

            if (node_result != null) {

                int partition = Vertex.DEFAULT_PARTITION_NONE;

                if (node_result.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    partition = (int) node_result.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                v = new Vertex((long) node_result.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node_result.getProperty(NetworkNeo4jProperties.WEIGHT),
                        partition,
                        level + 1);

            }

        } finally {
            endTransaction();
        }

        return v;

    }

    @Override
    public Edge getEdge(long id1, long id2) {
        Edge edge = null;
        try {
            beginTransaction();
            Relationship rel = getInnerRelationship(id1, id2);
            if (rel != null) {
                Vertex v1 = getVertex(id1);
                Vertex v2 = getVertex(id2);

                int weigth = 0;
                if (rel.hasProperty(NetworkNeo4jProperties.WEIGHT)) {
                    weigth = (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT);
                }

                long id = (long) rel.getProperty(NetworkNeo4jProperties.INDEX);

                edge = new Edge(id, weigth, v1, v2);
            }

        } finally {
            endTransaction();
        }

        return edge;
    }

    @Override
    public Edge getEdge(long id1, long id2, int level) {
        if (level == 0) {
            return getEdge(id1, id2);
        }

        Edge edge = null;
        try {
            beginTransaction();
            Relationship rel = getInnerRelationship(id1, id2, level);

            if (rel != null) {
                Vertex v1 = getVertex(id1, level);
                Vertex v2 = getVertex(id2, level);

                int weigth = 0;
                if (rel.hasProperty(NetworkNeo4jProperties.WEIGHT)) {
                    weigth = (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT);
                }

                long id = (long) rel.getProperty(NetworkNeo4jProperties.INDEX);

                edge = new Edge(id, weigth, v1, v2);
            }
        } finally {
            endTransaction();
        }

        return edge;
    }

    @Override
    public Edge getEdge(long id) {
        Edge edge = null;
        try {
            beginTransaction();
            Relationship rel = getInnerRelationship(id);
            if (rel != null) {
                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX));
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX));

                int weigth = 0;
                if (rel.hasProperty(NetworkNeo4jProperties.WEIGHT)) {
                    weigth = (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT);
                }

                edge = new Edge(id, weigth, v1, v2);
            }

        } finally {
            endTransaction();
        }

        return edge;
    }

    @Override
    public Edge getEdge(long id, int level) {
        if (level == 0) {
            return getEdge(id);
        }

        Edge edge = null;
        try {
            beginTransaction();
            Relationship rel = getInnerRelationship(id, (int) level);
            if (rel != null) {
                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX));
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX));

                int weigth = 0;
                if (rel.hasProperty(NetworkNeo4jProperties.WEIGHT)) {
                    weigth = (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT);
                }

                edge = new Edge(id, weigth, v1, v2);
            }
        } finally {
            endTransaction();
        }

        return edge;
    }

    @Override
    public int getWeightEdges(long id) {

        Iterator<Integer> list;
        int weight = 0;

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH  (a:NODE{INDEX:" + id + "})-[r:LINK]-(b) "
                    + "RETURN SUM(r.WEIGHT) AS total");
            list = result.columnAs("total");
            if (list.hasNext()) {
                int value = list.next();
                weight = (int) value;
            }
        } finally {
            endTransaction();
        }

        return weight;
    }

    @Override
    public Edge createEdge(int weight, long startNode, long endNode) {
        Edge edge = null;
        long id = 0;
        try {
            beginTransaction();

            Result result = graphDb.execute("MATCH (a:NODE)-[r:LINK]->(b:NODE) RETURN r ORDER BY r.INDEX DESC LIMIT 1");
            Iterator<Relationship> edges = result.columnAs("r");
            if (edges.hasNext()) {
                id = ((int) edges.next().getId()) + 1;
            }

            result = graphDb.execute("MATCH (a:NODE{INDEX:" + startNode + "})-[r:LINK]-(b:NODE{INDEX:" + endNode + "}) RETURN r ORDER BY r.INDEX DESC LIMIT 1");
            edges = result.columnAs("r");
            if (!edges.hasNext()) {
                Node firstNode = getInnerNode(startNode);
                Node secondNode = getInnerNode(endNode);

                result = graphDb.execute("MATCH (a:NODE{INDEX:" + startNode + "}), (b:NODE{INDEX:" + endNode + "}) "
                        + "CREATE (a)-[r:LINK]->(b) "
                        + "RETURN r");

                Iterator<Relationship> n_column = result.columnAs("r");
                Relationship rel = null;
                for (Relationship r : IteratorUtil.asIterable(n_column)) {
                    rel = r;
                }
                rel.setProperty(NetworkNeo4jProperties.INDEX, id);
                rel.setProperty(NetworkNeo4jProperties.WEIGHT, weight);

                Vertex v1 = getVertex(startNode);
                Vertex v2 = getVertex(endNode);

                edge = new Edge(id, weight, v1, v2);
            }

        } finally {
            endTransaction();
        }

        return edge;
    }

    @Override
    public Edge createEdge(int weight, long startNode, long endNode, int level) {
        if (level == 0) {
            return createEdge(weight, startNode, endNode);
        }

        Edge edge = getEdge(startNode, endNode, level);

        if (edge == null) {

            long id = 0;
            try {
                beginTransaction();

                Result result = graphDb.execute("MATCH (a:COARSEN_NODE{LEVEL:" + level + "})-[r:LINK]->(b:COARSEN_NODE{LEVEL:" + level + "}) RETURN r ORDER BY r.INDEX DESC LIMIT 1");
                Iterator<Relationship> edges = result.columnAs("r");
                if (edges.hasNext()) {
                    id = ((int) edges.next().getId()) + 1;
                }

                result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + startNode + ", LEVEL:" + level + "})-[r:LINK]-(b:COARSEN_NODE{INDEX:" + endNode + ", LEVEL:" + level + "}) RETURN r ORDER BY r.INDEX DESC LIMIT 1");
                edges = result.columnAs("r");
                if (!edges.hasNext()) {
                    Node firstNode = getInnerNode(startNode);
                    Node secondNode = getInnerNode(endNode);

                    result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + startNode + ", LEVEL:" + level + "}), (b:COARSEN_NODE{INDEX:" + endNode + ", LEVEL:" + level + "}) "
                            + "CREATE (a)-[r:LINK]->(b) "
                            + "RETURN r");

                    Iterator<Relationship> n_column = result.columnAs("r");
                    Relationship rel = null;
                    for (Relationship r : IteratorUtil.asIterable(n_column)) {
                        rel = r;
                    }
                    rel.setProperty(NetworkNeo4jProperties.INDEX, id);
                    rel.setProperty(NetworkNeo4jProperties.WEIGHT, weight);

                    Vertex v1 = getVertex(startNode);
                    Vertex v2 = getVertex(endNode);

                    edge = new Edge(id, weight, v1, v2);
                }

            } finally {
                endTransaction();
            }

        }

        return edge;

    }

    @Override
    public Edge createCoarsenEdge(long startNode, long endNode, int level) {

        Edge edge = null;

        try {
            beginTransaction();

            Node firstNode = getInnerNode(startNode, level - 1);
            Node secondNode = getInnerNode(endNode, level);

            Relationship e = firstNode.createRelationshipTo(secondNode, COARSEN);

            Vertex v1 = getVertex(startNode, level - 1);
            Vertex v2 = getVertex(endNode, level);

            edge = new Edge(0, v1, v2);

        } finally {
            endTransaction();
        }

        return edge;

    }

    @Override
    public Iterable<Edge> getAllEdges() {
        List<Edge> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE)-[r:LINK]->(b:NODE) RETURN r ORDER BY r.INDEX");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("r");

                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX));
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX));

                Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                        v1,
                        v2);
                list.add(edge);
            }
        } finally {
            endTransaction();
        }
        return new EdgeIterable<Edge>(list.iterator(), new Edge());
    }

    @Override
    public Iterable<Edge> getAllEdges(int level) {
        if (level == 0) {
            return getAllEdges();
        }

        List<Edge> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:COARSEN_NODE{LEVEL:" + level + "})-[r:LINK]->(b:COARSEN_NODE{LEVEL:" + level + "}) RETURN r ORDER BY r.INDEX");

            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("r");

                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX), level);
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX), level);

                Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                        v1,
                        v2);
                list.add(edge);
            }
        } finally {
            endTransaction();
        }
        return new EdgeIterable<Edge>(list.iterator(), new Edge());
    }

    @Override
    public Iterable<Edge> getAllEdgesSortedByWight() {
        List<Edge> list = new ArrayList<>();

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE)-[r:LINK]->(b:NODE) RETURN r ORDER BY r.WIGHT ASC");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("r");

                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX));
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX));

                Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                        v1,
                        v2);
                list.add(edge);
            }
        } finally {
            endTransaction();
        }
        return new EdgeIterable<Edge>(list.iterator(), new Edge());
    }

    @Override
    public Iterable<Edge> getAllEdgesSortedByWight(int level) {
        if (level == 0) {
            return getAllEdgesSortedByWight();
        }

        List<Edge> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:COARSEN_NODE{LEVEL:" + level + "})-[r:LINK]->(b:COARSEN_NODE{LEVEL:" + level + "}) RETURN r ORDER BY r.WIGHT ASC");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("r");

                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX), level);
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX), level);

                Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                        v1,
                        v2);
                list.add(edge);
            }
        } finally {
            endTransaction();
        }
        return new EdgeIterable<Edge>(list.iterator(), new Edge());
    }

    @Override
    public Iterable<Edge> getAllEdgesSortedByWightDesc() {
        List<Edge> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE)-[r:LINK]->(b:NODE) RETURN r ORDER BY r.WIGHT DESC");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("r");

                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX));
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX));

                Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                        v1,
                        v2);
                list.add(edge);
            }
        } finally {
            endTransaction();
        }
        return new EdgeIterable<Edge>(list.iterator(), new Edge());
    }

    @Override
    public Iterable<Edge> getAllEdgesSortedByWightDesc(int level) {
        if (level == 0) {
            return getAllEdgesSortedByWight();
        }

        List<Edge> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:COARSEN_NODE{LEVEL:" + level + "})-[r:LINK]->(b:COARSEN_NODE{LEVEL:" + level + "}) RETURN r ORDER BY r.WIGHT DESC");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("r");

                Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX), level);
                Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX), level);

                Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                        v1,
                        v2);
                list.add(edge);
            }
        } finally {
            endTransaction();
        }
        return new EdgeIterable<Edge>(list.iterator(), new Edge());
    }

    @Override
    public Iterable<Edge> getEdgesFromNode(Vertex v) {
        List<Edge> list = new ArrayList<>();

        if (!v.hasLevel()) {

            try {

                beginTransaction();
                Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + v.getId() + "})-[r:LINK]-(b) RETURN r");
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    Relationship rel = (Relationship) row.get("r");

                    Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX));
                    Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX));

                    Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                            (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                            v1,
                            v2);
                    list.add(edge);
                }
            } finally {
                endTransaction();
            }

        } else {

            try {
                beginTransaction();
                int level = v.getLevel();
                Result result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + v.getId() + ", LEVEL:" + level + "})-[r:LINK]-(b) RETURN r");
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    Relationship rel = (Relationship) row.get("r");

                    Vertex v1 = getVertex((long) rel.getStartNode().getProperty(NetworkNeo4jProperties.INDEX), level);
                    Vertex v2 = getVertex((long) rel.getEndNode().getProperty(NetworkNeo4jProperties.INDEX), level);

                    Edge edge = new Edge((long) rel.getProperty(NetworkNeo4jProperties.INDEX),
                            (int) rel.getProperty(NetworkNeo4jProperties.WEIGHT),
                            v1,
                            v2);
                    list.add(edge);
                }
            } finally {
                endTransaction();
            }

        }
        return new EdgeIterable<Edge>(list.iterator(), new Edge());
    }

    @Override
    public Iterator<Long> getIdFromNeighbor(Vertex v) {

        List<Long> list = new ArrayList<>();
        //Iterator<Long> list;

        int level = v.getLevel();
        if (level != 0) {

            try {
                beginTransaction();
                Result result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + v.getId() + ", LEVEL:" + level + "})-[r:LINK]-(n:COARSEN_NODE) RETURN n.INDEX");
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    long id = (long) row.get("n.INDEX");
                    list.add(id);
                }

            } finally {
                endTransaction();
            }

        } else {

            try {
                beginTransaction();
                Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + v.getId() + "})-[r:LINK]-(n) RETURN n.INDEX");
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    long id = (long) row.get("n.INDEX");
                    list.add(id);
                }
            } finally {
                endTransaction();
            }

        }
        return list.iterator();
    }

    @Override
    public Iterable<Vertex> getNeighbor(Vertex v) {

        List<Vertex> list = new ArrayList<>();

        int level = v.getLevel();
        if (level != 0) {

            try {
                beginTransaction();
                //Iterator<Node> nodes = null;
                Result result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + v.getId() + ", LEVEL:" + level + "})-[r:LINK]-(n:COARSEN_NODE) RETURN n");
                //nodes = result.columnAs("n"); 
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    Node node = (Node) row.get("n");

                    int vert_level = Vertex.DEFAULT_LEVEL;
                    if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                        vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                    }

                    int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                    if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                        vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                    }

                    Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                            (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                            vert_partition,
                            vert_level);
                    list.add(vert);
                }
            } finally {
                endTransaction();
            }

        } else {

            try {
                beginTransaction();
                //Iterator<Node> nodes = null;
                Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + v.getId() + "})-[r:LINK]-(n) RETURN n.");
                //nodes = result.columnAs("n"); 
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    Node node = (Node) row.get("n");

                    int vert_level = Vertex.DEFAULT_LEVEL;
                    if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                        vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                    }

                    int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                    if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                        vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                    }

                    Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                            (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                            vert_partition,
                            vert_level);
                    list.add(vert);
                }
            } finally {
                endTransaction();
            }

        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Iterable<Vertex> getAllVertex() {

        List<Vertex> list = new ArrayList<>();

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE) RETURN a ORDER BY a.INDEX");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("a");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Iterable<Vertex> getAllVertex(int level) {
        if (level == 0) {
            return getAllVertex();
        }

        List<Vertex> list = new ArrayList<>();

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:COARSEN_NODE{LEVEL:" + level + "}) RETURN a ORDER BY a.INDEX");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("a");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Iterable<Vertex> getAllVertexWithDegree(int degree) {

        List<Vertex> list = new ArrayList<>();

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (n:NODE) "
                    + "WHERE size((n)-[:LINK]-()) = " + degree
                    + "RETURN n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("n");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Iterable<Vertex> getAllVertexWithDegree(int degree, int level) {
        if (level == 0) {
            return getAllVertexWithDegree(degree);
        }

        List<Vertex> list = new ArrayList<>();

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (n:COARSEN_NODE{LEVEL:" + level + "}) "
                    + "WHERE size((n)-[:LINK]-()) = " + degree
                    + "RETURN n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("n");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public void resetPartitionAllNodes() {
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE) REMOVE a.PARTITION RETURN a");
        } finally {
            endTransaction();
        }
    }

    @Override
    public int getNumberOfNodes() {
        return IteratorUtil.count(getAllVertex());
    }

    @Override
    public int getNumberOfNodes(int level) {
        return IteratorUtil.count(getAllVertex(level));
    }

    @Override
    public int getNumberOfEdges() {
        return IteratorUtil.count(getAllEdges());
    }

    @Override
    public int getNumberOfEdges(int level) {
        return IteratorUtil.count(getAllEdges(level));
    }

    @Override
    public void finish() {
        shutdown();
    }

    //método interno para busca de nós
    private Node getInnerNode(long id) {
        Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + String.valueOf(id) + "}) RETURN a LIMIT 1");
        Node node_result = null;
        Iterator<Node> n_column = result.columnAs("a");
        for (Node node : IteratorUtil.asIterable(n_column)) {
            node_result = node;
        }
        return node_result;
    }

    //método interno para busca de nós
    private Node getInnerNode(long id, int level) {
        if (level == 0) {
            return getInnerNode(id);
        }
        Result result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + String.valueOf(id) + ", LEVEL:" + level + "}) RETURN a LIMIT 1");
        Node node_result = null;
        Iterator<Node> n_column = result.columnAs("a");
        for (Node node : IteratorUtil.asIterable(n_column)) {
            node_result = node;
        }
        return node_result;
    }

    //método interno para busca de arestas
    private Relationship getInnerRelationship(long id) {
        Result result = graphDb.execute("MATCH (a:NODE)-[r:LINK{INDEX:" + String.valueOf(id) + "}]-(b:NODE) RETURN r LIMIT 1");
        Relationship rel_result = null;
        Iterator<Relationship> n_column = result.columnAs("r");
        for (Relationship rel : IteratorUtil.asIterable(n_column)) {
            rel_result = rel;
        }
        return rel_result;
    }

    //método interno para busca de arestas
    private Relationship getInnerRelationship(long id, int level) {
        if (level == 0) {
            return getInnerRelationship(id);
        }
        Result result = graphDb.execute("MATCH (a:COARSEN_NODE{LEVEL:" + level + "})-[r:LINK]-(b:COARSEN_NODE{LELVEL:" + level + "}) RETURN r LIMIT 1");
        Relationship rel_result = null;
        Iterator<Relationship> n_column = result.columnAs("r");
        for (Relationship rel : IteratorUtil.asIterable(n_column)) {
            rel_result = rel;
        }
        return rel_result;
    }

    //método interno para busca de arestas
    private Relationship getInnerRelationship(long id1, long id2) {
        Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + id1 + "})-[r:LINK]-(b:NODE{INDEX:" + id2 + "}) RETURN r LIMIT 1");
        Relationship rel_result = null;
        Iterator<Relationship> n_column = result.columnAs("r");
        for (Relationship rel : IteratorUtil.asIterable(n_column)) {
            rel_result = rel;
        }
        return rel_result;
    }

    //método interno para busca de arestas
    private Relationship getInnerRelationship(long id1, long id2, int level) {
        if (level == 0) {
            return getInnerRelationship(id1, id2);
        }
        Result result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + id1 + ", LEVEL:" + level + "})-[r:LINK]-(b:COARSEN_NODE{INDEX:" + id2 + ", LEVEL:" + level + "}) RETURN r LIMIT 1");
        Relationship rel_result = null;
        Iterator<Relationship> n_column = result.columnAs("r");
        for (Relationship rel : IteratorUtil.asIterable(n_column)) {
            rel_result = rel;
        }
        return rel_result;
    }

    @Override
    public int getDegreeOfNode(long id) {
        Vertex v = getVertex(id);
        Iterator<Long> neighbor = getIdFromNeighbor(v);
        return IteratorUtil.count(neighbor);
    }

    @Override
    public int getDegreeOfNode(long id, int level) {
        if (level == 0) {
            return getDegreeOfNode(id);
        }
        Vertex v = getVertex(id, level);
        Iterator<Long> neighbor = getIdFromNeighbor(v);
        return IteratorUtil.count(neighbor);
    }

    @Override
    public boolean edgeExist(long id1, long id2) {
        boolean res = false;
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + String.valueOf(id1) + "})-[r:LINK]-(b:NODE{INDEX:" + String.valueOf(id2) + "}) RETURN r LIMIT 1");
            Relationship rel_result = null;
            Iterator<Relationship> n_column = result.columnAs("r");
            for (Relationship rel : IteratorUtil.asIterable(n_column)) {
                rel_result = rel;
            }
            if (rel_result != null) {
                res = false;
            }
        } finally {
            endTransaction();
        }

        return res;
    }

    @Override
    public boolean edgeExist(long id1, long id2, int level) {
        if (level == 0) {
            return edgeExist(id1, id2);
        }
        boolean res = false;
        try {
            Result result = graphDb.execute("MATCH (a:COARSEN_NODE{INDEX:" + String.valueOf(id1) + ", LEVEL:" + level + "})-[r:LINK]-(b:COARSEN_NODE{INDEX:" + String.valueOf(id2) + ", LEVEL:" + level + "}) RETURN r LIMIT 1");
            Relationship rel_result = null;
            Iterator<Relationship> n_column = result.columnAs("r");
            for (Relationship rel : IteratorUtil.asIterable(n_column)) {
                rel_result = rel;
            }
        } finally {
            endTransaction();
        }

        return res;
    }

    @Override
    public Iterator<Integer> getPartitions() {
        List<Integer> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (n:NODE) RETURN DISTINCT n.PARTITION ORDER BY n.PARTITION");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                int partition = (int) row.get("n.PARTITION");
                list.add(partition);
            }
        } finally {
            endTransaction();
        }
        return list.iterator();
    }

    @Override
    public int getNumberOfPartitions() {
        return IteratorUtil.count(getPartitions());
    }

    @Override
    public Pair<Integer,Integer> getMinDistPartitions() {

        Pair<Integer, Integer> pair = null;
        
        try {
            beginTransaction();
            
            Result result = graphDb.execute("MATCH  (startNode:NODE), (endNode:NODE)," +
                                            "path = shortestPath((startNode)-[*]-(endNode)) " +
                                            "WHERE startNode.PARTITION <> endNode.PARTITION " +
                                            "RETURN LENGTH(path),startNode.PARTITION AS part1,endNode.PARTITION AS part2 " +
                                            "ORDER BY LENGTH(path) ASC LIMIT 1");
            
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                int part1 = (int) row.get("part1");
                int part2 = (int) row.get("part2");
                pair = new Pair<>(part1,part2);
            }

        } finally {
            endTransaction();
        }

        return pair;

    }

    @Override
    public Pair<Integer,Integer> getMaxDistPartitions() {

        Pair<Integer, Integer> pair = null;
        
        try {
            beginTransaction();
            
            Result result = graphDb.execute("MATCH  (startNode:NODE), (endNode:NODE)," +
                                            "path = shortestPath((startNode)-[*]-(endNode)) " +
                                            "WHERE startNode.PARTITION <> endNode.PARTITION " +
                                            "RETURN LENGTH(path),startNode.PARTITION AS part1,endNode.PARTITION AS part2 " +
                                            "ORDER BY LENGTH(path) DESC LIMIT 1");
            
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                int part1 = (int) row.get("part1");
                int part2 = (int) row.get("part2");
                pair = new Pair<>(part1,part2);
            }

        } finally {
            endTransaction();
        }

        return pair;

    }

    @Override
    public Pair<Integer,Integer> getAVGDistPartitions() {

        Pair<Integer, Integer> pair = null;
        
        try {
            beginTransaction();
            
            int avg = Integer.MAX_VALUE;
            
            Result pre_result = graphDb.execute("MATCH  (startNode:NODE), (endNode:NODE)," +
                                            "path = shortestPath((startNode)-[*]-(endNode)) " +
                                            "WHERE startNode.PARTITION <> endNode.PARTITION " +
                                            "RETURN AVG(LENGTH(path)) AS avg");
            
            if (pre_result.hasNext()) {
                Map<String, Object> row = pre_result.next();
                double avg_temp = (double) row.get("avg"); 
                avg = (int) Math.floor(avg_temp);
            }
            
            if (avg != Integer.MAX_VALUE){
                
                Result result = graphDb.execute("MATCH  (startNode:NODE), (endNode:NODE)," +
                                                "path = shortestPath((startNode)-[*]-(endNode)) " +
                                                "WHERE startNode.PARTITION <> endNode.PARTITION AND LENGTH(path) >= " + avg + " " +
                                                "RETURN LENGTH(path),startNode.PARTITION AS part1,endNode.PARTITION AS part2 " +
                                                "ORDER BY LENGTH(path) ASC LIMIT 1");

                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    int part1 = (int) row.get("part1");
                    int part2 = (int) row.get("part2");
                    pair = new Pair<>(part1,part2);
                }
            }

        } finally {
            endTransaction();
        }

        return pair;

    }

    @Override
    public int getNumVertexWithoutPartition() {

        int num = 0;
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (n:NODE) WHERE n.PARTITION IS NULL RETURN n");
            Iterator<Integer> list = result.columnAs("n");
            num = IteratorUtil.count(list);
        } finally {
            endTransaction();
        }

        return num;//IteratorUtil.count(list);

    }

    @Override
    public Iterable<Vertex> getPartitionVertex(int partition) {

        List<Vertex> list = new ArrayList<>();

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE{PARTITION:" + partition + "}) RETURN a ORDER BY a.INDEX");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("a");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Iterable<Vertex> getPartitionVertex(int i, int level) {
        if (level == 0) {
            return getPartitionVertex(i);
        }

        List<Vertex> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:COARSEN_NODE{PARTITION:" + i + ",LEVEL:" + level + "}) RETURN a ORDER BY a.INDEX");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("a");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Iterable<Vertex> getPartitioFrontier(int i) {

        List<Vertex> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE{PARTITION:" + i + "})-[:LINK]-(b) WHERE b.PARTITION IS NULL RETURN b");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("b");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Iterable<Vertex> getPartitioFrontierSortedByWight(int i) {

        List<Vertex> list = new ArrayList<>();

        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE{PARTITION:" + i + "})-[r:LINK]-(b) WHERE b.PARTITION IS NULL "
                    + "RETURN b,(SUM(r.WHEIGH)) AS weight");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("b");

                int vert_level = Vertex.DEFAULT_LEVEL;
                if (node.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level = (int) node.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition = Vertex.DEFAULT_PARTITION_NONE;
                if (node.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition = (int) node.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert = new Vertex((long) node.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) node.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition,
                        vert_level);
                list.add(vert);
            }
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public int getVertexFrontierWeight(long id, int partition) {

        Iterator<Integer> list;
        int frontierWeight = 0;
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + id + "})-[r:LINK]-(b:NODE{PARTITION:" + partition + "}) "
                    + "RETURN SUM(r.WEIGHT) AS total");
            list = result.columnAs("total");
            if (list.hasNext()) {
                int value = list.next();
                frontierWeight = (int) value;
            }
        } finally {
            endTransaction();
        }

        return frontierWeight;
    }

    @Override
    public Iterable<Vertex> getFrontierBetweenPartitions(int partition1, int partition2, int num) {
        List<Vertex> list = new ArrayList<>();
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a:NODE{PARTITION:" + partition1 + "})-[r:LINK*1.." + num + "]-(b:NODE{PARTITION:" + partition2 + "}) "
                    + "RETURN a,b");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node a = (Node) row.get("a");
                Node b = (Node) row.get("b");

                int vert_level_a = Vertex.DEFAULT_LEVEL;
                if (a.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level_a = (int) a.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition_a = Vertex.DEFAULT_PARTITION_NONE;
                if (a.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition_a = (int) a.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert_a = new Vertex((long) a.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) a.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition_a,
                        vert_level_a);

                int vert_level_b = Vertex.DEFAULT_LEVEL;
                if (b.hasProperty(NetworkNeo4jProperties.LEVEL)) {
                    vert_level_b = (int) b.getProperty(NetworkNeo4jProperties.LEVEL);
                }

                int vert_partition_b = Vertex.DEFAULT_PARTITION_NONE;
                if (b.hasProperty(NetworkNeo4jProperties.PARTITION)) {
                    vert_partition_b = (int) b.getProperty(NetworkNeo4jProperties.PARTITION);
                }

                Vertex vert_b = new Vertex((long) b.getProperty(NetworkNeo4jProperties.INDEX),
                        (int) b.getProperty(NetworkNeo4jProperties.WEIGHT),
                        vert_partition_b,
                        vert_level_b);

                list.add(vert_a);
                list.add(vert_b);

            };
        } finally {
            endTransaction();
        }
        return new VertexIterable<Vertex>(list.iterator(), new Vertex());
    }

    @Override
    public Network getSubnetworkFrontier(int partition1, int partition2, int num) {
        Network newNetwork = new NetworkNeo4j(networkFileName + "_frontier_" + num);

        List<Long> idVertexList = new ArrayList<Long>();
        List<Long> idEdgeList = new ArrayList<Long>();

        for (Vertex v : getFrontierBetweenPartitions(partition1, partition2, num)) {
            Vertex vertex = newNetwork.createVertex(v.getId(), v.getWeight());
            vertex.setPartition(v.getPartition());
            idVertexList.add(v.getId());
        }

        for (Vertex v : newNetwork.getAllVertex()) {
            for (Edge e : getEdgesFromNode(v)) {

                if (!idEdgeList.contains(e.getId())) {

                    long otherId = e.getEndNode().getId();
                    if (otherId == v.getId()) {
                        otherId = e.getStartNode().getId();
                    }

                    if (idVertexList.contains(otherId)) {
                        newNetwork.createEdge(e.getWeight(), v.getId(), otherId);
                        idEdgeList.add(e.getId());
                    }

                }
            }
        }

        return newNetwork;
    }

    @Override
    public int getSubnetworkFrontierNum(int partition1, int partition2, int num) {
        return IteratorUtil.count(getFrontierBetweenPartitions(partition1, partition2, num));
    }

    @Override
    public int getCutWeight() {

        Iterator<Integer> list;
        int cutWeight = 0;
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH  (a:NODE), (b:NODE), (a)-[r:LINK]->(b) "
                    + "WHERE a.PARTITION <> b.PARTITION "
                    + "RETURN SUM(r.WEIGHT) AS total");
            list = result.columnAs("total");
            if (list.hasNext()) {
                int value = list.next();
                cutWeight = (int) value;
            }
        } finally {
            endTransaction();
        }

        return cutWeight;
    }

    public List<Pair> getPatitionsWithNumVertex() {

        List<Pair> list = new ArrayList<Pair>();

        for (Iterator iterator = getPartitions(); iterator.hasNext();) {
            int partition = (int) iterator.next();
            int num = IteratorUtil.count(getPartitionVertex(partition));

            Pair pair = new Pair(partition, num);
            list.add(pair);

        }

        return list;

    }

    public int getExpansion() {

        return 0;
    }

    public int getConductance() {

        return 0;
    }

    @Override
    public int gainOfVertex(Vertex v) {

        Iterator<Long> list;
        int gain = 0;
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH  (a:NODE{INDEX:1}), (a)-[r:LINK]-(b) "
                    + "RETURN SUM( "
                    + "CASE a.PARTITION "
                    + "WHEN b.PARTITION "
                    + "THEN 1 "
                    + "ELSE -1 "
                    + "END) AS gain");
            list = result.columnAs("gain");
            if (list.hasNext()) {
                long value = list.next();
                gain = (int) value;
            }
        } finally {
            endTransaction();
        }

        return gain;
    }

    @Override
    public List<Pair> gainOfAllVertex() {

        List<Pair> list = new ArrayList<>();

        Iterator<Long> list_ids;
        Iterator<Long> list_gains;
        try {
            beginTransaction();
            Result result = graphDb.execute("MATCH (a)-[r:LINK]-(b) "
                    + "RETURN SUM( "
                    + "CASE a.PARTITION "
                    + "WHEN b.PARTITION "
                    + "THEN 1 "
                    + "ELSE -1 "
                    + "END) AS result, a.INDEX ORDER BY result DESC");

            //System.out.println("HA \n"+result.resultAsString());
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                long id = (long) row.get("a.INDEX");
                long gain = (long) row.get("result");
                Pair p = new Pair(id, (int) gain);
                list.add(p);
            }

        } finally {
            endTransaction();
        }

        return list;
    }

    @Override
    public void uncoarsen() {

        try {
            beginTransaction();
            Result result1 = graphDb.execute("MATCH (a:COARSEN_NODE) WITH MAX(a.LEVEL) AS level "
                    + "MATCH (n:NODE)-[r:COARSEN*]->(m:COARSEN_NODE{LEVEL:level}) "
                    + "SET n.PARTITION = m.PARTITION "
                    + "RETURN n,m");
        } finally {
            endTransaction();
        }

        try {
            beginTransaction();
            Result result1 = graphDb.execute("MATCH (a)-[r:COARSEN]->(b), (c:COARSEN_NODE) "
                    + "DELETE r,c");
        } finally {
            endTransaction();
        }

    }

    public double getGlobalClusteringCoefficient() {

        double value = 0;

        try {
            beginTransaction();

            Result result = graphDb.execute("match (a:NODE)--(n:NODE)--()--(a) "
                    + "with count(n)/6 as triangle "
                    + "match (c)--(d)--(e) "
                    + "return (count(d)/2) AS triple, triangle");

            if (result.hasNext()) {
                Map<String, Object> row = result.next();
                Long triple = (Long) row.get("triple");
                Long triangle = (Long) row.get("triangle");

                if (triple != 0) {
                    value = (3 * triangle.doubleValue()) / triple.doubleValue();
                }
            }

        } finally {
            endTransaction();
        }

        return value;

    }

    public double getGlobalClusteringCoefficient(int partition) {

        double value = 0;

        try {
            beginTransaction();

            Result result = graphDb.execute("match (a:NODE{PARTITION:" + partition + "})--(n:NODE{PARTITION:" + partition + "})--(b:NODE{PARTITION:" + partition + "})--(a) "
                    + "with count(n)/6 as triangle "
                    + "match (c:NODE{PARTITION:" + partition + "})--(d:NODE{PARTITION:" + partition + "})--(e:NODE{PARTITION:" + partition + "}) "
                    + "return (count(d)/2) AS triple, triangle");

            if (result.hasNext()) {
                Map<String, Object> row = result.next();
                Long triple = (Long) row.get("triple");
                Long triangle = (Long) row.get("triangle");

                if (triple != 0) {
                    value = (3 * triangle.doubleValue()) / triple.doubleValue();
                }
            }

        } finally {
            endTransaction();
        }

        return value;

    }

    public double getVertexLocalClusteringCoefficient(long index) {
        double value = 0;

        try {
            beginTransaction();

            Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + index + "})--(b:NODE), (c:NODE{INDEX:" + index + "})--(d:NODE), (c)--(e:NODE), (d)-[r]-(e) "
                    + "RETURN count(distinct(b)) AS k, count(distinct(r)) AS num");

            if (result.hasNext()) {
                Map<String, Object> row = result.next();
                Long k = (Long) row.get("k");
                Long num = (Long) row.get("num");

                if (k > 1) {
                    value = (2 * num.doubleValue()) / (k.doubleValue() * (k.doubleValue() - 1));
                }
            }

        } finally {
            endTransaction();
        }

        return value;

    }

    public double getVertexLocalClusteringCoefficient(long index, int partition) {
        double value = 0;

        try {
            beginTransaction();

            Result result = graphDb.execute("MATCH (a:NODE{INDEX:" + index + ", PARTITION:" + partition + "})--(b:NODE{PARTITION:" + partition + "}), (c:NODE{INDEX:" + index + ", PARTITION:" + partition + "})--(d:NODE{PARTITION:" + partition + "}), (c)--(e:NODE{PARTITION:" + partition + "}), (d)-[r]-(e) "
                    + "RETURN count(distinct(b)) AS k, count(distinct(r)) AS num");

            if (result.hasNext()) {
                Map<String, Object> row = result.next();
                Long k = (Long) row.get("k");
                Long num = (Long) row.get("num");

                if (k > 1) {
                    value = (2 * num.doubleValue()) / (k.doubleValue() * (k.doubleValue() - 1));
                }
            }

        } finally {
            endTransaction();
        }

        return value;

    }

    public double getLocalClusteringCoefficient() {

        double value = 0;
        int num = 0;

        for (Vertex v : getAllVertex()) {
            long index = v.getId();
            value += getVertexLocalClusteringCoefficient(index);
            num++;
        }

        if (num != 0) {
            value = value / num;
        }

        return value;
    }

    public double getLocalClusteringCoefficient(int partition) {

        double value = 0;
        int num = 0;

        for (Vertex v : getPartitionVertex(partition)) {
            long index = v.getId();
            value += getVertexLocalClusteringCoefficient(index, partition);
            num++;
        }

        if (num != 0) {
            value = value / num;
        }

        return value;

    }

    private void deleteFileOrDirectory(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    deleteFileOrDirectory(child);
                }
            }
            file.delete();
        }
    }
}
