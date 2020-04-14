package ca.uqac.steganographygallery;

// STEGANOGRAPHY -- ANDROID PROJECT


import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Steganography{

    private Bitmap img;

    public Steganography(Bitmap bitmap) {
        this.img = bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void free(){
        if(img != null){
            img.recycle();
        }
    }

    private int[] getPixels(){
        int[] pixelsArray = new int[img.getHeight()*img.getWidth()];
        img.getPixels(pixelsArray, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        return pixelsArray;
    }

    private ArrayList<Integer> stringToBits(String msg){
        List<Integer> EOMList = Arrays.asList(1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0);
        ArrayList<Integer> bitsArray = new ArrayList<>(EOMList);
        byte[] bytesArray = msg.getBytes();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytesArray) {
            String byteTemp = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            for (int j = 0; j < 8; j++) {
                sb.append(byteTemp.charAt(j));
                bitsArray.add(Character.getNumericValue(byteTemp.charAt(j)));
            }
        }
        bitsArray.addAll(EOMList);
        return bitsArray;
    }

    public Bitmap hideMessage(String msg) {
        int i=0;
        int countBits=0;
        int[] pixelsArray = getPixels();
        ArrayList<Integer> bitsArray = stringToBits(msg);
        while(countBits<bitsArray.size() && i<pixelsArray.length){

            int pixel = pixelsArray[i];

            /*int alpha = (pixel>>24) & 0xFF;
            int valueBitZero = bitsArray.get(countBits);
            countBits++;
            alpha = overrideLastsBits(alpha, valueBitZero, bitsArray.get(countBits));
            countBits++;*/

            int red = (pixel>>16) & 0xFF;
            int valueBitZero = bitsArray.get(countBits);
            countBits++;
            red = overrideLastsBits(red, valueBitZero, bitsArray.get(countBits));
            countBits++;

            int green = (pixel >> 8) & 0xFF;
            if(countBits<bitsArray.size()) {
                valueBitZero = bitsArray.get(countBits);
                countBits++;
                green = overrideLastsBits(green, valueBitZero, bitsArray.get(countBits));
                countBits++;
            }

            int blue = pixel & 0xFF;
            if(countBits<bitsArray.size()) {
                valueBitZero = bitsArray.get(countBits);
                countBits++;
                blue = overrideLastsBits(blue, valueBitZero, bitsArray.get(countBits));
                countBits++;
            }

            pixel = (red<<16) | (green<<8) | blue;
            pixelsArray[i] = pixel;
            i++;
        }
        img.setPixels(pixelsArray, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        return img;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private int overrideLastsBits(int byteToChange, int bitZero, int bitOne){
        int mask = 1 << 1;
        byteToChange = (byteToChange & ~mask) | ((bitZero << 1) & mask);
        mask = 1 << 0;
        byteToChange = (byteToChange & ~mask) | ((bitOne << 0) & mask);
        return byteToChange;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private ArrayList<Integer> getBitsOfMessage (){
        int i=0;
        int[] pixelsArray = getPixels();
        ArrayList<Integer> bitsArray = new ArrayList<>();
        while(i<pixelsArray.length){

            int pixel = pixelsArray[i];

            /*int alpha = (pixel>>24) & 0xFF;
            bitsArray.add((alpha >> 1) & 1);
            bitsArray.add((alpha >> 0) & 1);*/
            if(testEOM(bitsArray))
                return bitsArray;
            int red = (pixel>>16) & 0xFF;
            bitsArray.add((red >> 1) & 1);
            bitsArray.add((red >> 0) & 1);

            if(testEOM(bitsArray))
                return bitsArray;
            int green = (pixel>>8) & 0xFF;
            bitsArray.add((green >> 1) & 1);
            bitsArray.add((green >> 0) & 1);

            if(testEOM(bitsArray))
                return bitsArray;
            int blue = pixel & 0xFF;
            bitsArray.add((blue >> 1) & 1);
            bitsArray.add((blue >> 0) & 1);

            i++;
        }
        return bitsArray;
    }

    private boolean testEOM(ArrayList<Integer> bitsArray){
        List<Integer> EOMList = Arrays.asList(1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0);
        if(bitsArray.size() == 16){
            if(!bitsArray.equals(EOMList)) {
                return true;
            }
        }
        if(bitsArray.size()>16 && bitsArray.size()%8==0){
            boolean eq = true;
            int k=0;
            for(int p=16; p>0; p--){
                if(!bitsArray.get(bitsArray.size() - p).equals(EOMList.get(k))){
                    eq = false;
                    break;
                }
                k++;
            }
            return eq;
        }
        return false;
    }

    public String getHiddenMessage(){
        ArrayList<Integer> bitsArray = getBitsOfMessage();
        if(bitsArray.size() <= 16){
            return "";
        }

        StringBuilder sbBits = new StringBuilder();
        for(int i=0; i<bitsArray.size(); i++){
            sbBits.append(bitsArray.get(i).toString());
        }
        String bitsString = sbBits.toString();

        StringBuilder sbResult = new StringBuilder();
        int start = 16;
        while(start <= bitsArray.size()-24){
            int cValue = Integer.parseInt(bitsString.substring(start, start+8), 2);
            if(cValue>=0 && cValue<9)
                sbResult.append((char)(cValue+'0'));
            else
                sbResult.append((char)cValue);
            start+=8;
        }
        return sbResult.toString();
    }
}

