package com.sketcher.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for finding perfect matchings of near minimum weight in graphs.
 * 
 * @author robert
 *
 * @param <E>
 *            data to associate with each edge
 */
public class MatchGraph<E> {

	private List<Vertex<E>> vertices;
	private List<Edge<E>> edges;

	/**
	 * Creates an empty graph with no edges and <code>size</code> verticies
	 * 
	 * @param size
	 *            The number of vertices in this graph
	 */
	public MatchGraph(int size) {
		vertices = new ArrayList<>();
		this.edges = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Vertex<E> v = new Vertex<E>();
			v.id = i;
			vertices.add(v);
		}
	}

	/**
	 * Adds an edge to this graph
	 * 
	 * @param v1
	 *            Id of first vertex
	 * @param v2
	 *            Id of second vertex
	 * @param weight
	 *            weight of the edge
	 * @param data
	 *            data to associate with this edge
	 */
	public final void addEdge(int v1, int v2, double weight, E data) {
		Edge<E> e = new Edge<E>();
		e.v1 = vertices.get(v1);
		e.v2 = vertices.get(v2);
		e.data = data;
		e.weight = weight;
		e.v1.weightedDegree += e.weight;
		e.v2.weightedDegree += e.weight;
		edges.add(e);
		e.v1.adjacent.add(e);
		e.v2.adjacent.add(e);
	}

	/**
	 * Resets the graph after performing an algorithm
	 */
	public void clear() {
		for (Vertex<E> v : vertices) {
			v.weightedDegree = 0.0;
		}
		for (Edge<E> e : edges) {
			e.matched = false;
			e.tmpMatched = false;
			e.v1.weightedDegree += e.weight;
			e.v2.weightedDegree += e.weight;
		}
	}

	/**
	 * Brute force search for perfect matching with minimum weight. May be VERY
	 * slow.
	 * 
	 * @return A list of the edge data included in the matching.
	 */
	public List<E> match() {
		match(Double.POSITIVE_INFINITY, 0.0);
		return edges.stream().filter(e -> e.matched).map(e -> e.data).collect(Collectors.toList());
	}

	/**
	 * Helper function for {@link #match()}
	 * 
	 * @param best
	 *            Double.POSITIVE_INFINITY
	 * @param weight
	 *            0.0
	 * @return the weight of the matching
	 */
	private double match(double best, double weight) {
		int count = 0;
		for (Edge<E> e : edges) {
			double newWeight = weight + e.weight;
			if (e.tmpMatched || e.v1.isTmpMatched() || e.v2.isTmpMatched()) {
				continue;
			}
			count++;
			if (newWeight >= best) {
				continue;
			}
			e.tmpMatched = true;
			newWeight = match(best, newWeight);
			e.tmpMatched = false;
			if (newWeight < best) {
				best = newWeight;
			}
		}

		if (count == 0 && weight < best) {
			for (Edge<E> e : edges) {
				e.matched = e.tmpMatched;
			}
			best = weight;
		}
		return best;
	}

	/**
	 * Fast greedy algorithm for finding a perfect matching with near-minimum
	 * weight.
	 * 
	 * @return A list of the edge data included in the matching.
	 */
	public List<E> matchGreedy() {
		List<E> matched = new LinkedList<E>();
		Set<Edge<E>> edgeSet = new HashSet<>(edges);
		while (!edgeSet.isEmpty()) {
			Edge<E> match = null;
			double max = Double.NEGATIVE_INFINITY;
			for (Edge<E> e : edgeSet) {
				double score = e.v1.weightedDegree + e.v2.weightedDegree - 2 * e.weight;
				if (match == null || score > max) {
					match = e;
					max = score;
				}
			}
			matched.add(match.data);
			match.matched = true;

			for (Edge<E> e : match.v1.adjacent) {
				edgeSet.remove(e);
				e.getOther(match.v1).weightedDegree -= e.weight;
			}
			for (Edge<E> e : match.v2.adjacent) {
				edgeSet.remove(e);
				e.getOther(match.v2).weightedDegree -= e.weight;
			}

		}

		return matched;
	}

	@Override
	public String toString() {
		return edges.toString();
	}

	/**
	 * Represents a vertex
	 * 
	 * @author robert
	 *
	 * @param <E>
	 *            the edge data
	 */
	private static class Vertex<E> {
		private int id;
		private double weightedDegree;
		private List<Edge<E>> adjacent = new ArrayList<>();

		/**
		 * For use in {@link MatchGraph#match()}
		 * 
		 * @return
		 */
		public boolean isTmpMatched() {
			for (Edge<E> e : adjacent) {
				if (e.tmpMatched) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Vertex<?>))
				return false;
			return id == ((Vertex<?>) obj).id;
		}

	}

	/**
	 * Represents an edge in the graph
	 * 
	 * @author robert
	 *
	 * @param <E>
	 *            the edge data
	 */
	private static class Edge<E> {
		private E data;
		private Vertex<E> v1, v2;
		private double weight;
		private boolean matched = false;
		private boolean tmpMatched = false;

		/**
		 * Given the current vertex, return the other vertex that this edge is attached
		 * to.
		 * 
		 * @param v
		 *            the current vertex
		 * @return the other vertex that this edge is attached to
		 */
		public Vertex<E> getOther(Vertex<E> v) {
			return v == v1 ? v2 : v1;
		}

		@Override
		public String toString() {
			if (!matched)
				return "";
			StringBuilder sb = new StringBuilder("{ ");
			sb.append(v1.id);
			sb.append(", ");
			sb.append(v2.id);
			sb.append(", ");
			sb.append(weight);
			sb.append(", ");
			sb.append(matched);
			sb.append(" }");
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
			result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Edge<?>))
				return false;
			Edge<?> other = (Edge<?>) obj;
			return Objects.equals(v1, other.v1) && Objects.equals(v2, other.v2);
		}

	}
}
