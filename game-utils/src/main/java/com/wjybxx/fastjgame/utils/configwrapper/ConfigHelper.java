package com.wjybxx.fastjgame.utils.configwrapper;

import com.wjybxx.fastjgame.utils.constants.UtilConstants;

/**
 * 基于字符串键值对配置的帮助类。
 *
 * 注意：数组分隔为{@link UtilConstants#ARRAY_DELIMITER} 即'|'
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/11 16:05
 * @github - https://github.com/hl845740757
 */
public abstract class ConfigHelper {

    /**
     * 如果属性名获取属性的值，如果不存在则返回null
     * 子类可以有不同的存储结构，这里需要自己实现。
     * @param key
     * @return
     */
    public abstract String getAsString(String key);

    public String getAsString(String key,String defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:stringValue;
    }

    // region 对基本类型的支持
    public int getAsInt(String key){
        return Integer.parseInt(getAsString(key));
    }

    public int getAsInt(String key,int defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:Integer.parseInt(stringValue);
    }

    public long getAsLong(String key){
        return Long.parseLong(getAsString(key));
    }

    public long getAsLong(String key,long defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:Long.parseLong(stringValue);
    }

    public double getAsDouble(String key){
        return Double.parseDouble(getAsString(key));
    }

    public double getAsDouble(String key,double defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:Double.parseDouble(stringValue);
    }

    public byte getAsByte(String key){
        return Byte.parseByte(getAsString(key));
    }

    public byte getAsByte(String key,byte defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:Byte.parseByte(stringValue);
    }

    public short getAsShort(String key){
        return Short.parseShort(getAsString(key));
    }

    public short getAsShort(String key,short defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:Short.parseShort(stringValue);
    }

    public float getAsFloat(String key){
        return Float.parseFloat(getAsString(key));
    }

    public float getAsFloat(String key,float defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:Float.parseFloat(stringValue);
    }

    /**
     *
     * @param key
     * @return true,yes,1,y表示为真，其余为假。
     */
    public boolean getAsBool(String key){
        return isTrueString(getAsString(key));
    }

    /**
     * 字符串是否表示true。
     * true,yes,1,y表示为真，其余为假。
     * @param value
     * @return
     */
    private boolean isTrueString(String value){
        return value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("1")
                || value.equalsIgnoreCase("y");
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return true,yes,1,y表示为真，其余为假。
     */
    public boolean getAsBool(String key,boolean defaultValue){
        String stringValue = getAsString(key);
        return null==stringValue?defaultValue:isTrueString(stringValue);
    }
    // endregion

    // region 获取为数组类型
    public String[] getAsStringArray(String key){
        return getAsString(key).split(UtilConstants.ARRAY_DELIMITER);
    }

    public int[] getAsIntArray(String key){
        String[] stringArray = getAsStringArray(key);
        int[] intArray=new int[stringArray.length];
        for (int index=0;index<stringArray.length;index++){
            intArray[index] = Integer.parseInt(stringArray[index]);
        }
        return intArray;
    }

    public long[] getAsLongArray(String key){
        String[] stringArray = getAsStringArray(key);
        long[] intArray=new long[stringArray.length];
        for (int index=0;index<stringArray.length;index++){
            intArray[index] = Long.parseLong(stringArray[index]);
        }
        return intArray;
    }

    public double[] getAsDoubleArray(String key){
        String[] stringArray = getAsStringArray(key);
        double[] doubleArray=new double[stringArray.length];
        for (int index=0;index<stringArray.length;index++){
            doubleArray[index] = Double.parseDouble(stringArray[index]);
        }
        return doubleArray;
    }
    // endregion
}
