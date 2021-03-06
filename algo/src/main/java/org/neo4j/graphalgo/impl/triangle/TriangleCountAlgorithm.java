/**
 * Copyright (c) 2017 "Neo4j, Inc." <http://neo4j.com>
 *
 * This file is part of Neo4j Graph Algorithms <http://github.com/neo4j-contrib/neo4j-graph-algorithms>.
 *
 * Neo4j Graph Algorithms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.impl.triangle;

import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.api.HugeGraph;
import org.neo4j.graphalgo.core.utils.Pools;
import org.neo4j.graphalgo.core.utils.ProgressLogger;
import org.neo4j.graphalgo.core.utils.TerminationFlag;
import org.neo4j.graphalgo.core.utils.paged.AllocationTracker;

import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * @author mknblch
 */
public interface TriangleCountAlgorithm {

    long getTriangleCount();

    double getAverageCoefficient();

    <V> V getTriangles();

    <V> V getCoefficients();

    Stream<Result> resultStream();

    TriangleCountAlgorithm withProgressLogger(ProgressLogger wrap);

    TriangleCountAlgorithm withTerminationFlag(TerminationFlag wrap);

    TriangleCountAlgorithm release();

    TriangleCountAlgorithm compute();

    static double calculateCoefficient(int triangles, int degree) {
        if (triangles == 0) {
            return 0.0;
        }
        return ((double) (triangles << 1)) / (degree * (degree - 1));
    }

    class Result {

        public final long nodeId;
        public final long triangles;

        public final double coefficient;

        public Result(long nodeId, long triangles, double coefficient) {
            this.nodeId = nodeId;
            this.triangles = triangles;
            this.coefficient = coefficient;
        }
        @Override
        public String toString() {
            return "Result{" +
                    "nodeId=" + nodeId +
                    ", triangles=" + triangles +
                    ", coefficient=" + coefficient +
                    '}';
        }

    }

    static TriangleCountAlgorithm instance(Graph graph, ExecutorService pool, int concurrency) {
        if (graph instanceof HugeGraph) {
            return new HugeTriangleCount((HugeGraph) graph, pool, concurrency, AllocationTracker.create());
        } else {
            return new TriangleCountQueue(graph, pool, concurrency);
        }
    }

}
