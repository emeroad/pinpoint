package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.LRUCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringCacheableBufferTest {

    @Test
    public void stringCache() {

        Buffer writer = new AutomaticBuffer();
        writer.putPrefixedString("abc");
        writer.putPrefixedString("123");
        writer.putPrefixedString("abc");


        StringAllocator allocator = new CachedStringAllocator(new LRUCache<>(2));

        Buffer buffer = new StringCacheableBuffer(writer.getBuffer(), allocator);
        String s1 = buffer.readPrefixedString();
        String s2 = buffer.readPrefixedString();
        String s3 = buffer.readPrefixedString();

        Assertions.assertSame(s1, s3);
    }

    @Test
    public void boundaryCheck() {

        Buffer writer = new AutomaticBuffer();
        writer.putPrefixedString("abc");
        // prefix index
        writer.putSVInt(10);


        StringAllocator allocator = new CachedStringAllocator(new LRUCache<>(2));

        Buffer buffer = new StringCacheableBuffer(writer.getBuffer(), allocator);
        String s1 = buffer.readPrefixedString();
        Assertions.assertEquals("abc", s1);

        Assertions.assertThrows(IndexOutOfBoundsException.class, buffer::readPrefixedString);
    }
}