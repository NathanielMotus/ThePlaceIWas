package com.nathanielmotus.theplaceiwas.model;

import java.util.Comparator;

public class PlaceComparator implements Comparator<Place> {
    @Override
    public int compare(Place o1, Place o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
