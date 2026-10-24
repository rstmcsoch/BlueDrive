package com.moorixlabs.bluedrive;
import android.content.*;
import android.security.keystore.*;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
/** AES-GCM key never leaves Android Keystore. Clear legacy only after verified persistence. */
public final class SettingsUtils {
 private static final String ALIAS="BlueDrive.connection.v1";
 private static javax.crypto.SecretKey key() throws Exception {
  KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
  if(store.containsAlias(ALIAS))return (javax.crypto.SecretKey)store.getKey(ALIAS,null);
  KeyGenerator gen=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
  gen.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());return gen.generateKey();
 }
 private static String encrypt(String value)throws Exception {Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());return Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+":"+Base64.encodeToString(c.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)),Base64.NO_WRAP);}
 private static String decrypt(String value)throws Exception {String[] p=value.split(":",2);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p[0],Base64.NO_WRAP)));return new String(c.doFinal(Base64.decode(p[1],Base64.NO_WRAP)),java.nio.charset.StandardCharsets.UTF_8);}
 public static synchronized void migrate(Context c){SharedPreferences old=c.getSharedPreferences("BlueDrivePrefs",0);if(old.contains("bot_token")||old.contains("chat_id"))saveSettings(c,old.getString("bot_token",""),old.getString("chat_id",""));}
 public static synchronized void saveSettings(Context c,String token,String chat){try{
  String t=encrypt(token),h=encrypt(chat);if(!decrypt(t).equals(token)||!decrypt(h).equals(chat))return;
  if(c.getSharedPreferences("secure_connection",0).edit().putString("token",t).putString("chat",h).commit()) c.getSharedPreferences("BlueDrivePrefs",0).edit().remove("bot_token").remove("chat_id").commit();
 }catch(Exception ignored){/* Preserve legacy values on migration failure; never log secrets. */}}
 private static String get(Context c,String name){try{String value=c.getSharedPreferences("secure_connection",0).getString(name,"");return value.isEmpty()?"":decrypt(value);}catch(Exception e){return "";}}
 public static String getBotToken(Context c){return get(c,"token");}public static String getChatId(Context c){return get(c,"chat");}
 public static boolean isConfigured(Context c){return !getBotToken(c).isEmpty()&&!getChatId(c).isEmpty();}
 public static void clear(Context c){c.getSharedPreferences("secure_connection",0).edit().clear().apply();c.getSharedPreferences("BlueDrivePrefs",0).edit().remove("bot_token").remove("chat_id").apply();}
}