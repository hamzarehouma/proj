package com.immobilier.corba;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.TimeUnit;

public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "ImmobilierDB";

    static {
        try {
            // ✅ CONFIGURATION: Avec timeout et retry
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                    .applyToConnectionPoolSettings(builder ->
                            builder.maxConnectionIdleTime(60, TimeUnit.SECONDS)
                                    .maxSize(10)
                    )
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(10, TimeUnit.SECONDS)
                                    .readTimeout(10, TimeUnit.SECONDS)
                    )
                    .retryWrites(true)
                    .build();

            mongoClient = MongoClients.create(settings);

            // ✅ TEST: Vérifier la connexion
            mongoClient.getDatabase(DATABASE_NAME)
                    .runCommand(new org.bson.Document("ping", 1));

            System.out.println("✅ [MODULE] Connexion MongoDB établie");

        } catch (Exception e) {
            System.err.println("❌ [MODULE] Erreur connexion MongoDB: " + e.getMessage());
            System.err.println("💡 Assurez-vous que MongoDB est démarré sur localhost:27017");
            e.printStackTrace();
            System.exit(1); // ✅ Arrêter si pas de DB
        }
    }

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            throw new IllegalStateException("MongoDB client not initialized");
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static MongoClient getClient() {
        return mongoClient;
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("✅ Connexion MongoDB fermée");
        }
    }
}