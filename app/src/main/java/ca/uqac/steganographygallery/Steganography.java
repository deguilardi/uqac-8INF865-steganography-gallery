package ca.uqac.steganographygallery;

// STEGANOGRAPHY -- ANDROID PROJECT


import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class Steganography{

    Bitmap img = null;
    String msg = "";

    public Steganography(Bitmap bitmap, String msg) throws FileNotFoundException {
        /*this.filePath = filePath;
        Log.i("steganoTag","appel au constructeur");
        FileInputStream in = new FileInputStream(filePath);
        this.img = BitmapFactory.decodeStream(in).copy(Bitmap.Config.ARGB_8888, true);*/
        this.img = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Log.i("steganoTag","img width = "+img.getWidth());
        this.msg = msg;
        Log.i("steganoTag", "msg = "+this.msg);
    }

    private int[] getPixels(){
        Log.i("steganoTag", "get pixel");
        Log.i("steganoTag", "image de "+img.getWidth()+" par "+img.getHeight());
        int[] pixelsArray = new int[img.getHeight()*img.getWidth()];
        Log.i("steganoTag", "pixelsArray created");
        img.getPixels(pixelsArray, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        return pixelsArray;
    }

    private ArrayList<Integer> stringToBits(){
        Log.i("steganoTag", "msg = "+msg);
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
        Log.i("steganoTag", "encoded msg = "+sb.toString());
        bitsArray.addAll(EOMList);
        return bitsArray;
    }

    public Bitmap hideMessage() throws IOException {
        int i=0;
        int countBits=0;
        Log.i("steganoTag", "hide message");
        int[] pixelsArray = getPixels();
        Log.i("steganoTag", "getPixels return array of size "+pixelsArray.length);
        ArrayList<Integer> bitsArray = stringToBits();
        Log.i("steganoTag", "msg returned; bitsArray size = "+bitsArray.size());
        Log.i("steganoTag", "bitsArray = "+bitsArray.toString());
        while(countBits<bitsArray.size() && i<pixelsArray.length){

            int pixel = pixelsArray[i];
            Log.i("steganoTag", "pixel before modif "+pixel);

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
            Log.i("steganoTag",  ""+red+" "+green+" "+blue);
            pixelsArray[i] = pixel;
            Log.i("steganoTag", "pixel after modif "+pixel);
            i++;
        }
        //Log.i("steganoTag", "transformation completed");
        img.setPixels(pixelsArray, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        return img;
        /*FileOutputStream out = new FileOutputStream(filePath);
        img.compress(Bitmap.CompressFormat.PNG, 100, out);
        Log.i("steganoTag", "file saved");
        out.close();*/
    }


    public int overrideLastsBits(int byteToChange, int bitZero, int bitOne){
        int mask = 1 << 1;
        byteToChange = (byteToChange & ~mask) | ((bitZero << 1) & mask);
        mask = 1 << 0;
        byteToChange = (byteToChange & ~mask) | ((bitOne << 0) & mask);
        return byteToChange;
    }


    public ArrayList<Integer> getMessage (){
        Log.i("steganoTag", "function decode");
        int i=0;
        int countBits=0;
        int[] pixelsArray = getPixels();
        ArrayList<Integer> bitsArray = new ArrayList<Integer>();
        List<Integer> EOMList = Arrays.asList(1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0);
        while(i<pixelsArray.length){

            int pixel = pixelsArray[i];

            /*int alpha = (pixel>>24) & 0xFF;
            bitsArray.add((alpha >> 1) & 1);
            bitsArray.add((alpha >> 0) & 1);*/
            if(bitsArray.size() == 16){
                if(!bitsArray.equals(EOMList)) {
                    Log.i("steganoTag", "bitsArray "+bitsArray.toString());
                    Log.i("steganoTag", "no encrypted message detected");
                    return new ArrayList<Integer>() {};
                }
            }
            if(bitsArray.size()>16 && bitsArray.size()%8==0){
                boolean eq = true;
                int k=0;
                for(int p=16; p>0; p--){
                    if(bitsArray.get(bitsArray.size()-p) != EOMList.get(k)){
                        eq = false;
                        break;
                    }
                    k++;
                }
                if(eq)
                    return bitsArray;
            }
            int red = (pixel>>16) & 0xFF;
            bitsArray.add((red >> 1) & 1);
            bitsArray.add((red >> 0) & 1);

            if(bitsArray.size() == 16){
                if(!bitsArray.equals(EOMList)) {
                    Log.i("steganoTag", "bitsArray "+bitsArray.toString());
                    Log.i("steganoTag", "no encrypted message detected");
                    return new ArrayList<Integer>() {};
                }
            }
            if(bitsArray.size()>16 && bitsArray.size()%8==0){
                boolean eq = true;
                int k=0;
                for(int p=16; p>0; p--){
                    if(bitsArray.get(bitsArray.size()-p)!=EOMList.get(k)){
                        eq = false;
                        break;
                    }
                    k++;
                }
                if(eq)
                    return bitsArray;
            }
            int green = (pixel>>8) & 0xFF;
            bitsArray.add((green >> 1) & 1);
            bitsArray.add((green >> 0) & 1);

            if(bitsArray.size() == 16){
                if(!bitsArray.equals(EOMList)) {
                    Log.i("steganoTag", "bitsArray "+bitsArray.toString());
                    Log.i("steganoTag", "no encrypted message detected");
                    return new ArrayList<Integer>() {};
                }
            }
            if(bitsArray.size()>16 && bitsArray.size()%8==0){
                boolean eq = true;
                int k=0;
                for(int p=16; p>0; p--){
                    if(bitsArray.get(bitsArray.size()-p)!=EOMList.get(k)){
                        eq = false;
                        break;
                    }
                    k++;
                }
                if(eq)
                    return bitsArray;
            }
            int blue = pixel & 0xFF;
            bitsArray.add((blue >> 1) & 1);
            bitsArray.add((blue >> 0) & 1);
            Log.i("steganoTag", ""+red+" "+green+" "+blue);
            Log.i("steganoTag", "pixel decryption "+pixel);

            i++;
        }
        return bitsArray;
    }


    public String bitsToString(){
        ArrayList<Integer> bitsArray = getMessage();
        Log.i("steganoTag", "bitsArray from encrypted message of size "+bitsArray.size());
        Log.i("steganoTag", "full msg "+bitsArray.toString());
        if(bitsArray.size() == 0){
            return "";
        }
        int start = 16;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<bitsArray.size(); i++){
            sb.append(bitsArray.get(i).toString());
        }
        Log.i("steganoTag", "msg "+sb.toString());
        String bitsString = sb.toString();
        while(start <= bitsArray.size()-24){
            int cValue = Integer.parseInt(bitsString.substring(start, start+8), 2);
            if(cValue>=0 && cValue<9)
                msg += (char)(cValue+'0');
            else
                msg += (char)cValue;
            start+=8;
        }
        return msg;
    }

}

