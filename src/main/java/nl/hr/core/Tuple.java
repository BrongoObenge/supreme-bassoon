package nl.hr.core;

import lombok.*;

/**
 * Created by j on 6/21/2016.
 */
@lombok.Value
public class Tuple<T> {
    T _1;
    T _2;
}
