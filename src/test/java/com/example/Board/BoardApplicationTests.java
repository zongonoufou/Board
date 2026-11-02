package com.example.Board;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifie simplement que le contexte Spring demarre (controle les beans,
 * les requetes Spring Data et la configuration JPA).
 * Les proprietes de TestEnvironment garantissent l'utilisation de H2,
 * quelles que soient les variables d'environnement de la machine.
 */
@SpringBootTest(properties = {
        TestEnvironment.DATASOURCE_URL,
        TestEnvironment.DATASOURCE_DRIVER,
        TestEnvironment.DATASOURCE_USERNAME,
        TestEnvironment.DATASOURCE_PASSWORD,
        TestEnvironment.JPA_DDL_AUTO,
        TestEnvironment.JPA_DIALECT,
        TestEnvironment.JPA_OPEN_IN_VIEW,
        TestEnvironment.SWAGGER_UI_PATH
})
class BoardApplicationTests {

    @Test
    void contextLoads() {
    }

}
