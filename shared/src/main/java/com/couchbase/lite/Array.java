//
// Array.java
//
// Copyright (c) 2017 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.lite;

import com.couchbase.lite.internal.utils.DateUtils;
import com.couchbase.litecore.fleece.Encoder;
import com.couchbase.litecore.fleece.FLEncodable;
import com.couchbase.litecore.fleece.FLEncoder;
import com.couchbase.litecore.fleece.MArray;
import com.couchbase.litecore.fleece.MCollection;
import com.couchbase.litecore.fleece.MContext;
import com.couchbase.litecore.fleece.MValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Array provides readonly access to array data.
 */
public class Array implements ArrayInterface, FLEncodable, Iterable<Object> {

    //---------------------------------------------
    // member variables
    //---------------------------------------------

    MArray _array;

    protected Object _sharedLock;

    //---------------------------------------------
    // Constructors
    //---------------------------------------------

    Array() {
        _array = new MArray();
        setupSharedLock();
    }

    Array(MValue mv, MCollection parent) {
        _array = new MArray();
        _array.initInSlot(mv, parent);
        setupSharedLock();
    }

    // to crete mutable copy
    Array(MArray mArray, boolean isMutable) {
        _array = new MArray();
        _array.initAsCopyOf(mArray, isMutable);
        setupSharedLock();
    }

    //---------------------------------------------
    // API - public methods
    //---------------------------------------------

    /**
     * Gets a number of the items in the array.
     *
     * @return
     */
    @Override
    public int count() {
        synchronized (_sharedLock) {
            return (int) _array.count();
        }
    }

    /**
     * Gets value at the given index as an object. The object types are Blob,
     * Array, Dictionary, Number, or String based on the underlying
     * data type; or nil if the value is nil.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Object or null.
     */
    @Override
    public Object getValue(int index) {
        synchronized (_sharedLock) {
            return _get(_array, index).asNative(_array);
        }
    }

    /**
     * Gets value at the given index as a String. Returns null if the value doesn't exist, or its value is not a String.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the String or null.
     */
    @Override
    public String getString(int index) {
        synchronized (_sharedLock) {
            Object obj = _get(_array, index).asNative(_array);
            return obj instanceof String ? (String) obj : null;
        }
    }

    /**
     * Gets value at the given index as a Number. Returns null if the value doesn't exist, or its value is not a Number.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Number or nil.
     */
    @Override
    public Number getNumber(int index) {
        synchronized (_sharedLock) {
            return CBLConverter.asNumber(_get(_array, index).asNative(_array));
        }
    }

    /**
     * Gets value at the given index as an int.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the int value.
     */
    @Override
    public int getInt(int index) {
        synchronized (_sharedLock) {
            return CBLConverter.asInteger(_get(_array, index), _array);
        }
    }

    /**
     * Gets value at the given index as an long.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the long value.
     */
    @Override
    public long getLong(int index) {
        synchronized (_sharedLock) {
            return CBLConverter.asLong(_get(_array, index), _array);
        }
    }

    /**
     * Gets value at the given index as an float.
     * Integers will be converted to float. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the value doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the float value.
     */
    @Override
    public float getFloat(int index) {
        synchronized (_sharedLock) {
            return CBLConverter.asFloat(_get(_array, index), _array);
        }
    }

    /**
     * Gets value at the given index as an double.
     * Integers will be converted to double. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the property doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the double value.
     */
    @Override
    public double getDouble(int index) {
        synchronized (_sharedLock) {
            return CBLConverter.asDouble(_get(_array, index), _array);
        }
    }

    /**
     * Gets value at the given index as a boolean.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the boolean value.
     */
    @Override
    public boolean getBoolean(int index) {
        synchronized (_sharedLock) {
            Object value = _get(_array, index).asNative(_array);
            return CBLConverter.asBoolean(value);
        }

    }

    /**
     * Gets value at the given index as a Blob.
     * Returns null if the value doesn't exist, or its value is not a Blob.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Blob value or null.
     */
    @Override
    public Blob getBlob(int index) {
        synchronized (_sharedLock) {
            return (Blob) _get(_array, index).asNative(_array);
        }
    }

