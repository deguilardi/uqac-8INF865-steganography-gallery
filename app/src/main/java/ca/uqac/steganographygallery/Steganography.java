package ca.uqac.steganographygallery;

// STEGANOGRAPHY -- ANDROID PROJECT


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.util.*;


public class Steganography{

    File f = null;
    Bitmap img = null;
    String msg = "";

    public Steganography(String path, String msg){
        Log.i("stegano","appel au constructeur");
        f = new File(path);
        if(f.exists()){
            img = BitmapFactory.decodeFile(f.getAbsolutePath());
        }
        this.msg = msg;
    }

    private int[][] getPixels(){
        Log.i("stegano", "image de "+img.getWidth()+" par "+img.getHeight());
        int[][] pixelsArray = new int[img.getHeight()][img.getWidth()];
        for(int i=0; i<img.getHeight(); i++){
            for(int j=0; j<img.getWidth(); j++){
                pixelsArray[i][j] = img.getPixel(i,j);
            }
        }
        return pixelsArray;
    }

    private ArrayList<Integer> stringToBits(){
        Log.i("stegano", "msg = "+msg);
        List<Integer> EOMList = Arrays.asList(1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0);
        ArrayList<Integer> bitsArray = new ArrayList<Integer>();
        bitsArray.addAll(EOMList);
        byte[] bytesArray = msg.getBytes();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<bytesArray.length; i++){
            String byteTemp = String.format("%8s", Integer.toBinaryString(bytesArray[i] & 0xFF)).replace(' ', '0');
            for(int j=0; j<8; j++){
                sb.append(byteTemp.charAt(j));
                bitsArray.add(Character.getNumericValue(byteTemp.charAt(j)));
            }
        }
        Log.i("stegano", "encoded msg = "+sb.toString());
        bitsArray.addAll(EOMList);
        return bitsArray;
    }

    public void hideMessage(){
        int i=0;
        int j = 0;
        int countBits=0;
        int[][] pixelsArray = getPixels();
        ArrayList<Integer> bitsArray = stringToBits();
        while(countBits<bitsArray.size() && i<pixelsArray.length && j<pixelsArray[0].length){

            int pixel = pixelsArray[i][j];

            int alpha = (pixel>>24) & 0xFF;
            int valueBitZero = bitsArray.get(countBits);
            countBits++;
            alpha = overrideLastsBits(alpha, valueBitZero, bitsArray.get(countBits));
            countBits++;

            int red = (pixel>>16) & 0xFF;
            valueBitZero = bitsArray.get(countBits);
            countBits++;
            red = overrideLastsBits(red, valueBitZero, bitsArray.get(countBits));
            countBits++;

            int green = (pixel>>8) & 0xFF;
            valueBitZero = bitsArray.get(countBits);
            countBits++;
            green = overrideLastsBits(green, valueBitZero, bitsArray.get(countBits));
            countBits++;

            int blue = pixel & 0xFF;
            valueBitZero = bitsArray.get(countBits);
            countBits++;
            blue = overrideLastsBits(blue, valueBitZero, bitsArray.get(countBits));
            countBits++;

            pixel = (alpha<<24) | (red<<16) | (green<<8) | blue;
            img.setPixel(i, j, pixel);
            i++;
            j++;
        }

    }


    public int overrideLastsBits(int byteToChange, int bitZero, int bitOne){
        int mask = 1 << 1;
        byteToChange = (byteToChange & ~mask) | ((bitZero << 1) & mask);
        mask = 1 << 0;
        return (byteToChange & ~mask) | ((bitOne << 0) & mask);
    }


    public ArrayList<Integer> getMessage (){
        int i=0;
        int j=0;
        int countBits=0;
        int[][] pixelsArray = getPixels();
        ArrayList<Integer> bitsArray = new ArrayList<Integer>();
        List<Integer> EOMList = Arrays.asList(1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0);
        while(countBits<bitsArray.size() && i<pixelsArray.length && j<pixelsArray[0].length){

            int pixel = pixelsArray[i][j];

            int alpha = (pixel>>24) & 0xFF;
            bitsArray.add(alpha & 2);
            bitsArray.add(alpha & 1);

            int red = (pixel>>16) & 0xFF;
            bitsArray.add(red & 2);
            bitsArray.add(red & 1);

            int green = (pixel>>8) & 0xFF;
            bitsArray.add(green & 2);
            bitsArray.add(green & 1);

            int blue = pixel & 0xFF;
            bitsArray.add(blue & 2);
            bitsArray.add(blue & 1);

            if(bitsArray.size() == 16){
                if(!bitsArray.equals(EOMList))
                    return new ArrayList<Integer>(){};
            }

            if(bitsArray.size() > 16){
                boolean eq = true;
                int k=0;
                for(int p=15; p>=0; p--){
                    if(bitsArray.get(bitsArray.size()-p)!=EOMList.get(j++)){
                        eq = false;
                        break;
                    }
                }
                if(eq == true)
                    return bitsArray;
            }
            i++;
            j++;
        }
        return bitsArray;
    }


    public String bitsToString(){
        ArrayList<Integer> bitsArray = getMessage();
        if(bitsArray.size() == 0){
            return "";
        }
        int start = 16;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<bitsArray.size(); i++){
            sb.append(bitsArray.get(i).toString());
        }
        char[] charArray = sb.toString().toCharArray();
        while(start <= bitsArray.size()-24){
            char[] bits = Arrays.copyOfRange(charArray, start, start + 8);
            int cValue = Integer.parseInt(new String(bits));
            if(cValue>=0 && cValue<9)
                msg += (char)(cValue+'0');
            else
                msg += (char)cValue;
            start+=8;
        }
        return msg;
    }

}

