package br.edu.unifei.rmss.graph;

import java.util.Iterator;



public class VertexIterable<T> implements Iterable<Vertex> {

	private Iterator<T> nodes;
	private Vertex nodeObject;
	
	public VertexIterable(Iterator<T> nodes, Vertex nodeObject) {
		this.nodes = nodes;
		this.nodeObject = nodeObject;
	}
	
	@Override
	public Iterator<Vertex> iterator() {
		return new Iterator<Vertex>() {

			@Override
			public Vertex next() {
                            
				T node = nodes.next();
				
				try {
                                        nodeObject = new Vertex();
					nodeObject.setInner((Vertex)node);
					return nodeObject;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean hasNext() {
				return nodes.hasNext();
			}

			@Override
			public void remove() {
			}
		};
	}
}