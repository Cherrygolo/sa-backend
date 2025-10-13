CREATE DATABASE sa;

# Utilisation de la BDD sa pour y insérer les tables 

USE sa;

# Création table client donnant son avis, avec ID généré automatiquement et adresse email unique

CREATE TABLE client (
    id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
    email VARCHAR(50) UNIQUE,
    telephone VARCHAR(15)
);

# Création table sentiment pour contenir le sentiment (texte, type) de l utilisateur (client_id)

CREATE TABLE sentiment (
    id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
    texte VARCHAR(50),
    type VARCHAR(10),
    client_id INTEGER,
    CONSTRAINT fk_sentiment_client FOREIGN KEY (client_id) REFERENCES client(id)
);

