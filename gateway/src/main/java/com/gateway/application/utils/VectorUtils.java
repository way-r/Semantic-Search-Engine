package com.gateway.application.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class VectorUtils {
    public static byte[] floatListToByteArr(List<Float> floatVector) {
        ByteBuffer buffer = ByteBuffer.allocate(floatVector.size() * Float.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (Float f : floatVector) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }
}
