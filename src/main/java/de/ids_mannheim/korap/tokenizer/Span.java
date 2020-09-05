package de.ids_mannheim.korap.tokenizer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Serializable;
import java.util.Objects;


/**
 * Class for storing start and end integer offsets.
 *
 */
public class Span implements Comparable<Span>, Serializable {

    private final int start;
    private final int end;
    private final double prob;//default is 0
    private final String type;

    /**
     * Initializes a new Span Object. Sets the prob to 0 as default.
     *
     * @param s start of span.
     * @param e end of span, which is +1 more than the last element in the span.
     * @param type the type of the span
     */
    public Span(int s, int e, String type) {
        this(s, e, type, 0d);
    }

    /**
     * Initializes a new Span Object.
     *
     * @param s start of span.
     * @param e end of span, which is +1 more than the last element in the span.
     * @param type the type of the span
     * @param prob probability of span.
     */
    public Span(int s, int e, String type, double prob) {

        if (s < 0) {
            throw new IllegalArgumentException("start index must be zero or greater: " + s);
        }
        if (e < 0) {
            throw new IllegalArgumentException("end index must be zero or greater: " + e);
        }
        if (s > e) {
            throw new IllegalArgumentException(
                    "start index must not be larger than end index: " + "start=" + s + ", end=" + e);
        }

        start = s;
        end = e;
        this.prob = prob;
        this.type = type;
    }

    /**
     * Return the start of a span.
     *
     * @return the start of a span.
     *
     */
    public int getStart() {
        return start;
    }

    /**
     * Return the end of a span.
     *
     * Note: that the returned index is one past the actual end of the span in the
     * text, or the first element past the end of the span.
     *
     * @return the end of a span.
     *
     */
    public int getEnd() {
        return end;
    }

    /**
     * Retrieves the type of the span.
     *
     * @return the type or null if not set
     */
    public String getType() {
        return type;
    }

    /**
     * Compares the specified span to the current span.
     */
    public int compareTo(Span s) {
        if (getStart() < s.getStart()) {
            return -1;
        } else if (getStart() == s.getStart()) {
            if (getEnd() > s.getEnd()) {
                return -1;
            } else if (getEnd() < s.getEnd()) {
                return 1;
            } else {
                // compare the type
                if (getType() == null && s.getType() == null) {
                    return 0;
                } else if (getType() != null && s.getType() != null) {
                    // use type lexicography order
                    return getType().compareTo(s.getType());
                } else if (getType() != null) {
                    return -1;
                }
                return 1;
            }
        } else {
            return 1;
        }
    }

    /**
     * Generates a hash code of the current span.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), getType());
    }

    /**
     * Checks if the specified span is equal to the current span.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof Span) {
            Span s = (Span) o;

            return getStart() == s.getStart() && getEnd() == s.getEnd() && Objects.equals(getType(), s.getType());
        }

        return false;
    }

    /**
     * Generates a human readable string.
     */
    @Override
    public String toString() {
        StringBuilder toStringBuffer = new StringBuilder(15);
        toStringBuffer.append("[");
        toStringBuffer.append(getStart());
        toStringBuffer.append("..");
        toStringBuffer.append(getEnd());
        toStringBuffer.append(")");
        if (getType() != null) {
            toStringBuffer.append(" ");
            toStringBuffer.append(getType());
        }

        return toStringBuffer.toString();
    }

}