    /**
     * Gets value at the given index as a Date.
     * JSON does not directly support dates, so the actual property value must be a string, which is
     * then parsed according to the ISO-8601 date format (the default used in JSON.)
     * Returns null if the value doesn't exist, is not a string, or is not parseable as a date.
     * NOTE: This is not a generic date parser! It only recognizes the ISO-8601 format, with or
     * without milliseconds.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Date value or null.
     */
    @Override
    public Date getDate(int index) {
        return DateUtils.fromJson(getString(index));
    }

    /**
     * Gets a Array at the given index. Return null if the value is not an array.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Array object.
     */
    @Override
    public Array getArray(int index) {
        synchronized (_sharedLock) {
            Object obj = _get(_array, index).asNative(_array);
            return obj instanceof Array ? (Array) obj : null;
        }
    }

    /**
     * Gets a Dictionary at the given index. Return null if the value is not an dictionary.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Dictionary object.
     */
    @Override
    public Dictionary getDictionary(int index) {
        synchronized (_sharedLock) {
            Object obj = _get(_array, index).asNative(_array);
            return obj instanceof Dictionary ? (Dictionary) obj : null;
        }
    }

    /**
     * Gets content of the current object as an List. The values contained in the returned
     * List object are all JSON based values.
     *
     * @return the List object representing the content of the current object in the JSON format.
     */
    @Override
    public List<Object> toList() {
        synchronized (_sharedLock) {
            int count = (int) _array.count();
            List<Object> result = new ArrayList<>(count);
            for (int index = 0; index < count; index++)
                result.add(Fleece.toObject(_get(_array, index).asNative(_array)));
            return result;
        }
    }

    /**
     * Return a mutable copy of the array
     *
     * @return the MutableArray instance
     */
    public MutableArray toMutable() {
        synchronized (_sharedLock) {
            return new MutableArray(_array, true);
        }
    }

    //-------------------------------------------------------------------------
    // Implementation of FLEncodable
    //-------------------------------------------------------------------------

    /**
     * encodeTo(FlEncoder) is internal method. Please don't use this method.
     */
    @Override
    public void encodeTo(FLEncoder enc) {
        Encoder encoder = new Encoder(enc);
        _array.encodeTo(encoder);
        encoder.release();
    }

    //---------------------------------------------
    // Iterable implementation
    //---------------------------------------------

    @Override
    public Iterator<Object> iterator() {
        return new ArrayIterator();
    }

    //---------------------------------------------
    // Override
    //---------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Array)) return false;

        Array a = (Array) o;
        Iterator<Object> itr1 = iterator();
        Iterator<Object> itr2 = a.iterator();
        while (itr1.hasNext() && itr2.hasNext()) {
            Object o1 = itr1.next();
            Object o2 = itr2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2)))
                return false;
        }
        return !(itr1.hasNext() || itr2.hasNext());
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (Object o : this)
            h = 31 * h + (o == null ? 0 : o.hashCode());
        return h;
    }

    //---------------------------------------------
    // package level access
    //---------------------------------------------
    static MValue _get(MArray array, int index) {
        MValue value = array.get(index);
        if (value.isEmpty())
            throwRangeException(index);
        return value;
    }

    static String throwRangeException(int index) {
        throw new IndexOutOfBoundsException("Array index " + index + " is out of range");
    }

    MCollection toMCollection() {
        return _array;
    }

    private class ArrayIterator implements Iterator<Object> {
        private int index = 0;
        private int count = count();

        @Override
        public boolean hasNext() {
            return index < count;
        }

        @Override
        public Object next() {
            return getValue(index++);
        }
    }

    private void setupSharedLock() {
        MContext context = _array.getContext();
        if (context != null && context != MContext.NULL)
            _sharedLock = ((DocContext)context).getDatabase().getLock();
        else
            _sharedLock = new Object();
    }
}
