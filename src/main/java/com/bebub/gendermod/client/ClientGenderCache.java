package com.bebub.gendermod.client;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientGenderCache {
    private static final Map<UUID, String> GENDER_MAP = new ConcurrentHashMap<>();
    private static final String CACHE_FILE_NAME = "gendermod_cache.dat";

    public static void put(UUID uuid, String gender) {
        GENDER_MAP.put(uuid, gender);
        save();
    }

    public static String get(UUID uuid) {
        return GENDER_MAP.get(uuid);
    }

    public static void load() {
        File cacheFile = new File(CACHE_FILE_NAME);
        if (!cacheFile.exists()) return;
        
        try (DataInputStream dis = new DataInputStream(new FileInputStream(cacheFile))) {
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                long mostSigBits = dis.readLong();
                long leastSigBits = dis.readLong();
                UUID uuid = new UUID(mostSigBits, leastSigBits);
                String gender = dis.readUTF();
                GENDER_MAP.put(uuid, gender);
            }
        } catch (IOException e) {
        }
    }

    public static void save() {
        File cacheFile = new File(CACHE_FILE_NAME);
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(cacheFile))) {
            dos.writeInt(GENDER_MAP.size());
            for (Map.Entry<UUID, String> entry : GENDER_MAP.entrySet()) {
                dos.writeLong(entry.getKey().getMostSignificantBits());
                dos.writeLong(entry.getKey().getLeastSignificantBits());
                dos.writeUTF(entry.getValue());
            }
            dos.flush();
        } catch (IOException e) {
        }
    }

    public static void clear() {
        GENDER_MAP.clear();
        save();
    }
}