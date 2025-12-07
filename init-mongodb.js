// Connexion à MongoDB
use ImmobilierDB

// Supprimer les anciennes données
db.utilisateurs.deleteMany({})
db.biens.deleteMany({})
db.contrats.deleteMany({})

// Créer des utilisateurs
db.utilisateurs.insertMany([
    {
        _id: NumberLong(1),
        nom: "Agent Smith",
        email: "agent@immobilier.com",
        password: "agent123",
        role: "agent"
    },
    {
        _id: NumberLong(2),
        nom: "Jean Dupont",
        email: "jean@email.com",
        password: "acheteur123",
        role: "acheteur"
    },
    {
        _id: NumberLong(3),
        nom: "Marie Martin",
        email: "marie@email.com",
        password: "acheteur123",
        role: "acheteur"
    }
])

// Créer des biens
db.biens.insertMany([
    {
        _id: NumberLong(1),
        titre: "Appartement 3 pièces Centre-Ville",
        description: "Bel appartement rénové avec balcon",
        prix: 180000.0,
        disponible: true,
        agentId: NumberLong(1)
    },
    {
        _id: NumberLong(2),
        titre: "Maison avec jardin",
        description: "Maison familiale 4 chambres",
        prix: 350000.0,
        disponible: true,
        agentId: NumberLong(1)
    },
    {
        _id: NumberLong(3),
        titre: "Studio étudiant",
        description: "Proche universités",
        prix: 85000.0,
        disponible: true,
        agentId: NumberLong(1)
    }
])

print("✅ Base de données initialisée!")