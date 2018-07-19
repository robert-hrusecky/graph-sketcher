package com.sketcher.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Graph for use in solving the Chinese Postman Problem. Given a connected
 * graph, finds the minimum weight (or near minimum weight) closed tour that
 * visits every edge.
 * 
 * @author robert
 *
 * @param <D>
 *            Data to associate with each vertex
 */
public class CPPGraph<D> {
	private List<Vertex<D>> vertices;
	private List<Edge<D>> edges;

	/**
	 * Initializes the graph object.
	 * 
	 * @param data
	 *            An array of data to store at each vertex. <code>data.length</code>
	 *            will be the number of vertices in the graph.
	 * @param edges
	 *            An array of tuples representing edges. Each element should be of
	 *            type <code>int[2]</code> where the first element is the id of the
	 *            first vertex, and the second element is the id of the second
	 *            vertex.
	 * @param dist
	 *            A function for computing the edge weight for each edge. Takes in
	 *            the <code>D</code> for each vertex and outputs the weight.
	 */
	public CPPGraph(D[] data, int[][] edges, BiFunction<D, D, Double> dist) {
		vertices = new ArrayList<>();
		this.edges = new ArrayList<>();
		for (int i = 0; i < data.length; i++) {
			Vertex<D> v = new Vertex<>();
			v.id = i;
			v.data = data[i];
			vertices.add(v);
		}
		for (int i = 0; i < edges.length; i++) {
			int[] edge = edges[i];
			Vertex<D> v1 = vertices.get(edge[0]);
			Vertex<D> v2 = vertices.get(edge[1]);
			Edge<D> e = new Edge<>(v1, v2, dist.apply(v1.data, v2.data));
			this.edges.add(e);
		}
	}

