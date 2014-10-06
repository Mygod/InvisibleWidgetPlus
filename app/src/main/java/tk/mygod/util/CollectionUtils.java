package tk.mygod.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Mygod
 */
public class CollectionUtils {
    public static <T> ArrayList<T> filter(Collection<T> target, Predicate<T> predicate) {
        ArrayList<T> result = new ArrayList<T>();
        for (T element : target) if (predicate.apply(element)) result.add(element);
        return result;
    }
    public static <T> ArrayList<T> filterArray(T[] target, Predicate<T> predicate) {
        ArrayList<T> result = new ArrayList<T>();
        for (T element : target) if (predicate.apply(element)) result.add(element);
        return result;
    }
}
