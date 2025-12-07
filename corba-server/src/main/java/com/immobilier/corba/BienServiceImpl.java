package com.immobilier.corba;

import Immobilier.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.*;

public class BienServiceImpl extends BienServicePOA {
    private final MongoCollection<Document> collection;

    public BienServiceImpl() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.collection = db.getCollection("biens");
        System.out.println("✅ BienServiceImpl initialisé avec MongoDB");
    }

    @Override
    public int addBien(Immobilier.Bien b) {
        try {
            // ✅ CORRECTION: Utiliser une méthode plus robuste pour générer l'ID
            long newId;
            Document lastDoc = collection.find()
                    .sort(new Document("_id", -1))
                    .limit(1)
                    .first();

            if (lastDoc != null) {
                newId = lastDoc.getLong("_id") + 1;
            } else {
                newId = 1;
            }

            Document doc = new Document()
                    .append("_id", newId)
                    .append("titre", b.titre)
                    .append("description", b.description)
                    .append("prix", b.prix)
                    .append("disponible", b.disponible)
                    .append("agentId", (long) b.agentId);

            collection.insertOne(doc);
            System.out.println("✅ Bien ajouté avec ID: " + newId);
            return (int) newId;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout du bien: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    @Override
    public Immobilier.Bien[] listBiens() {
        List<Immobilier.Bien> list = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                list.add(documentToBien(doc));
            }
            System.out.println("📋 " + list.size() + " biens récupérés");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des biens: " + e.getMessage());
            e.printStackTrace();
        }
        return list.toArray(new Immobilier.Bien[0]);
    }

    @Override
    public Immobilier.Bien[] listBiensAgent(int agentId) {
        List<Immobilier.Bien> list = new ArrayList<>();
        try {
            for (Document doc : collection.find(eq("agentId", (long) agentId))) {
                list.add(documentToBien(doc));
            }
            System.out.println("📋 " + list.size() + " biens trouvés pour l'agent " + agentId);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des biens de l'agent: " + e.getMessage());
            e.printStackTrace();
        }
        return list.toArray(new Immobilier.Bien[0]);
    }

    @Override
    public boolean checkDisponibilite(int bienId) {
        try {
            Document doc = collection.find(eq("_id", (long) bienId)).first();
            if (doc != null) {
                boolean disponible = doc.getBoolean("disponible", false);
                System.out.println("🔍 Bien " + bienId + " - Disponible: " + disponible);
                return disponible;
            }
            System.out.println("⚠️ Bien " + bienId + " non trouvé");
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification de disponibilité: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convertit un Document MongoDB en objet Bien CORBA
     */
    private Immobilier.Bien documentToBien(Document doc) {
        Immobilier.Bien b = new Immobilier.Bien();
        b.id = doc.getLong("_id").intValue();
        b.titre = doc.getString("titre");
        b.description = doc.getString("description");
        b.prix = doc.getDouble("prix");
        b.disponible = doc.getBoolean("disponible");
        b.agentId = doc.getLong("agentId").intValue();
        return b;
    }
}