	/**
	 * Reset the graph after a call to {@link #dijkstra(Vertex)}
	 */
	private void clearDijkstra() {
		for (Vertex<D> v : vertices) {
			v.visited = false;
			v.dist = Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * Runs Dijkstra's minimum spanning tree alogrtihm on the graph starting at
	 * vertex <code>src</code>.
	 * 
	 * @param src
	 *            the source vertex.
	 */
	private void dijkstra(Vertex<D> src) {
		clearDijkstra();

		Queue<DijkstraVertex<D>> queue = new PriorityQueue<>((a, b) -> (int) Math.signum(a.dist - b.dist));
		queue.add(new DijkstraVertex<>(src, 0.0));

		while (!queue.isEmpty()) {
			DijkstraVertex<D> curr = queue.remove();
			if (!curr.v.visited) {
				curr.v.visited = true;
				for (Edge<D> e : curr.v.adjacent) {
					double newDist = curr.dist + e.weight;
					Vertex<D> next = e.getOther(curr.v);
					if (newDist < next.dist) {
						next.dist = newDist;
						next.prev = curr.v;
						queue.add(new DijkstraVertex<>(next, newDist));
					}
				}
			}
		}
	}

	/**
	 * Gets a minimum TJoin for the graph. Doubling the edges of the TJoin will
	 * result in an eulerian graph for which an Euler tour can be constructed.
	 * 
	 * @return The edges of the TJoin.
	 */
	private List<List<Vertex<D>>> getTJoin() {
		List<Vertex<D>> odds = vertices.stream().filter(v -> v.adjacent.size() % 2 == 1).collect(Collectors.toList());

		MatchGraph<List<Vertex<D>>> matchGraph = new MatchGraph<>(odds.size());

		for (int i = 0; i < odds.size(); i++) {
			Vertex<D> v1 = odds.get(i);
			dijkstra(v1);
			for (int j = i + 1; j < odds.size(); j++) {
				Vertex<D> v2 = odds.get(j);
				List<Vertex<D>> path = new ArrayList<>();
				double dist = v2.dist;
				do {
					path.add(v2);
					v2 = v2.prev;
				} while (v2 != v1);
				path.add(v1);
				matchGraph.addEdge(i, j, dist, path);
			}
		}

		return matchGraph.matchGreedy();
		// return matchGraph.match();
	}

	/**
	 * Computes the solution to the Chinese Postamn problem for this graph.
	 * 
	 * @return The verticies in a list representing the path of the closed tour that
	 *         solves the problem.
	 */
	public List<D> getPath() {
		List<List<Vertex<D>>> tJoin = getTJoin();
		for (List<Vertex<D>> path : tJoin) {
			for (int i = 1; i < path.size(); i++) {
				Edge<D> e = path.get(i).findEdge(path.get(i - 1));
				if (e.mult != 2) {
					e.mult = 2;
				}
			}
		}

		return getTour(vertices.get(0)).stream().map(v -> v.data).collect(Collectors.toList());
	}

	/**
	 * Calculates an Euler tour for the given graph. Assumes graph is Eulerian and
	 * edges have already been doubled via the <code>mult</code> variable.
	 * 
	 * @param start
	 *            Where the tour should start and end.
	 * @return A list of the vertices in the tour.
	 */
	private LinkedList<Vertex<D>> getTour(Vertex<D> start) {
		LinkedList<Vertex<D>> cycle = getCycle(start);
		if (cycle.size() == 1) {
			return cycle;
		}
		// System.out.println("Cycle: " + start + ": " + cycle);

		LinkedList<Vertex<D>> tour = new LinkedList<>();
		for (Vertex<D> v : cycle) {
			tour.addAll(getTour(v));
		}
		return tour;
	}

	/**
	 * Wanders the Eulerian graph constructing a large cycle that starts and ends at
	 * the given vertex. Goes until it gets stuck back at the start. (It is not
	 * possible to get stuck at any other vertex)
	 * 
	 * @param start
	 *            Where the tour should start and end.
	 * @return
	 */
	private LinkedList<Vertex<D>> getCycle(Vertex<D> start) {
		LinkedList<Vertex<D>> cycle = new LinkedList<>();
		cycle.add(start);
		Vertex<D> next = start;
		while (!next.adjacent.isEmpty()) {
			while (!next.adjacent.isEmpty() && next.adjacent.getFirst().mult == 0) {
				next.adjacent.removeFirst();
			}

			if (next.adjacent.isEmpty()) {
				break;
			}

			Edge<D> e = next.adjacent.getFirst();
			e.mult--;

			next = e.getOther(next);
			cycle.add(next);
		}
		return cycle;
	}

	@Override
	public String toString() {
		return edges.toString();
	}

	/**
	 * Class for representing the vertices.
	 * 
	 * @author robert
	 *
	 * @param <D>
	 *            The vertex data.
	 */
	private static class Vertex<D> {
		private Vertex<D> prev;
		private double dist;
		private boolean visited = false;
		private D data;
		private int id;
		private LinkedList<Edge<D>> adjacent = new LinkedList<>();

		/**
		 * Finds the egde adjacent to this vertex and the given vertex.
		 * 
		 * @param other
		 *            the other vertex
		 * @return the edge
		 */
		public Edge<D> findEdge(Vertex<D> other) {
			for (Edge<D> e : adjacent) {
				if (e.getOther(this) == other) {
					return e;
				}
			}
			return null;
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

		@Override
		public String toString() {
			return String.valueOf(id);
		}

	}

	/**
	 * A class to represent the edges of the graph.
	 * 
	 * @author robert
	 *
	 * @param <D>
	 *            the vertex data.
	 */
	private static class Edge<D> {
		private Vertex<D> v1, v2;
		private double weight;
		private int mult;

		public Edge(Vertex<D> v1, Vertex<D> v2, double weight) {
			mult = 1;
			this.v1 = v1;
			this.v2 = v2;
			this.weight = weight;
			v1.adjacent.add(this);
			v2.adjacent.add(this);
		}

		/**
		 * Gets the vertex adjacent to the given vertex through this edge.
		 * 
		 * @param v
		 *            the given vertex
		 * @return the other vertex
		 */
		public Vertex<D> getOther(Vertex<D> v) {
			return v == v1 ? v2 : v1;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("{ ");
			sb.append(v1.id);
			sb.append(", ");
			sb.append(v2.id);
			sb.append(", ");
			sb.append(weight);
			sb.append(" }");
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (v1 == null ? 0 : v1.hashCode());
			result = prime * result + (v2 == null ? 0 : v2.hashCode());
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

	/**
	 * Temprorary class for use in Dijkstra's algorithm.
	 * 
	 * @author robert
	 *
	 * @param <D>
	 *            the vertex data.
	 */
	private static class DijkstraVertex<D> {
		private Vertex<D> v;
		private double dist;

		public DijkstraVertex(Vertex<D> v, double dist) {
			this.v = v;
			this.dist = dist;
		}
	}
}
