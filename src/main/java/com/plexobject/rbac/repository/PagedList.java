package com.plexobject.rbac.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PagedList<T, ID> implements List<T> {
    private static final long serialVersionUID = 1L;
    private final List<T> list;
    private final ID firstKey;
    private final ID lastKey;
    private final int limit;
    private final boolean more;

    PagedList() {
        this.list = null;
        this.firstKey = null;
        this.lastKey = null;
        this.limit = 0;
        this.more = false;
    }

    public PagedList(final List<T> list) {
        this(list, null, null, list.size(), false);
    }

    public PagedList(final List<T> list, final ID start, final ID end,
            final int limit, final boolean hasMore) {
        this.list = list;
        this.firstKey = start;
        this.lastKey = end;
        this.limit = limit;
        this.more = hasMore;
    }

    /**
     * @return the start of the key
     */
    public ID getFirstKey() {
        return firstKey;
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @return the last key that will be passed to the next request
     */
    public ID getLastKey() {
        return lastKey;
    }

    /**
     * @return the hasMore
     */
    public boolean hasMore() {
        return more;
    }

    public static <T, ID> PagedList<T, ID> emptyList() {
        return new PagedList<T, ID>(Collections.<T> emptyList(), null, null, 0,
                false);
    }

    public static <T, ID> PagedList<T, ID> asList(T... args) {
        return new PagedList<T, ID>(Arrays.<T> asList(args));
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
