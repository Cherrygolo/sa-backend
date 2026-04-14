package ld.feeltrack_backend.projection;

import ld.feeltrack_backend.enums.ReviewType;

/**
 * Projection Spring Data JPA permettant de récupérer le nombre de reviews par type.
 *
 * <p>
 * Cette interface est implémentée automatiquement par Spring Data JPA à l'exécution,
 * via un proxy dynamique. Chaque méthode est associée à une colonne du résultat
 * de la requête, en fonction de son nom.
 * </p>
 *
 * <p>
 * Les alias définis dans la requête JPQL/SQL doivent correspondre aux noms des getters.
 * </p>
 *
 * <pre>
 * Exemple :
 * {@code
 * SELECT r.type AS type, COUNT(r) AS count
 * FROM Review r
 * GROUP BY r.type
 * }
 * </pre>
 *
 * <p>
 * Cette projection est destinée à la couche repository uniquement.
 * </p>
 */

public interface ReviewCountProjection {

    ReviewType getType();
    long getCount();

}
