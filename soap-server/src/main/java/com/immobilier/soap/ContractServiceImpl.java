package com.immobilier.soap;

import jakarta.jws.WebService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.Date;
import static com.mongodb.client.model.Filters.*;

@WebService(endpointInterface = "com.immobilier.soap.ContractService")
public class ContractServiceImpl implements ContractService {
    private final MongoCollection<Document> contratsCollection;
    private final MongoCollection<Document> biensCollection;

    public ContractServiceImpl() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.contratsCollection = db.getCollection("contrats");
        this.biensCollection = db.getCollection("biens");
        System.out.println("✅ ContractServiceImpl initialisé avec MongoDB");
    }

    @Override
    public String createContract(int bienId, int acheteurId) {
        try {
            // ✅ VALIDATION: Vérifier les paramètres
            if (bienId <= 0 || acheteurId <= 0) {
                return "❌ Erreur: ID invalide";
            }

            // Vérifier si le bien existe et est disponible
            Document bien = biensCollection.find(eq("_id", (long) bienId)).first();

            if (bien == null) {
                return "❌ Erreur: Bien " + bienId + " introuvable";
            }

            if (!bien.getBoolean("disponible", false)) {
                return "❌ Erreur: Bien " + bienId + " déjà vendu";
            }

            // ✅ VÉRIFIER: L'acheteur existe
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection usersCol = db.getCollection("utilisateurs");
            Document acheteur = (Document) usersCol.find(
                    and(eq("_id", (long) acheteurId), eq("role", "acheteur"))
            ).first();

            if (acheteur == null) {
                return "❌ Erreur: Acheteur " + acheteurId + " invalide ou non autorisé";
            }

            // Créer le contrat avec ID robuste
            long newId;
            Document lastContract = contratsCollection.find()
                    .sort(new Document("_id", -1))
                    .limit(1)
                    .first();

            if (lastContract != null) {
                newId = lastContract.getLong("_id") + 1;
            } else {
                newId = 1;
            }

            Document contrat = new Document()
                    .append("_id", newId)
                    .append("bienId", (long) bienId)
                    .append("acheteurId", (long) acheteurId)
                    .append("dateCreation", new Date())
                    .append("statut", "en_cours")
                    .append("montant", bien.getDouble("prix"));

            contratsCollection.insertOne(contrat);

            // Marquer le bien comme non disponible
            biensCollection.updateOne(
                    eq("_id", (long) bienId),
                    new Document("$set", new Document("disponible", false))
            );

            String message = "✅ Contrat #" + newId + " créé!\n" +
                    "Bien: " + bien.getString("titre") + "\n" +
                    "Prix: " + bien.getDouble("prix") + " €\n" +
                    "Acheteur: " + acheteur.getString("nom");

            System.out.println(message);
            return message;

        } catch (Exception e) {
            String error = "❌ Erreur: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return error;
        }
    }
}