
package com.immobilier.corba;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CorbaServer {
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Démarrage du serveur CORBA...\n");

            // Initialiser l'ORB
            ORB orb = ORB.init(args, null);

            // Obtenir le POA root
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Créer le servant
            BienServiceImpl servant = new BienServiceImpl();

            // Enregistrer le servant
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(servant);

            // Obtenir l'IOR
            String ior = orb.object_to_string(ref);

            // Sauvegarder l'IOR dans plusieurs emplacements
            String homeDir = System.getProperty("user.home");
            String[] iorPaths = {
                    homeDir + File.separator + "BienService.ior",
                    "BienService.ior",
                    "client" + File.separator + "BienService.ior"
            };

            for (String path : iorPaths) {
                try {
                    File iorFile = new File(path);
                    iorFile.getParentFile().mkdirs();

                    try (PrintWriter out = new PrintWriter(new FileWriter(iorFile))) {
                        out.println(ior);
                    }
                    System.out.println("✅ IOR sauvegardé dans: " + iorFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("⚠️  Impossible de sauvegarder dans: " + path);
                }
            }

            System.out.println("\n✅ Serveur CORBA prêt!");
            System.out.println("📡 En attente de connexions...\n");

            orb.run();

        } catch (Exception e) {
            System.err.println("❌ Erreur serveur CORBA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}