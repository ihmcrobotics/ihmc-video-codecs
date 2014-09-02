package org.jcodec.movtool;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jcodec.common.model.Rational;
import org.jcodec.containers.mp4.boxes.Edit;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * Cut on ref movies
 * 
 * @author The JCodec project
 * 
 */
public class Util {

    public static class Pair<T> {
        private T a;
        private T b;

        public Pair(T a, T b) {
            this.a = a;
            this.b = b;
        }

        public T getA() {
            return a;
        }

        public T getB() {
            return b;
        }
    }

    public static Pair<List<Edit>> split(List<Edit> edits, Rational trackByMv, long tvMv) {
        long total = 0;
        List<Edit> l = new ArrayList<Edit>();
        List<Edit> r = new ArrayList<Edit>();
        ListIterator<Edit> lit = edits.listIterator();
        while (lit.hasNext()) {
            Edit edit = lit.next();
            if (total + edit.getDuration() > tvMv) {
                int leftDurMV = (int) (tvMv - total);
                int leftDurMedia = trackByMv.multiplyS(leftDurMV);

                Edit left = new Edit(leftDurMV, edit.getMediaTime(), 1.0f);
                Edit right = new Edit(edit.getDuration() - leftDurMV, leftDurMedia + edit.getMediaTime(), 1.0f);

                lit.remove();
                if (left.getDuration() > 0) {
                    lit.add(left);
                    l.add(left);
                }
                if (right.getDuration() > 0) {
                    lit.add(right);
                    r.add(right);
                }
                break;
            } else {
                l.add(edit);
            }
            total += edit.getDuration();
        }
        while (lit.hasNext()) {
            r.add(lit.next());
        }
        return new Pair<List<Edit>>(l, r);
    }

    public static List<Edit> editsOnEdits(Rational mvByTrack, List<Edit> lower, List<Edit> higher) {
        List<Edit> result = new ArrayList<Edit>();
        List<Edit> next = new ArrayList<Edit>(lower);
        for (Edit edit : higher) {
            long startMv = mvByTrack.multiply(edit.getMediaTime());
            Pair<List<Edit>> split = split(next, mvByTrack.flip(), startMv);
            Pair<List<Edit>> split2 = split(split.getB(), mvByTrack.flip(), startMv + edit.getDuration());
            result.addAll(split2.getA());
            next = split2.getB();
        }
        return result;
    }
}
