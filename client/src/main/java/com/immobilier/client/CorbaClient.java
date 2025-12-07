package com.immobilier.client;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import org.omg.CORBA.ORB;

import Immobilier.BienService;
import Immobilier.BienServiceHelper;
import Immobilier.Bien;

public class CorbaClient {

    private BienService service;

    /**
     * Initialise le client CORBA avec l'IOR fourni dans le fichier.
     * Cherche d'abord dans le répertoire home, puis dans le répertoire courant.
     */
    public CorbaClient(String iorPath) throws Exception {
        File iorFile = new File(iorPath);

        // Si le fichier n'existe pas, essayer d'autres emplacements
        if (!iorFile.exists()) {
            System.out.println("⚠️ Fichier IOR non trouvé à: " + iorPath);

            // Essayer dans le répertoire home
            String homeDir = System.getProperty("user.home");
            iorFile = new File(homeDir + File.separator + "BienService.ior");

            if (!iorFile.exists()) {
                // Essayer dans le répertoire courant
                iorFile = new File("BienService.ior");

                if (!iorFile.exists()) {
                    // Essayer dans le répertoire parent
                    iorFile = new File("../BienService.ior");

                    if (!iorFile.exists()) {
                        throw new Exception(
                                "❌ Fichier IOR introuvable!\n" +
                                        "Cherché dans:\n" +
                                        "- " + iorPath + "\n" +
                                        "- " + homeDir + "/BienService.ior\n" +
                                        "- ./BienService.ior\n" +
                                        "- ../BienService.ior\n\n" +
                                        "Assurez-vous que le serveur CORBA est démarré!"
                        );
                    }
                }
            }
        }

        System.out.println("✅ Fichier IOR trouvé: " + iorFile.getAbsolutePath());

        // Lire l'IOR depuis le fichier
        String ior = new String(Files.readAllBytes(iorFile.toPath())).trim();

        System.out.println("🔗 Connexion au serveur CORBA...");

        // Initialiser l'ORB
        ORB orb = ORB.init(new String[]{}, null);

        // Convertir la chaîne IOR en objet CORBA
        org.omg.CORBA.Object obj = orb.string_to_object(ior);

        // Récupérer l'interface BienService
        service = BienServiceHelper.narrow(obj);

        System.out.println("✅ Connexion CORBA établie!");
    }

    /**
     * Liste tous les biens.
     */
    public Bien[] listBiens() {
        return service.listBiens();
    }

    /**
     * Ajoute un bien et retourne son ID.
     */
    public int addBien(Bien b) {
        return service.addBien(b);
    }

    /**
     * Vérifie la disponibilité d'un bien par son ID.
     */
    public boolean checkDisponibilite(int id) {
        return service.checkDisponibilite(id);
    }

    /**
     * Liste les biens d'un agent spécifique.
     */
    public Bien[] listBiensAgent(int agentId) {
        return service.listBiensAgent(agentId);
    }

    /**
     * Exemple d'utilisation du client.
     */
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Démarrage du client CORBA...\n");

            // Chercher le fichier IOR
            String iorPath = System.getProperty("user.home") + File.separator + "BienService.ior";
            CorbaClient client = new CorbaClient(iorPath);

            // Lister les biens existants
            System.out.println("\n📋 Liste des biens :");
            Bien[] biens = client.listBiens();

            if (biens.length == 0) {
                System.out.println("  Aucun bien disponible");
            } else {
                for (Bien b : biens) {
                    System.out.println("  🏠 ID: " + b.id +
                            " | Titre: " + b.titre +
                            " | Prix: " + b.prix + " €" +
                            " | Disponible: " + (b.disponible ? "✅" : "❌"));
                }
            }

            // Ajouter un nouveau bien
            System.out.println("\n➕ Ajout d'un nouveau bien...");
            Bien newBien = new Bien();
            newBien.titre = "Appartement Test CORBA";
            newBien.description = "Ajouté via le client CORBA";
            newBien.prix = 180000.0;
            newBien.disponible = true;
            newBien.agentId = 1;

            int newId = client.addBien(newBien);
            System.out.println("✅ Bien ajouté avec ID: " + newId);

            // Vérifier la disponibilité
            boolean dispo = client.checkDisponibilite(newId);
            System.out.println("🔍 Disponibilité du bien " + newId + ": " + (dispo ? "✅ Disponible" : "❌ Non disponible"));

            System.out.println("\n✅ Test terminé avec succès!");

        } catch (Exception e) {
            System.err.println("\n❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}