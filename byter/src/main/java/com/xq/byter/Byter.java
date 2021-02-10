package com.xq.byter;

import android.text.TextUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Byter {

    //为了避免包装类和基础数据类型的频繁装箱拆箱操作，所有涉及到赋值和取值的地方都需要相当谨慎，不要额外封装方法去赋值
    //对于数组类型数据来说，切记不要使用List来装数据，因为List只能使用包装类型，其内存占用大大高于基础类型数据，推荐使用数组的方式;同时，也不要使用包装类数据

    private static int BYTES_SIZE = 64*1024;

    public static byte[] toBytes(Object o) throws Exception {
        if (o == null) return new byte[0];

        Class mClass = o.getClass();

        ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES_SIZE);

        Field[] fields = getCacheOrderedDeclaredFields(mClass);

        for (int i=0;i<fields.length;i++){

            Field field = fields[i];

            // 静态字段 等情况不受理
            if (Modifier.isStatic(field.getModifiers())){
                continue;
            }

            //设置强制访问
            if (!field.isAccessible()){
                field.setAccessible(true);
            }

            if ( o instanceof ToBytesConverter && ((ToBytesConverter) o).interceptToBytes(fields,i,byteBuffer) )
            {
                //暂时不知道做什么事情...
            }
            else
            {
                if (field.getType().isArray() || String.class.isAssignableFrom(field.getType()))
                {
                    Object value = field.get(o);

                    int length = 0;

                    if (field.getAnnotation(Length.class) != null)
                    {
                        String lengthFieldName = field.getAnnotation(Length.class).lengthByField();

                        if (!TextUtils.isEmpty(lengthFieldName)){

                            Field lengthField = getFieldByName(lengthFieldName,fields);
                            if (lengthField == null){
                                throw new Exception("lengthField is null");
                            }

                            if (field.getType().isArray()){
                                length = Array.getLength(value);
                            } else  if (String.class.isAssignableFrom(field.getType())){
                                length = ((String)value).length();
                            }

                            if (long.class.isAssignableFrom(lengthField.getType())){
                                lengthField.set(o,(long)length);
                            }
                            else   if (int.class.isAssignableFrom(lengthField.getType())){
                                lengthField.set(o,length);
                            }
                            else   if (short.class.isAssignableFrom(lengthField.getType())){
                                lengthField.set(o,(short)length);
                            }
                            else   if (byte.class.isAssignableFrom(lengthField.getType())){
                                lengthField.set(o,(byte)length);
                            }
                            else   if (float.class.isAssignableFrom(lengthField.getType())){
                                lengthField.set(o,(float)length);
                            }
                            else   if (double.class.isAssignableFrom(lengthField.getType())){
                                lengthField.set(o,(double)length);
                            }
                            else {
                                throw new Exception("lengthField is not a number");
                            }
                        } else {

                            length = field.getAnnotation(Length.class).length();

                        }
                    }
                    else
                    {
                        if (field.getType().isArray()){
                            length = Array.getLength(value);
                        } else  if (String.class.isAssignableFrom(field.getType())){
                            length = ((String)value).length();
                        }
                    }

                    if (field.getType().isArray())
                    {
                        for (int j=0;j<length;j++){

                            Class valueClass = field.getType().getComponentType();

                            if (long.class.isAssignableFrom(valueClass)){
                                byteBuffer.put(ByteUtils.long2Bytes((long) Array.get(value,j)));
                            }
                            else    if (int.class.isAssignableFrom(valueClass) ){
                                byteBuffer.put(ByteUtils.int2Bytes((int)Array.get(value,j)));
                            }
                            else    if (short.class.isAssignableFrom(valueClass) ){
                                byteBuffer.put(ByteUtils.short2Bytes((short)Array.get(value,j)));
                            }
                            else    if (char.class.isAssignableFrom(valueClass) ){
                                byteBuffer.put(ByteUtils.char2Bytes((char)Array.get(value,j)));
                            }
                            else    if (byte.class.isAssignableFrom(valueClass) ){
                                byteBuffer.put(ByteUtils.byte2Bytes((byte)Array.get(value,j)));
                            }
                            else    if (float.class.isAssignableFrom(valueClass) ){
                                byteBuffer.put(ByteUtils.float2bytes((float)Array.get(value,j)));
                            }
                            else    if (double.class.isAssignableFrom(valueClass) ){
                                byteBuffer.put(ByteUtils.double2bytes((double)Array.get(value,j)));
                            }
                            else {
                                byteBuffer.put(toBytes(o));
                            }
                        }
                    }
                    else    if (String.class.isAssignableFrom(field.getType())){
                        byteBuffer.put(((String)value).getBytes());
                    }
                }
                else
                {
                    Class valueClass = field.getType();

                    if (long.class.isAssignableFrom(valueClass)){
                        byteBuffer.put(ByteUtils.long2Bytes((long) field.get(o)));
                    }
                    else    if (int.class.isAssignableFrom(valueClass) ){
                        byteBuffer.put(ByteUtils.int2Bytes((int)field.get(o)));
                    }
                    else    if (short.class.isAssignableFrom(valueClass) ){
                        byteBuffer.put(ByteUtils.short2Bytes((short)field.get(o)));
                    }
                    else    if (char.class.isAssignableFrom(valueClass) ){
                        byteBuffer.put(ByteUtils.char2Bytes((char)field.get(o)));
                    }
                    else    if (byte.class.isAssignableFrom(valueClass) ){
                        byteBuffer.put(ByteUtils.byte2Bytes((byte)field.get(o)));
                    }
                    else    if (float.class.isAssignableFrom(valueClass) ){
                        byteBuffer.put(ByteUtils.float2bytes((float)field.get(o)));
                    }
                    else    if (double.class.isAssignableFrom(valueClass) ){
                        byteBuffer.put(ByteUtils.double2bytes((double)field.get(o)));
                    }
                    else {
                        byteBuffer.put(toBytes(o));
                    }
                }
            }
        }

        if (o instanceof ToBytesConverter){
            ((ToBytesConverter) o).finishToBytes(fields,byteBuffer);
        }

        return ByteUtils.byteBuffer2Bytes(byteBuffer);

    }

    public static <T>T fromBytes(Class<T> mClass, byte[] bytes) throws Exception {
        return fromBytes(mClass,ByteBuffer.wrap(bytes));
    }

    public static <T>T fromBytes(Class<T> mClass, ByteBuffer byteBuffer) throws Exception {
        if (mClass == null)  return null;

        Constructor<T> constructor = mClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        T o = constructor.newInstance();

        Field[] fields = getCacheOrderedDeclaredFields(mClass);

        for (int i=0;i<fields.length;i++){

            Field field = fields[i];

            // 静态字段 等情况不受理
            if (Modifier.isStatic(field.getModifiers())){
                continue;
            }

            //设置强制访问
            if (!field.isAccessible()){
                field.setAccessible(true);
            }

            if (o instanceof FromBytesConverter && ((FromBytesConverter) o).interceptFromBytes(fields,i,byteBuffer))
            {

            }
            else
            {
                if (field.getType().isArray() || String.class.isAssignableFrom(field.getType()))
                {
                    Object value = null;

                    int length = 0;

                    if (field.getAnnotation(Length.class) != null){

                        String lengthFieldName = field.getAnnotation(Length.class).lengthByField();

                        if (!TextUtils.isEmpty(lengthFieldName)){

                            Field lengthField = getFieldByName(lengthFieldName,fields);
                            if (lengthField == null){
                                throw new Exception("lengthField is null");
                            }

                            if (long.class.isAssignableFrom(lengthField.getType())){
                                length = (int)(long)lengthField.get(o);
                            }
                            else   if (int.class.isAssignableFrom(lengthField.getType())){
                                length = (int)lengthField.get(o);
                            }
                            else   if (short.class.isAssignableFrom(lengthField.getType())){
                                length = (int)(short)lengthField.get(o);
                            }
                            else   if (byte.class.isAssignableFrom(lengthField.getType())){
                                length = (int)(byte)lengthField.get(o);
                            }
                            else   if (float.class.isAssignableFrom(lengthField.getType())){
                                length = (int)(float)lengthField.get(o);
                            }
                            else   if (double.class.isAssignableFrom(lengthField.getType())){
                                length = (int)(double)lengthField.get(o);
                            }
                            else {
                                throw new Exception("lengthField is not a number");
                            }

                        } else {

                            length = field.getAnnotation(Length.class).length();

                        }

                    } else {
                        //即使没有设置Length注解，还可以通过推算后面的数据从而得出当前数组的长度（如果同时存在两个以上的数组那就没办法了）
                        //如果你的协议有补0的处理，那这段代码段可能会导致你的解析失败
                        Field[] afterFields = new Field[fields.length-(i+1)];
                        System.arraycopy(fields,i+1,afterFields,0,afterFields.length);
                        int afterSize = sizeOf(afterFields);

                        int mSize = -1;
                        if (field.getType().isArray()){
                            mSize = sizeOf(field.getType().getComponentType());
                        }
                        else    if (String.class.isAssignableFrom(field.getType())){
                            mSize = 1;
                        }

                        if (mSize < 0 || afterSize < 0){
                            throw new Exception("please to set Length Annotation");
                        }

                        length = (byteBuffer.limit() - byteBuffer.position() - afterSize) / mSize;
                    }

                    if (field.getType().isArray())
                    {
                        value = Array.newInstance(field.getType().getComponentType(),length);

                        for (int j=0;j<length;j++){

                            Class valueClass = field.getType().getComponentType();

                            if (long.class.isAssignableFrom(valueClass)){
                                Array.set(value,j,byteBuffer.getLong());
                            }
                            else    if (int.class.isAssignableFrom(valueClass)){
                                Array.set(value,j,byteBuffer.getInt());
                            }
                            else    if (short.class.isAssignableFrom(valueClass)){
                                Array.set(value,j,byteBuffer.getShort());
                            }
                            else    if (char.class.isAssignableFrom(valueClass)){
                                Array.set(value,j,byteBuffer.getChar());
                            }
                            else    if (byte.class.isAssignableFrom(valueClass)){
                                Array.set(value,j,byteBuffer.get());
                            }
                            else    if (float.class.isAssignableFrom(valueClass)){
                                Array.set(value,j,byteBuffer.getFloat());
                            }
                            else    if (double.class.isAssignableFrom(valueClass)){
                                Array.set(value,j,byteBuffer.getDouble());
                            }
                            else {
                                Array.set(value,j, fromBytes(valueClass,byteBuffer));
                            }
                        }
                    }
                    else    if (String.class.isAssignableFrom(field.getType())){
                        byte[] strBytes = new byte[length];
                        for (int k=0;k<strBytes.length;k++){
                            strBytes[k] = byteBuffer.get();
                        }
                        value = new String(strBytes);
                    }

                    field.set(o,value);
                }
                else
                {
                    Class valueClass = field.getType();

                    if (long.class.isAssignableFrom(valueClass)){
                        field.set(o,byteBuffer.getLong());
                    }
                    else    if (int.class.isAssignableFrom(valueClass)){
                        field.set(o,byteBuffer.getInt());
                    }
                    else    if (short.class.isAssignableFrom(valueClass)){
                        field.set(o,byteBuffer.getShort());
                    }
                    else    if (char.class.isAssignableFrom(valueClass)){
                        field.set(o,byteBuffer.getChar());
                    }
                    else    if (byte.class.isAssignableFrom(valueClass)){
                        field.set(o,byteBuffer.get());
                    }
                    else    if (float.class.isAssignableFrom(valueClass)){
                        field.set(o,byteBuffer.getFloat());
                    }
                    else    if (double.class.isAssignableFrom(valueClass)){
                        field.set(o,byteBuffer.getDouble());
                    }
                    else {
                        field.set(o, fromBytes(valueClass,byteBuffer));
                    }
                }
            }
        }

        if (o instanceof FromBytesConverter){
            ((FromBytesConverter) o).finishFromBytes(fields,byteBuffer);
        }

        return o;
    }

    private static int sizeOf(Class valueClass){
        if (long.class.isAssignableFrom(valueClass) || double.class.isAssignableFrom(valueClass)){
            return 8;
        }
        else    if (int.class.isAssignableFrom(valueClass) || float.class.isAssignableFrom(valueClass)){
            return 4;
        }
        else    if (short.class.isAssignableFrom(valueClass) || char.class.isAssignableFrom(valueClass)){
            return 2;
        }
        else    if (byte.class.isAssignableFrom(valueClass)){
            return 1;
        }
        else {
            return sizeOf(getCacheOrderedDeclaredFields(valueClass));
        }
    }

    //不要将数组类型包含在内
    private static int sizeOf(Field[] fields){
        int length = 0;
        for (Field field : fields){
            if (field.getType().isArray() || String.class.isAssignableFrom(field.getType())){
                return -1;
            }
            Class valueClass = field.getType();
            if (long.class.isAssignableFrom(valueClass) || double.class.isAssignableFrom(valueClass)){
                length  = length+8;
            }
            else    if (int.class.isAssignableFrom(valueClass) || float.class.isAssignableFrom(valueClass)){
                length  = length+4;
            }
            else    if (short.class.isAssignableFrom(valueClass) || char.class.isAssignableFrom(valueClass)){
                length  = length+2;
            }
            else    if (byte.class.isAssignableFrom(valueClass)){
                length  = length+1;
            }
            else {
                int objectLength = sizeOf(getCacheOrderedDeclaredFields(valueClass));
                if (objectLength < 0){
                    return objectLength;
                }
                length = length + objectLength;
            }
        }
        return length;
    }

    private static Map<Class, Field[]> classMap = new HashMap<>();
    private static Field[] getCacheOrderedDeclaredFields(Class mClass){
        if (!classMap.containsKey(mClass)){
            classMap.put(mClass,orderedFields(getAllDeclaredFields(mClass)));
        }
        return classMap.get(mClass);
    }

    private static Field[] getAllDeclaredFields(Class mClass){
        if (mClass == null || mClass.getName().equals(Object.class.getName())) return null;

        List<Field> list = new LinkedList<>();

        Field[] allParentField = getAllDeclaredFields(mClass.getSuperclass());
        if (allParentField != null && allParentField.length >0)
            list.addAll(Arrays.asList(allParentField));

        list.addAll(Arrays.asList(mClass.getDeclaredFields()));

        return list.toArray(new Field[0]);
    }

    private static Field[] orderedFields(Field[]fields){
        List<Field> list = new ArrayList<>();
        for(Field field:fields){
            if(field.getAnnotation(Order.class)!=null){
                list.add(field);
            }
        }
        Collections.sort(list, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.getAnnotation(Order.class).order() - o2.getAnnotation(Order.class).order();
            }
        });
        return list.toArray(new Field[0]);
    }

    private static Field getFieldByName(String name,Field[] fields){
        for (Field lengthField : fields) {
            if (lengthField.getName().equals(name)) {
                return lengthField;
            }
        }
        return null;
    }

    public interface ToBytesConverter {

        public boolean interceptToBytes(Field[] fields, int fieldPosition, ByteBuffer byteBuffer) throws RuntimeException;

        public void finishToBytes(Field[] fields, ByteBuffer byteBuffer) throws RuntimeException;
    }

    public interface FromBytesConverter{

        public boolean interceptFromBytes(Field[] fields, int fieldPosition, ByteBuffer byteBuffer) throws RuntimeException;

        public void finishFromBytes(Field[] fields, ByteBuffer byteBuffer) throws RuntimeException;

    }

}
