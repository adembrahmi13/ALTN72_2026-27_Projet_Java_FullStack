CREATE DATABASE IF NOT EXISTS guardian_bdd CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE station (

                        id INT AUTO_INCREMENT PRIMARY KEY,
                        nom VARCHAR(100) NOT NULL
);

USE guardian_bdd;

DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS incident;
DROP TABLE IF EXISTS prestataire;
DROP TABLE IF EXISTS equipement;
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS utilisateur;



CREATE TABLE equipement (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            reference_unique VARCHAR(50) NOT NULL UNIQUE,
                            libelle_description TEXT,
                            type VARCHAR(50) NOT NULL,
                            station_id INT NOT NULL,
                            etat_fonctionnement VARCHAR(50) NOT NULL,
                            FOREIGN KEY (station_id) REFERENCES station(id)
);

CREATE TABLE prestataire (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             nom VARCHAR(100) NOT NULL,
                             contact VARCHAR(255) NOT NULL
);

CREATE TABLE incident (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          equipement_id INT NOT NULL,
                          description TEXT NOT NULL,
                          date_heure_ouverture DATETIME NOT NULL,
                          statut VARCHAR(50) NOT NULL,
                          prestataire_id INT,
                          date_heure_resolution DATETIME,
                          FOREIGN KEY (equipement_id) REFERENCES equipement(id),
                          FOREIGN KEY (prestataire_id) REFERENCES prestataire(id)
);

CREATE TABLE evaluation (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            incident_id INT NOT NULL,
                            note INT NOT NULL,
                            commentaire TEXT,
                            date DATE NOT NULL,
                            FOREIGN KEY (incident_id) REFERENCES incident(id)
);

CREATE TABLE utilisateur (
                             identifiant_connexion VARCHAR(50) PRIMARY KEY,
                             mot_de_passe VARCHAR(255) NOT NULL,
                             prenom VARCHAR(50) NOT NULL,
                             profil VARCHAR(50) NOT NULL
);

-- 1. Insertion dans la table station
INSERT INTO station (nom) VALUES
                              ('Station Nord'),
                              ('Station Sud');

-- 2. Insertion dans la table equipement
INSERT INTO equipement (reference_unique, libelle_description, type, station_id, etat_fonctionnement) VALUES
                                                                                                          ('EQ-001', 'Distributeur de billets principal', 'Distributeur', 1, 'Opérationnel'),
                                                                                                          ('EQ-002', 'Portique d acces automatique', 'Portique', 1, 'En maintenance'),
                                                                                                          ('EQ-003', 'Borne d information interactive', 'Borne', 2, 'Opérationnel');

-- 3. Insertion dans la table prestataire
INSERT INTO prestataire (nom, contact) VALUES
                                           ('Maintenance Express', 'contact@maintenance-express.fr / 01 23 45 67 89'),
                                           ('TechSecurite SARL', 'support@techsecurite.com / 01 98 76 54 32');

-- 4. Insertion dans la table incident
INSERT INTO incident (equipement_id, description, date_heure_ouverture, statut, prestataire_id, date_heure_resolution) VALUES
                                                                                                                           (2, 'Blocage mécanique du portique gauche', '2026-06-01 08:30:00', 'Résolu', 1, '2026-06-01 10:15:00'),
                                                                                                                           (1, 'Écran tactile figé', '2026-06-02 14:00:00', 'En cours', 2, NULL);

-- 5. Insertion dans la table evaluation
INSERT INTO evaluation (incident_id, note, commentaire, date) VALUES
    (1, 5, 'Intervention rapide et efficace.', '2026-06-01');
