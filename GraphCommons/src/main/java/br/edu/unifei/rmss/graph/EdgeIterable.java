package br.edu.unifei.rmss.graph;

import java.util.Iterator;

public class EdgeIterable<T> implements Iterable<Edge> {

	private Iterator<T> edges;
	private Edge edgeObject;
	
	public EdgeIterable(Iterator<T> edges, Edge edgeObject) {
		this.edges = edges;
		this.edgeObject = edgeObject;
	}
	
	@Override
	public Iterator<Edge> iterator() {
		return new Iterator<Edge>() {

			@Override
			public Edge next() {
				T edge = edges.next();
				
				try {
                                        edgeObject = new Edge();
					edgeObject.setInner((Edge)edge);
					return edgeObject;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean hasNext() {
				return edges.hasNext();
			}

			@Override
			public void remove() {
			}
		};
	}
}
