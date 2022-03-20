package com.example.sampleserver;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.security.auth.DestroyFailedException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import sun.security.pkcs.PKCS8Key;

public class Token {

    // https://jwt.io/

    public static String getJWTToken(String username, String hostname) {
        KeyPair key = Keys.keyPairFor(SignatureAlgorithm.RS256);

        // нужно сохранить ключи и больше не перезаписывать
        PrivateKey privateKey = key.getPrivate();
        PublicKey publicKey = key.getPublic();


        String token = Jwts.builder()
                .claim("username",username)
                .claim("hostname", hostname)
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
        writData(("Bearer " + token), (hostname + ".txt"));
        return "Bearer " + token;
    }

    public static void writData(String token, String fileName) {
        File myExternalStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        File documentsFile = new File(myExternalStorage, fileName);
        FileOutputStream fos = null;
        try {
            if (!documentsFile.exists()){
                documentsFile.createNewFile();
            }
            fos = new FileOutputStream(documentsFile, false);
            fos.write(token.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
