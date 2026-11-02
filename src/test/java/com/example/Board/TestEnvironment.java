package com.example.Board;

/**
 * Proprietes imposees aux tests qui demarrent un contexte Spring.
 *
 * Pourquoi ? Spring Boot donne une priorite PLUS FORTE aux variables
 * d'environnement (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, ...)
 * qu'au fichier src/main/resources/application.properties. Sur une machine
 * ou ces variables sont deja definies (par exemple pour un PostgreSQL local),
 * les tests tenteraient de se connecter a cette base au lieu du H2 en memoire.
 *
 * @SpringBootTest(properties = ...) injecte ces valeurs comme proprietes
 * de test : elles ont la priorite la plus elevee, donc elles ecrasent
 * aussi bien les variables d'environnement que les fichiers de config.
 * Un test doit toujours etre "hermetique" : meme resultat sur toute machine.
 */
public final class TestEnvironment {

    public static final String DATASOURCE_URL =
            "spring.datasource.url=jdbc:h2:mem:boarddb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    public static final String DATASOURCE_DRIVER =
            "spring.datasource.driver-class-name=org.h2.Driver";
    public static final String DATASOURCE_USERNAME =
            "spring.datasource.username=sa";
    public static final String DATASOURCE_PASSWORD =
            "spring.datasource.password=";
    public static final String JPA_DDL_AUTO =
            "spring.jpa.hibernate.ddl-auto=create-drop";
    public static final String JPA_DIALECT =
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect";
    public static final String JPA_OPEN_IN_VIEW =
            "spring.jpa.open-in-view=false";
    public static final String SWAGGER_UI_PATH =
            "springdoc.swagger-ui.path=/docs";

    private TestEnvironment() {
    }
}
