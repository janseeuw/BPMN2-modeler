package be.ugent.intec.ibcn.dbase.tooling.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Helper {
	
	public static <T> List<List<T>> getCross(List<List<T>> values) {
        List<List<T>> accumulator = new ArrayList<List<T>>();
        if (values.size() != 0) {
            List<T> comb = new ArrayList<T>();
            comb.addAll(Collections.<T>nCopies(values.size(), null));
            getCross(accumulator, 0, comb, values);
        }
        return accumulator;
    }

    private static <T> void getCross(List<List<T>> accumulator, int idx, List<T> combination, List<List<T>> param) {
        if (idx == combination.size()) {
            accumulator.add(new ArrayList<T>(combination));
        } else {
            for(T t : param.get(idx)) {
                combination.set(idx, t);
                getCross(accumulator, idx + 1, combination, param);
            }
        }
    }

}
