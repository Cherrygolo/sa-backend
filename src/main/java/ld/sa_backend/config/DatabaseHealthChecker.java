package ld.sa_backend.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration Spring pour vérifier la santé de la base de données au démarrage de l'application.
 * Cette classe tente d'établir une connexion à la base de données définie dans 
 * <code>spring.datasource.url</code> (application.properties) afin de s'assurer que le service de base est disponible 
 * avant de continuer le démarrage de l'application.
 */

@Configuration
public class DatabaseHealthChecker {

    // Codes ANSI pour les couleurs dans la console
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";

    private final DataSource dataSource;

    // Lecture du host et port depuis application.properties
    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    public DatabaseHealthChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    @Profile("!test") // N'exécute ce bean que si le profil n'est pas "test"
    public boolean checkDatabaseConnection() {
        System.out.println("Vérification de la connexion à la base...");

        // Tentative de connexion à la base pour vérifier sa disponibilité
        try (Connection connection = dataSource.getConnection()) {
            System.out.println(ANSI_GREEN + "Connexion à la base OK (" + jdbcUrl + ")" + ANSI_RESET);
        } catch (SQLException e) {
            System.err.println(ANSI_RED + "ERREUR : Impossible de se connecter à la base !" + ANSI_RESET);
            System.err.println(ANSI_RED + "Vérifiez que le conteneur Docker MariaDB est lancé et écoute sur le port configuré." + ANSI_RESET);
            System.err.println(ANSI_RED + "Détails techniques: " + e.getMessage() + ANSI_RESET);
            System.exit(1); // Arrête Spring proprement avant Hibernate
        }

        return true; // Bean inutile mais obligatoire pour que Spring l’évalue
    }
}